-- Useful queries for testing and analysis
-- This script contains various queries to test JPA functionality and analyze data

USE schooldb;

-- 1. Department Statistics
SELECT 
    d.department_name,
    d.department_code,
    d.head_of_department,
    COUNT(c.course_id) as total_courses,
    COUNT(CASE WHEN c.is_active = TRUE THEN 1 END) as active_courses
FROM tbl_department d
LEFT JOIN tbl_course c ON d.department_id = c.department_id
GROUP BY d.department_id, d.department_name, d.department_code, d.head_of_department
ORDER BY total_courses DESC;

-- 2. Course Enrollment Statistics
SELECT 
    c.course_code,
    c.title,
    d.department_name,
    COUNT(e.enrollment_id) as total_enrollments,
    COUNT(CASE WHEN e.enrollment_status = 'ACTIVE' THEN 1 END) as active_enrollments,
    COUNT(CASE WHEN e.enrollment_status = 'COMPLETED' THEN 1 END) as completed_enrollments,
    AVG(e.grade_points) as average_grade,
    AVG(e.attendance_percentage) as average_attendance
FROM tbl_course c
LEFT JOIN tbl_enrollment e ON c.course_id = e.course_id
LEFT JOIN tbl_department d ON c.department_id = d.department_id
GROUP BY c.course_id, c.course_code, c.title, d.department_name
ORDER BY total_enrollments DESC;

-- 3. Student Performance Analysis
SELECT 
    s.student_id_number,
    CONCAT(s.first_name, ' ', s.last_name) as full_name,
    s.student_status,
    s.gpa,
    COUNT(e.enrollment_id) as total_enrollments,
    COUNT(CASE WHEN e.enrollment_status = 'COMPLETED' THEN 1 END) as completed_courses,
    COUNT(CASE WHEN e.enrollment_status = 'ACTIVE' THEN 1 END) as current_enrollments,
    AVG(e.attendance_percentage) as average_attendance
FROM tbl_student s
LEFT JOIN tbl_enrollment e ON s.student_id = e.student_id
WHERE s.is_active = TRUE
GROUP BY s.student_id, s.student_id_number, s.first_name, s.last_name, s.student_status, s.gpa
ORDER BY s.gpa DESC;

-- 4. Semester-wise Enrollment Summary
SELECT 
    e.semester,
    e.academic_year,
    COUNT(DISTINCT e.student_id) as unique_students,
    COUNT(e.enrollment_id) as total_enrollments,
    COUNT(CASE WHEN e.enrollment_status = 'COMPLETED' THEN 1 END) as completed,
    COUNT(CASE WHEN e.enrollment_status = 'ACTIVE' THEN 1 END) as active,
    COUNT(CASE WHEN e.enrollment_status = 'DROPPED' THEN 1 END) as dropped,
    AVG(e.grade_points) as average_grade_points
FROM tbl_enrollment e
GROUP BY e.semester, e.academic_year
ORDER BY e.academic_year DESC, e.semester;

-- 5. Top Performing Students by Department
SELECT 
    d.department_name,
    CONCAT(s.first_name, ' ', s.last_name) as student_name,
    s.student_id_number,
    AVG(e.grade_points) as avg_grade_in_dept,
    COUNT(e.enrollment_id) as courses_taken
FROM tbl_student s
JOIN tbl_enrollment e ON s.student_id = e.student_id
JOIN tbl_course c ON e.course_id = c.course_id
JOIN tbl_department d ON c.department_id = d.department_id
WHERE e.grade_points IS NOT NULL
GROUP BY d.department_id, d.department_name, s.student_id, s.first_name, s.last_name, s.student_id_number
HAVING COUNT(e.enrollment_id) >= 2  -- Students with at least 2 courses in the department
ORDER BY d.department_name, avg_grade_in_dept DESC;

-- 6. Course Difficulty Analysis (based on average grades)
SELECT 
    c.course_code,
    c.title,
    c.course_level,
    d.department_name,
    COUNT(e.enrollment_id) as total_enrollments,
    AVG(e.grade_points) as average_grade,
    MIN(e.grade_points) as min_grade,
    MAX(e.grade_points) as max_grade,
    STDDEV(e.grade_points) as grade_std_dev,
    CASE 
        WHEN AVG(e.grade_points) >= 3.5 THEN 'Easy'
        WHEN AVG(e.grade_points) >= 2.5 THEN 'Moderate'
        ELSE 'Difficult'
    END as difficulty_level
FROM tbl_course c
LEFT JOIN tbl_enrollment e ON c.course_id = e.course_id AND e.grade_points IS NOT NULL
LEFT JOIN tbl_department d ON c.department_id = d.department_id
GROUP BY c.course_id, c.course_code, c.title, c.course_level, d.department_name
HAVING COUNT(e.enrollment_id) >= 3  -- Courses with at least 3 graded enrollments
ORDER BY average_grade ASC;

-- 7. Students with Low Attendance Warning
SELECT 
    s.student_id_number,
    CONCAT(s.first_name, ' ', s.last_name) as student_name,
    s.guardian_name,
    s.guardian_email,
    c.course_code,
    c.title,
    e.attendance_percentage,
    e.semester
FROM tbl_student s
JOIN tbl_enrollment e ON s.student_id = e.student_id
JOIN tbl_course c ON e.course_id = c.course_id
WHERE e.attendance_percentage < 75.0 
    AND e.enrollment_status = 'ACTIVE'
ORDER BY e.attendance_percentage ASC;

-- 8. Department-wise Grade Distribution
SELECT 
    d.department_name,
    e.grade,
    COUNT(*) as count,
    ROUND((COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (PARTITION BY d.department_id)), 2) as percentage
FROM tbl_department d
JOIN tbl_course c ON d.department_id = c.department_id
JOIN tbl_enrollment e ON c.course_id = e.course_id
WHERE e.grade IS NOT NULL
GROUP BY d.department_id, d.department_name, e.grade
ORDER BY d.department_name, e.grade;

-- 9. Most Recent Enrollments
SELECT 
    s.student_id_number,
    CONCAT(s.first_name, ' ', s.last_name) as student_name,
    c.course_code,
    c.title,
    e.enrollment_date,
    e.semester,
    e.enrollment_status
FROM tbl_enrollment e
JOIN tbl_student s ON e.student_id = s.student_id
JOIN tbl_course c ON e.course_id = c.course_id
ORDER BY e.enrollment_date DESC
LIMIT 20;

-- 10. Students Enrolled in Multiple Departments
SELECT 
    s.student_id_number,
    CONCAT(s.first_name, ' ', s.last_name) as student_name,
    COUNT(DISTINCT d.department_id) as departments_count,
    GROUP_CONCAT(DISTINCT d.department_name ORDER BY d.department_name) as departments
FROM tbl_student s
JOIN tbl_enrollment e ON s.student_id = e.student_id
JOIN tbl_course c ON e.course_id = c.course_id
JOIN tbl_department d ON c.department_id = d.department_id
GROUP BY s.student_id, s.student_id_number, s.first_name, s.last_name
HAVING COUNT(DISTINCT d.department_id) > 1
ORDER BY departments_count DESC;

-- 11. Course Prerequisites Analysis (hypothetical - would require additional table)
-- This query shows courses by level in each department
SELECT 
    d.department_name,
    c.course_level,
    COUNT(*) as course_count,
    AVG(c.credit_hours) as avg_credit_hours,
    GROUP_CONCAT(c.course_code ORDER BY c.course_code) as courses
FROM tbl_department d
JOIN tbl_course c ON d.department_id = c.department_id
WHERE c.is_active = TRUE
GROUP BY d.department_id, d.department_name, c.course_level
ORDER BY d.department_name, 
    CASE c.course_level 
        WHEN 'BEGINNER' THEN 1 
        WHEN 'INTERMEDIATE' THEN 2 
        WHEN 'ADVANCED' THEN 3 
        WHEN 'EXPERT' THEN 4 
    END;

-- 12. Guardian Contact Information for Students with Issues
SELECT DISTINCT
    s.student_id_number,
    CONCAT(s.first_name, ' ', s.last_name) as student_name,
    s.guardian_name,
    s.guardian_email,
    s.guardian_mobile_number,
    'Low GPA' as issue_type
FROM tbl_student s
WHERE s.gpa < 2.5 AND s.is_active = TRUE

UNION ALL

SELECT DISTINCT
    s.student_id_number,
    CONCAT(s.first_name, ' ', s.last_name) as student_name,
    s.guardian_name,
    s.guardian_email,
    s.guardian_mobile_number,
    'Low Attendance' as issue_type
FROM tbl_student s
JOIN tbl_enrollment e ON s.student_id = e.student_id
WHERE e.attendance_percentage < 75.0 
    AND e.enrollment_status = 'ACTIVE'
    AND s.is_active = TRUE

ORDER BY student_name, issue_type;