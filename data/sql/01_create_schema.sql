-- Schema creation script
-- This script creates all the necessary tables, indexes, and constraints
-- Spring Boot JPA will create these automatically, but this script serves as documentation

USE schooldb;

-- Department table
CREATE TABLE IF NOT EXISTS tbl_department (
    department_id BIGINT NOT NULL AUTO_INCREMENT,
    department_name VARCHAR(100) NOT NULL,
    department_address VARCHAR(255),
    department_code VARCHAR(10) UNIQUE,
    head_of_department VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    department_type ENUM('SCIENCE', 'ARTS', 'COMMERCE', 'ENGINEERING', 'MEDICAL', 'LAW'),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (department_id),
    INDEX idx_dept_name (department_name),
    INDEX idx_dept_code (department_code),
    INDEX idx_dept_type (department_type)
);

-- Course table
CREATE TABLE IF NOT EXISTS tbl_course (
    course_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    credit_hours DECIMAL(3,1),
    course_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'),
    is_active BOOLEAN DEFAULT TRUE,
    department_id BIGINT NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (course_id),
    FOREIGN KEY (department_id) REFERENCES tbl_department(department_id) ON DELETE RESTRICT,
    INDEX idx_course_code (course_code),
    INDEX idx_course_title (title),
    INDEX idx_department_id (department_id),
    INDEX idx_course_level (course_level)
);

-- Student table
CREATE TABLE IF NOT EXISTS tbl_student (
    student_id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email_address VARCHAR(100) NOT NULL UNIQUE,
    student_id_number VARCHAR(20) UNIQUE,
    admission_date DATE,
    date_of_birth DATE,
    student_status ENUM('ACTIVE', 'INACTIVE', 'GRADUATED', 'SUSPENDED', 'EXPELLED') DEFAULT 'ACTIVE',
    gpa DECIMAL(4,2),
    is_active BOOLEAN DEFAULT TRUE,
    guardian_name VARCHAR(100),
    guardian_email VARCHAR(100),
    guardian_mobile_number VARCHAR(20),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id),
    INDEX idx_student_name (first_name, last_name),
    INDEX idx_student_email (email_address),
    INDEX idx_admission_date (admission_date),
    INDEX idx_student_status (student_status),
    INDEX idx_student_id_number (student_id_number)
);

-- Enrollment table (junction table for Student-Course many-to-many relationship)
CREATE TABLE IF NOT EXISTS tbl_enrollment (
    enrollment_id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date DATE NOT NULL,
    semester VARCHAR(20) NOT NULL,
    academic_year INT NOT NULL,
    enrollment_status ENUM('ACTIVE', 'COMPLETED', 'DROPPED', 'WITHDRAWN', 'FAILED') DEFAULT 'ACTIVE',
    grade VARCHAR(5),
    grade_points DECIMAL(4,2),
    attendance_percentage DECIMAL(5,2),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (enrollment_id),
    FOREIGN KEY (student_id) REFERENCES tbl_student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES tbl_course(course_id) ON DELETE CASCADE,
    UNIQUE KEY enrollment_unique (student_id, course_id, semester, academic_year),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_enrollment_date (enrollment_date),
    INDEX idx_semester (semester),
    INDEX idx_academic_year (academic_year),
    INDEX idx_enrollment_status (enrollment_status)
);

-- Sequences for ID generation (MySQL uses AUTO_INCREMENT, but keeping for consistency)
-- These will be created automatically by Hibernate

-- Verify tables were created
SHOW TABLES;

-- Show table structures
DESCRIBE tbl_department;
DESCRIBE tbl_course;
DESCRIBE tbl_student;
DESCRIBE tbl_enrollment;

SELECT 'Schema created successfully' as status;