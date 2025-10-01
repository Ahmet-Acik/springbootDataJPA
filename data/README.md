# School Management System - Database Setup

This directory contains all the necessary files and scripts to set up a MySQL database for the Spring Boot JPA School Management System.

## 📁 Directory Structure

```
data/
├── sql/                          # SQL scripts for database initialization
│   ├── 00_init_database.sql      # Database creation and initial setup
│   ├── 01_create_schema.sql      # Table creation and schema setup
│   ├── 02_insert_sample_data.sql # Sample data insertion
│   └── 03_analysis_queries.sql   # Useful queries for testing and analysis
├── mysql/                        # MySQL-specific configuration files
├── setup_database.sh            # Automated database setup script
└── README.md                     # This file
```

## 🚀 Quick Start

### Prerequisites

1. **MySQL Server** installed and running
2. **MySQL Client** tools installed
3. **Java 21** installed
4. **Maven** installed

### Option 1: Fully Automated Setup (Recommended)

**Using Environment Variables (.env file):**
1. **Create credentials file:**
   ```bash
   cp data/.env.example data/.env
   # Edit data/.env with your MySQL credentials
   ```

2. **Run automated setup:**
   ```bash
   ./data/setup_auto.sh
   ```

3. **Start the application:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

**Using Command Line Environment Variables:**
```bash
# Set credentials and run in one command
DB_PASSWORD="your_password" ./data/setup_database.sh

# Or export and run
export DB_PASSWORD="your_password"
export DB_USER="root"
export DB_HOST="localhost"
./data/setup_database.sh
```

### Option 1b: MySQL Config File Approach

1. **Create MySQL config file:**
   ```bash
   cp data/mysql/my.cnf.example data/mysql/my.cnf
   # Edit data/mysql/my.cnf with your credentials
   ```

2. **Run config-based setup:**
   ```bash
   ./data/setup_config.sh
   ```

### Option 1c: Interactive Setup (Original)

1. **Run the setup script:**
   ```bash
   ./data/setup_database.sh
   ```

2. **Enter your MySQL root password when prompted** *(only once - the script securely stores and reuses it)*

> **Security Note:** All credential files (`.env`, `my.cnf`) are automatically excluded from version control via `.gitignore`.

### Option 2: Manual Setup

1. **Connect to MySQL:**
   ```bash
   mysql -u root -p
   ```

2. **Run the SQL scripts in order:**
   ```sql
   source data/sql/00_init_database.sql
   source data/sql/01_create_schema.sql
   source data/sql/02_insert_sample_data.sql
   ```

3. **Start the application:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

### Option 3: Spring Boot Auto-Initialization

1. **Configure your database connection in `application-mysql.properties`**

2. **Start the application with both mysql and init profiles:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

3. **The `DatabaseInitializer` component will automatically create sample data**

## 🔧 Configuration

### Database Connection

Update the following properties in `src/main/resources/application-mysql.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/schooldb?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### Environment Variables

You can also use environment variables:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=schooldb
export DB_USER=root
export DB_PASSWORD=your_password
```

## 📊 Sample Data

The database will be populated with:

- **8 Departments** (Computer Science, Mathematics, English Literature, etc.)
- **22 Courses** across different levels (Beginner to Advanced)
- **15 Students** with realistic information
- **40+ Enrollments** with grades and attendance data

### Sample Departments:
- Computer Science (Engineering)
- Mathematics (Science)
- English Literature (Arts)
- Business Administration (Commerce)
- Physics (Science)
- Chemistry (Science)
- History (Arts)
- Psychology (Science)

### Sample Students:
- Alice Johnson (STU001) - GPA: 3.75
- Bob Smith (STU002) - GPA: 3.25
- Carol Davis (STU003) - GPA: 3.90
- And 12 more...

## 🔍 Testing and Analysis

### Run Analysis Queries

```bash
mysql -u root -p schooldb < data/sql/03_analysis_queries.sql
```

### Key Analysis Queries Include:

1. **Department Statistics** - Course counts per department
2. **Course Enrollment Statistics** - Popular courses and performance
3. **Student Performance Analysis** - GPA and enrollment patterns
4. **Semester-wise Enrollment Summary** - Trends over time
5. **Top Performing Students by Department**
6. **Course Difficulty Analysis** - Based on average grades
7. **Low Attendance Warnings** - Students needing attention
8. **Grade Distribution by Department**
9. **Cross-Department Enrollment Patterns**

### Sample Query Results:

```sql
-- Top performing students
SELECT 
    s.student_id_number,
    CONCAT(s.first_name, ' ', s.last_name) as full_name,
    s.gpa,
    COUNT(e.enrollment_id) as total_enrollments
FROM tbl_student s
LEFT JOIN tbl_enrollment e ON s.student_id = e.student_id
GROUP BY s.student_id
ORDER BY s.gpa DESC;
```

## 🌟 JPA Features Demonstrated

This database setup showcases various JPA features:

### Entity Relationships
- **@OneToMany**: Department → Courses, Student → Enrollments
- **@ManyToOne**: Course → Department, Enrollment → Student/Course
- **@ManyToMany**: Student ↔ Course (through Enrollment)
- **@Embedded**: Student → Guardian

### Advanced JPA Annotations
- **@NamedQuery**: Custom queries defined at entity level
- **@Index**: Database indexes for performance
- **@UniqueConstraint**: Unique constraints
- **@Temporal**: Date/time handling
- **@Enumerated**: Enum value mapping
- **@CreatedDate/@LastModifiedDate**: JPA Auditing

### Query Types
- **Derived Queries**: `findByFirstName()`, `findByDepartmentType()`
- **JPQL Queries**: Custom queries using entity names
- **Native SQL**: Direct SQL queries for complex operations
- **Projections**: Custom return types with interfaces
- **Pagination**: `Pageable` support for large datasets

### Transaction Management
- **@Transactional**: Method-level transaction control
- **Propagation**: REQUIRED, REQUIRES_NEW, NEVER, MANDATORY
- **Isolation Levels**: READ_COMMITTED, READ_UNCOMMITTED
- **Rollback Scenarios**: Custom rollback conditions
- **Batch Operations**: Bulk insert/update operations

## 🚨 Troubleshooting

### Common Issues:

1. **MySQL Connection Refused**
   ```bash
   # Start MySQL service
   sudo systemctl start mysql
   # or on macOS
   brew services start mysql
   ```

2. **Permission Denied**
   ```bash
   # Make setup script executable
   chmod +x data/setup_database.sh
   ```

3. **Database Already Exists**
   - The scripts handle existing databases gracefully
   - Data will be truncated and recreated

4. **JPA Schema Creation Issues**
   ```properties
   # Use create-drop for development
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

### Logs and Debugging:

Enable detailed logging:
```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.springframework.orm.jpa=DEBUG
```

## 📈 Performance Considerations

### Indexes Created:
- Primary keys on all tables
- Unique indexes on email addresses and codes
- Composite indexes on frequently queried columns
- Foreign key indexes for join performance

### Connection Pooling:
- HikariCP configured with optimal settings
- Maximum pool size: 20 connections
- Connection timeout: 30 seconds
- Idle timeout: 5 minutes

### Batch Processing:
- Hibernate batch size: 25
- Order inserts and updates for better performance
- Versioned data batching enabled

## 🔐 Security Notes

- **Never use root user in production**
- **Create dedicated database user with limited privileges**
- **Use environment variables for sensitive configuration**
- **Enable SSL for production databases**

```sql
-- Create dedicated user (run as admin)
CREATE USER 'schoolapp'@'localhost' IDENTIFIED BY 'secure_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON schooldb.* TO 'schoolapp'@'localhost';
FLUSH PRIVILEGES;
```

## 📚 Learning Resources

This setup is designed for learning JPA concepts:

1. **Basic Concepts**: Entity mapping, relationships
2. **Intermediate**: Custom queries, projections, auditing
3. **Advanced**: Transaction management, performance optimization
4. **Expert**: Custom repositories, specifications, criteria API

## 🤝 Contributing

To add more sample data or modify the schema:

1. Update the SQL scripts in `data/sql/`
2. Modify the `DatabaseInitializer` class for programmatic data
3. Update configuration files as needed
4. Test with different profiles (test, mysql, production)

---

**Happy Learning with Spring Data JPA! 🎓**