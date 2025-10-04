# Spring Boot Data JPA Learning Project

A comprehensive Spring Boot application demonstrating JPA/Hibernate features with secure credential management.

## Quick Start

### 1. Environment Setup (Recommended)

Use our interactive setup script for secure credential management:

```bash
./setup_env.sh
```

This script will:
- Create a `.env` file with your database credentials
- Configure either MySQL or H2 database
- Set secure file permissions
- Provide clear next steps

### 2. Manual Environment Setup

If you prefer manual setup:

```bash
# Copy the template
cp .env.example .env

# Edit .env with your actual credentials
nano .env

# Load environment variables
source .env
```

### 3. Run the Application

```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or with environment variables inline
DB_PASSWORD=your_password ./mvnw spring-boot:run
```

## Database Configuration

### MySQL (Production/Development)
```bash
# Set these environment variables
export DB_URL="jdbc:mysql://localhost:3306/schooldb"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
export DB_DRIVER="com.mysql.cj.jdbc.Driver"
export DB_DIALECT="org.hibernate.dialect.MySQLDialect"
```

### H2 (Testing)
```bash
# H2 is the default fallback - no configuration needed
# Or explicitly set:
export DB_URL="jdbc:h2:mem:testdb"
export DB_USERNAME="sa"
export DB_PASSWORD=""
```

## Project Structure

```
src/
├── main/
│   ├── java/com/example/springdatajpa/
│   │   ├── SpringDataJpaApplication.java
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # REST API controllers
│   │   ├── entity/           # JPA entities (Student, Course, Department, Enrollment, Guardian)
│   │   ├── exception/        # Custom exception handling
│   │   ├── repository/       # Spring Data repositories
│   │   └── service/          # Business logic services
│   └── resources/
│       ├── application.properties
│       ├── application-mysql.properties
│       └── application-test.properties
├── test/                     # 18 test classes with 201 test methods
│   ├── java/com/example/springdatajpa/
│   │   ├── api/              # REST Assured API tests (8 classes)
│   │   ├── controller/       # MockMvc controller tests (3 classes)
│   │   ├── repository/       # JPA repository tests (4 classes)
│   │   ├── service/          # Service layer tests (2 classes)
│   │   ├── integration/      # End-to-end tests (1 class)
│   │   └── debug/            # Debug/validation tests (2 classes)
│   └── resources/
│       └── application-test.properties
docs/
├── TESTING_GUIDE.md         # Comprehensive testing documentation
└── REST_ASSURED_TESTING.md  # REST Assured specific guide
data/
├── setup_database.sh        # Database initialization
└── sql/                     # SQL scripts
```

## Running Tests

This project includes **162 comprehensive executable tests** from **18 test classes** with **201 test methods** total, covering unit, integration, and end-to-end scenarios with a **99.4% success rate**.

```bash
# Run all tests - RECOMMENDED
./mvnw test

# Run tests quietly (less output)  
./mvnw test -q

# Run specific test class
./mvnw test -Dtest=StudentRepositoryTest

# Run specific test method
./mvnw test -Dtest=StudentRepositoryTest#shouldFindByEmail

# Run tests with specific category
./mvnw test -Dtest="*ApiTest"           # API tests
./mvnw test -Dtest="*RepositoryTest"    # Repository tests
./mvnw test -Dtest="*ControllerTest"    # Controller tests

# Run integration tests
./mvnw test -Dtest="*IntegrationTest"

# Run debug/validation tests
./mvnw test -Dtest="*ValidationTest,*DebugTest"
```

### Test Coverage
- **API Tests** (8 classes): REST Assured integration tests for all endpoints
- **Repository Tests** (4 classes): JPA repository testing with `@DataJpaTest`
- **Controller Tests** (3 classes): MockMvc web layer testing
- **Service Tests** (2 classes): Business logic unit testing
- **Integration Tests** (1 class): End-to-end application testing
- **Debug/Validation Tests** (2 classes): Specialized testing scenarios
- **Testing Framework**: JUnit 5, REST Assured, MockMvc, AssertJ
- **Database**: H2 in-memory for all tests with proper isolation

> 📖 For detailed testing documentation, see [TESTING_GUIDE.md](TESTING_GUIDE.md)

## Security Features

### What's Secure:
- **Environment Variables**: All credentials use environment variables
- **Git Safety**: `.env` files are automatically ignored by git
- **Template Files**: Only placeholder values in version control
- **Multiple Options**: Several secure credential management approaches

### Security Best Practices:
- Never put real passwords in `application.properties`
- Use different passwords for different environments
- Don't use root user in production
- Set secure file permissions: `chmod 600 .env`

## Learning Features

This project demonstrates comprehensive Java enterprise patterns:

### JPA/Hibernate Concepts
- **Entity Relationships**: @OneToMany, @ManyToOne implemented across Student-Enrollment-Course-Department
- **Advanced Mapping**: Guardian entity with embedded relationships
- **Custom Repository Methods**: Complex JPQL queries and method naming conventions
- **Database Migrations**: Hibernate DDL with auto-update strategy
- **Lazy Loading**: Performance optimization with FetchType.LAZY

### Spring Boot Features:
- Profile-based configuration
- Environment variable injection
- Auto-configuration
- Testing with H2

### Database Operations:
- CRUD operations
- Custom finder methods
- Transaction management
- Connection pooling

## Development Tools

### Database Setup:
```bash
# Interactive database setup
./data/setup_database.sh

# Automated setup with environment files
./data/setup_auto.sh

# Configuration-based setup
./data/setup_config.sh
```

### Environment Management:
```bash
# Interactive environment setup
./setup_env.sh

# Load existing environment
source .env
```

## Available Profiles

- `default`: H2 in-memory database (testing)
- `mysql`: MySQL database (development/production)
- `test`: H2 with test-specific configuration

## Example Usage

```bash
# Development with MySQL
SPRING_PROFILES_ACTIVE=mysql DB_PASSWORD=your_password ./mvnw spring-boot:run

# Testing with H2
SPRING_PROFILES_ACTIVE=test ./mvnw spring-boot:run

# Production (use external configuration)
java -jar target/spring-data-jpa-0.0.1-SNAPSHOT.jar --spring.config.location=classpath:/application.properties,/etc/myapp/application.properties
```

## API Documentation

This project includes comprehensive REST API documentation powered by **Swagger/OpenAPI 3**:

### Interactive Documentation
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **OpenAPI YAML**: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

### API Endpoints
- **Students API**: `/api/students` - Comprehensive student management with search, stats, batch operations, and enrollment
- **Courses API**: `/api/courses` - Course catalog management with department relationships
- **Departments API**: `/api/departments` - Department hierarchy and statistics
- **Enrollments API**: `/api/enrollments` - Student-course enrollment management with grading

### Features
- **Interactive Testing**: Test endpoints directly from the browser
- **Comprehensive Documentation**: Detailed parameter descriptions and examples
- **Schema Validation**: Request/response model documentation
- **Error Handling**: HTTP status codes and error response formats

> For detailed implementation information, see [SWAGGER_IMPLEMENTATION.md](SWAGGER_IMPLEMENTATION.md)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Ensure tests pass: `./mvnw test`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Remember**: This is a learning project. In production, use proper secrets management solutions like:
- Spring Cloud Config
- HashiCorp Vault
- Kubernetes Secrets
- Cloud provider secret managers (AWS Secrets Manager, Azure Key Vault, etc.)
