package com.example.springdatajpa.service;

import com.example.springdatajpa.entity.Course;
import com.example.springdatajpa.entity.Enrollment;
import com.example.springdatajpa.entity.Student;
import com.example.springdatajpa.repository.CourseRepository;
import com.example.springdatajpa.repository.EnrollmentRepository;
import com.example.springdatajpa.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public Enrollment createEnrollment(Enrollment enrollment) {
        if (enrollment.getEnrollmentDate() == null) {
            enrollment.setEnrollmentDate(LocalDate.now());
        }
        if (enrollment.getEnrollmentStatus() == null) {
            enrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE);
        }
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment enrollStudentInCourse(Long studentId, Long courseId, String semester, Integer academicYear) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        // Check if enrollment already exists
        Optional<Enrollment> existingEnrollment = enrollmentRepository
                .findByStudentAndCourseAndSemester(student, course, semester);
        if (existingEnrollment.isPresent()) {
            throw new RuntimeException("Student is already enrolled in this course for the specified semester");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .semester(semester)
                .academicYear(academicYear)
                .enrollmentDate(LocalDate.now())
                .enrollmentStatus(Enrollment.EnrollmentStatus.ACTIVE)
                .attendancePercentage(BigDecimal.ZERO)
                .build();

        return enrollmentRepository.save(enrollment);
    }

    public Optional<Enrollment> findById(Long id) {
        return enrollmentRepository.findById(id);
    }

    public List<Enrollment> findAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public Page<Enrollment> findAllEnrollments(Pageable pageable) {
        return enrollmentRepository.findAll(pageable);
    }

    public List<Enrollment> findByStudent(Long studentId) {
        return enrollmentRepository.findByStudentStudentId(studentId);
    }

    public List<Enrollment> findByCourse(Long courseId) {
        return enrollmentRepository.findByCourseCourseId(courseId);
    }

    public List<Enrollment> findByStudentAndSemester(Long studentId, String semester) {
        return enrollmentRepository.findByStudentAndSemester(studentId, semester);
    }

    public List<Enrollment> findByCourseAndAcademicYear(Long courseId, Integer academicYear) {
        return enrollmentRepository.findByCourseAndAcademicYear(courseId, academicYear);
    }

    public List<Enrollment> findByStatus(Enrollment.EnrollmentStatus status) {
        return enrollmentRepository.findByEnrollmentStatus(status);
    }

    public List<Enrollment> findByEnrollmentDateBetween(LocalDate startDate, LocalDate endDate) {
        return enrollmentRepository.findByEnrollmentDateBetween(startDate, endDate);
    }

    public List<Enrollment> findGradedEnrollments() {
        return enrollmentRepository.findByGradeIsNotNull();
    }

    public List<Enrollment> findByDepartmentAndSemester(Long departmentId, String semester) {
        return enrollmentRepository.findEnrollmentsByDepartmentAndSemester(departmentId, semester);
    }

    @Transactional
    public Enrollment updateEnrollment(Enrollment enrollment) {
        if (!enrollmentRepository.existsById(enrollment.getEnrollmentId())) {
            throw new RuntimeException("Enrollment not found with id: " + enrollment.getEnrollmentId());
        }
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment updateEnrollmentStatus(Long enrollmentId, Enrollment.EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));
        enrollment.setEnrollmentStatus(status);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment updateGrade(Long enrollmentId, String grade, BigDecimal gradePoints) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));
        enrollment.setGrade(grade);
        enrollment.setGradePoints(gradePoints);
        if (gradePoints.compareTo(BigDecimal.ZERO) > 0) {
            enrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.COMPLETED);
        }
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment updateAttendance(Long enrollmentId, BigDecimal attendancePercentage) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));
        enrollment.setAttendancePercentage(attendancePercentage);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new RuntimeException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
    }

    @Transactional
    public Enrollment dropEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));
        enrollment.setEnrollmentStatus(Enrollment.EnrollmentStatus.DROPPED);
        return enrollmentRepository.save(enrollment);
    }

    public long countEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentStudentId(studentId).size();
    }

    public long countEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseCourseId(courseId).size();
    }

    public long countEnrollmentsByStatus(Enrollment.EnrollmentStatus status) {
        return enrollmentRepository.findByEnrollmentStatus(status).size();
    }

    public Double getAverageGradeByStudent(Long studentId) {
        return enrollmentRepository.calculateStudentGPA(studentId);
    }

    public List<EnrollmentRepository.CourseEnrollmentStats> getCourseStatistics(Integer academicYear) {
        return enrollmentRepository.getCourseEnrollmentStatistics(academicYear);
    }

    public List<EnrollmentRepository.StudentEnrollmentSummary> getTopStudentsByGrade(Integer academicYear) {
        return enrollmentRepository.getStudentPerformanceSummary(academicYear);
    }

    public List<Object[]> getTopEnrolledStudents(Integer academicYear, int limit) {
        return enrollmentRepository.findMostActiveStudentsByYear(academicYear, limit);
    }

    public List<Enrollment> searchEnrollments(Long studentId, Long courseId, String semester, 
                                            Integer academicYear, Enrollment.EnrollmentStatus status) {
        return enrollmentRepository.searchEnrollments(studentId, courseId, semester, academicYear, status);
    }

    @Transactional
    public void bulkUpdateGrades(List<Long> enrollmentIds, String grade, BigDecimal gradePoints) {
        for (Long enrollmentId : enrollmentIds) {
            updateGrade(enrollmentId, grade, gradePoints);
        }
    }

    public boolean isStudentEnrolledInCourse(Long studentId, Long courseId, String semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        
        return enrollmentRepository.findByStudentAndCourseAndSemester(student, course, semester).isPresent();
    }

    public List<Student> getStudentsInCourse(Long courseId, String semester) {
        return enrollmentRepository.findByCourseAndAcademicYear(courseId, null) // Using null for now, can be improved
                .stream()
                .filter(e -> semester.equals(e.getSemester()))
                .map(Enrollment::getStudent)
                .toList();
    }

    public List<Course> getCoursesForStudent(Long studentId, String semester) {
        return enrollmentRepository.findByStudentAndSemester(studentId, semester)
                .stream()
                .map(Enrollment::getCourse)
                .toList();
    }
}