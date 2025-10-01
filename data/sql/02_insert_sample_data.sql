-- Sample data insertion script
-- This script populates the database with realistic sample data for testing and development

USE schooldb;

-- Clear existing data (be careful with this in production!)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE tbl_enrollment;
TRUNCATE TABLE tbl_course;
TRUNCATE TABLE tbl_student;
TRUNCATE TABLE tbl_department;
SET FOREIGN_KEY_CHECKS = 1;

-- Insert Departments
INSERT INTO tbl_department (department_name, department_address, department_code, head_of_department, department_type, is_active) VALUES
('Computer Science', '123 Tech Building', 'CS', 'Dr. John Smith', 'ENGINEERING', TRUE),
('Mathematics', '456 Science Hall', 'MATH', 'Dr. Sarah Johnson', 'SCIENCE', TRUE),
('English Literature', '789 Arts Building', 'ENG', 'Dr. Michael Brown', 'ARTS', TRUE),
('Business Administration', '321 Business Center', 'BUS', 'Dr. Emily Davis', 'COMMERCE', TRUE),
('Physics', '654 Physics Lab', 'PHY', 'Dr. Robert Wilson', 'SCIENCE', TRUE),
('Chemistry', '987 Chemistry Building', 'CHEM', 'Dr. Lisa Anderson', 'SCIENCE', TRUE),
('History', '147 Humanities Hall', 'HIST', 'Dr. David Miller', 'ARTS', TRUE),
('Psychology', '258 Social Sciences', 'PSY', 'Dr. Jennifer Taylor', 'SCIENCE', TRUE);

-- Insert Courses
INSERT INTO tbl_course (title, course_code, description, credit_hours, course_level, department_id, is_active) VALUES
-- Computer Science Courses
('Introduction to Programming', 'CS101', 'Basic programming concepts using Java', 3.0, 'BEGINNER', 1, TRUE),
('Data Structures and Algorithms', 'CS201', 'Study of fundamental data structures and algorithms', 4.0, 'INTERMEDIATE', 1, TRUE),
('Database Management Systems', 'CS301', 'Design and implementation of database systems', 3.0, 'INTERMEDIATE', 1, TRUE),
('Software Engineering', 'CS401', 'Software development lifecycle and methodologies', 3.0, 'ADVANCED', 1, TRUE),
('Machine Learning', 'CS501', 'Introduction to machine learning algorithms', 4.0, 'ADVANCED', 1, TRUE),

-- Mathematics Courses
('Calculus I', 'MATH101', 'Differential and integral calculus', 4.0, 'BEGINNER', 2, TRUE),
('Calculus II', 'MATH102', 'Advanced calculus concepts', 4.0, 'INTERMEDIATE', 2, TRUE),
('Linear Algebra', 'MATH201', 'Vector spaces and linear transformations', 3.0, 'INTERMEDIATE', 2, TRUE),
('Statistics', 'MATH301', 'Probability and statistical inference', 3.0, 'INTERMEDIATE', 2, TRUE),
('Discrete Mathematics', 'MATH401', 'Mathematical structures for computer science', 3.0, 'ADVANCED', 2, TRUE),

-- English Literature Courses
('English Composition', 'ENG101', 'Writing and communication skills', 3.0, 'BEGINNER', 3, TRUE),
('World Literature', 'ENG201', 'Survey of world literature', 3.0, 'INTERMEDIATE', 3, TRUE),
('Shakespeare Studies', 'ENG301', 'Study of Shakespeare\'s works', 3.0, 'ADVANCED', 3, TRUE),
('Modern Poetry', 'ENG401', 'Contemporary poetry analysis', 3.0, 'ADVANCED', 3, TRUE),

-- Business Administration Courses
('Principles of Management', 'BUS101', 'Fundamental management concepts', 3.0, 'BEGINNER', 4, TRUE),
('Marketing Fundamentals', 'BUS201', 'Basic marketing principles', 3.0, 'INTERMEDIATE', 4, TRUE),
('Financial Accounting', 'BUS301', 'Financial reporting and analysis', 3.0, 'INTERMEDIATE', 4, TRUE),
('Strategic Management', 'BUS401', 'Corporate strategy and planning', 3.0, 'ADVANCED', 4, TRUE),

-- Physics Courses
('General Physics I', 'PHY101', 'Mechanics and thermodynamics', 4.0, 'BEGINNER', 5, TRUE),
('General Physics II', 'PHY102', 'Electricity and magnetism', 4.0, 'INTERMEDIATE', 5, TRUE),
('Quantum Physics', 'PHY301', 'Introduction to quantum mechanics', 4.0, 'ADVANCED', 5, TRUE),

-- Chemistry Courses
('General Chemistry', 'CHEM101', 'Basic chemical principles', 4.0, 'BEGINNER', 6, TRUE),
('Organic Chemistry', 'CHEM201', 'Study of carbon compounds', 4.0, 'INTERMEDIATE', 6, TRUE),
('Physical Chemistry', 'CHEM301', 'Chemical thermodynamics and kinetics', 4.0, 'ADVANCED', 6, TRUE);

-- Insert Students
INSERT INTO tbl_student (first_name, last_name, email_address, student_id_number, admission_date, date_of_birth, student_status, gpa, guardian_name, guardian_email, guardian_mobile_number, is_active) VALUES
('Alice', 'Johnson', 'alice.johnson@student.edu', 'STU001', '2023-09-01', '2002-05-15', 'ACTIVE', 3.75, 'Robert Johnson', 'robert.johnson@email.com', '+1-555-0101', TRUE),
('Bob', 'Smith', 'bob.smith@student.edu', 'STU002', '2023-09-01', '2002-03-22', 'ACTIVE', 3.25, 'Mary Smith', 'mary.smith@email.com', '+1-555-0102', TRUE),
('Carol', 'Davis', 'carol.davis@student.edu', 'STU003', '2023-09-01', '2002-07-08', 'ACTIVE', 3.90, 'James Davis', 'james.davis@email.com', '+1-555-0103', TRUE),
('David', 'Wilson', 'david.wilson@student.edu', 'STU004', '2022-09-01', '2001-12-03', 'ACTIVE', 3.50, 'Linda Wilson', 'linda.wilson@email.com', '+1-555-0104', TRUE),
('Emma', 'Brown', 'emma.brown@student.edu', 'STU005', '2022-09-01', '2001-09-18', 'ACTIVE', 3.85, 'Michael Brown', 'michael.brown@email.com', '+1-555-0105', TRUE),
('Frank', 'Miller', 'frank.miller@student.edu', 'STU006', '2024-01-15', '2003-01-25', 'ACTIVE', 3.60, 'Susan Miller', 'susan.miller@email.com', '+1-555-0106', TRUE),
('Grace', 'Taylor', 'grace.taylor@student.edu', 'STU007', '2022-09-01', '2001-11-12', 'ACTIVE', 3.95, 'David Taylor', 'david.taylor@email.com', '+1-555-0107', TRUE),
('Henry', 'Anderson', 'henry.anderson@student.edu', 'STU008', '2023-01-15', '2002-06-30', 'ACTIVE', 3.40, 'Patricia Anderson', 'patricia.anderson@email.com', '+1-555-0108', TRUE),
('Ivy', 'Martinez', 'ivy.martinez@student.edu', 'STU009', '2023-09-01', '2002-04-17', 'ACTIVE', 3.70, 'Carlos Martinez', 'carlos.martinez@email.com', '+1-555-0109', TRUE),
('Jack', 'Garcia', 'jack.garcia@student.edu', 'STU010', '2022-01-15', '2001-08-05', 'ACTIVE', 3.55, 'Maria Garcia', 'maria.garcia@email.com', '+1-555-0110', TRUE),
('Karen', 'Rodriguez', 'karen.rodriguez@student.edu', 'STU011', '2024-09-01', '2003-02-28', 'ACTIVE', 3.80, 'Jose Rodriguez', 'jose.rodriguez@email.com', '+1-555-0111', TRUE),
('Leo', 'Lopez', 'leo.lopez@student.edu', 'STU012', '2023-01-15', '2002-10-14', 'ACTIVE', 3.45, 'Carmen Lopez', 'carmen.lopez@email.com', '+1-555-0112', TRUE),
('Mia', 'Gonzalez', 'mia.gonzalez@student.edu', 'STU013', '2022-09-01', '2001-07-21', 'GRADUATED', 3.88, 'Fernando Gonzalez', 'fernando.gonzalez@email.com', '+1-555-0113', TRUE),
('Noah', 'Perez', 'noah.perez@student.edu', 'STU014', '2024-01-15', '2003-03-19', 'ACTIVE', 3.65, 'Isabella Perez', 'isabella.perez@email.com', '+1-555-0114', TRUE),
('Olivia', 'Turner', 'olivia.turner@student.edu', 'STU015', '2023-09-01', '2002-12-07', 'ACTIVE', 3.75, 'Christopher Turner', 'christopher.turner@email.com', '+1-555-0115', TRUE);

-- Insert Enrollments (Creating realistic enrollment patterns)
INSERT INTO tbl_enrollment (student_id, course_id, enrollment_date, semester, academic_year, enrollment_status, grade, grade_points, attendance_percentage) VALUES
-- Fall 2023 enrollments
(1, 1, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A', 4.0, 95.5),
(1, 6, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'B+', 3.3, 88.2),
(1, 11, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A-', 3.7, 92.1),
(1, 15, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'B', 3.0, 85.0),

(2, 1, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'B', 3.0, 82.5),
(2, 6, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'B-', 2.7, 79.8),
(2, 19, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'C+', 2.3, 75.5),
(2, 22, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'B+', 3.3, 87.2),

(3, 2, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A', 4.0, 96.8),
(3, 7, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A-', 3.7, 93.2),
(3, 12, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A', 4.0, 94.5),
(3, 16, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A-', 3.7, 91.8),

-- Spring 2024 enrollments
(1, 2, '2024-01-10', 'Spring 2024', 2024, 'COMPLETED', 'B+', 3.3, 89.7),
(1, 7, '2024-01-10', 'Spring 2024', 2024, 'COMPLETED', 'A-', 3.7, 91.2),
(1, 12, '2024-01-10', 'Spring 2024', 2024, 'COMPLETED', 'B', 3.0, 86.5),

(2, 2, '2024-01-10', 'Spring 2024', 2024, 'COMPLETED', 'C+', 2.3, 78.9),
(2, 8, '2024-01-10', 'Spring 2024', 2024, 'COMPLETED', 'B', 3.0, 83.4),
(2, 20, '2024-01-10', 'Spring 2024', 2024, 'COMPLETED', 'B-', 2.7, 81.2),

(4, 1, '2024-01-10', 'Spring 2024', 2024, 'COMPLETED', 'B+', 3.3, 88.9),
(4, 6, '2024-01-10', 'Spring 2024', 2024, 'COMPLETED', 'A-', 3.7, 92.7),
(4, 15, '2024-01-10', 'Spring 2024', 2024, 'COMPLETED', 'B', 3.0, 85.8),

-- Fall 2024 enrollments (current semester - some active, some completed)
(1, 3, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 87.5),
(1, 8, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 89.2),
(1, 13, '2024-08-15', 'Fall 2024', 2024, 'COMPLETED', 'A', 4.0, 95.8),

(2, 3, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 76.3),
(2, 9, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 82.1),
(2, 17, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 79.7),

(5, 1, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 94.2),
(5, 6, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 91.8),
(5, 11, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 93.5),

(6, 1, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 88.7),
(6, 19, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 85.9),
(6, 22, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 87.4),

-- Additional enrollments for variety
(7, 4, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A', 4.0, 97.2),
(7, 9, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A-', 3.7, 94.8),
(7, 14, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A', 4.0, 96.1),

(8, 1, '2023-01-10', 'Spring 2023', 2023, 'COMPLETED', 'B-', 2.7, 79.5),
(8, 6, '2023-01-10', 'Spring 2023', 2023, 'COMPLETED', 'C+', 2.3, 74.8),
(8, 15, '2023-01-10', 'Spring 2023', 2023, 'COMPLETED', 'B', 3.0, 83.2),

(9, 2, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 90.3),
(9, 7, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 88.7),
(9, 16, '2024-08-15', 'Fall 2024', 2024, 'ACTIVE', NULL, NULL, 91.5),

(10, 3, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'B+', 3.3, 89.4),
(10, 8, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'A-', 3.7, 92.8),
(10, 18, '2023-08-15', 'Fall 2023', 2023, 'COMPLETED', 'B', 3.0, 86.7);

-- Update student GPAs based on their enrollments (this would normally be calculated automatically)
UPDATE tbl_student SET gpa = (
    SELECT AVG(e.grade_points) 
    FROM tbl_enrollment e 
    WHERE e.student_id = tbl_student.student_id 
    AND e.grade_points IS NOT NULL
) WHERE student_id IN (SELECT DISTINCT student_id FROM tbl_enrollment WHERE grade_points IS NOT NULL);

-- Verify data insertion
SELECT 'Data insertion completed successfully' as status;

-- Show record counts
SELECT 
    (SELECT COUNT(*) FROM tbl_department) as departments,
    (SELECT COUNT(*) FROM tbl_course) as courses,
    (SELECT COUNT(*) FROM tbl_student) as students,
    (SELECT COUNT(*) FROM tbl_enrollment) as enrollments;