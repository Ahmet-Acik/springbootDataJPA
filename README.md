# Spring Boot Data JPA Learning Project

A comprehensive Spring Boot application demonstrating JPA/Hibernate features with secure credential management.

## 🚀 Quick Start

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

## 🗄️ Database Configuration

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

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/example/springdatajpa/
│   │   ├── SpringDataJpaApplication.java
│   │   ├── entity/           # JPA entities
│   │   ├── repository/       # Spring Data repositories
│   │   └── service/          # Business logic services
│   └── resources/
│       ├── application.properties
│       ├── application-mysql.properties
│       └── application-local.properties.example
├── test/                     # Unit and integration tests
data/
├── setup_database.sh        # Database initialization
├── sql/                     # SQL scripts
└── README.md               # Database setup guide
```

## 🧪 Running Tests

This project includes **127 comprehensive tests** covering unit, integration, and end-to-end scenarios.

```bash
# Run all tests (unit + integration) - RECOMMENDED
./mvnw clean verify

# Run only unit tests (89 repository tests)
./mvnw clean test

# Run only integration tests (38 tests)
./mvnw clean verify -P integration-tests -DskipTests=true

# Run tests with coverage report
./mvnw clean verify jacoco:report

# Run tests quietly (less output)
./mvnw clean verify -q

# Run specific test class
./mvnw test -Dtest=StudentRepositoryTest

# Run specific integration test
./mvnw verify -Dit.test=EndToEndIntegrationTest

# Run specific test method
./mvnw test -Dtest=StudentRepositoryTest#shouldFindByEmail
```

### 📊 Test Coverage
- **89 Unit Tests**: Repository layer testing with `@DataJpaTest`
- **38 Integration Tests**: Service, Web, and End-to-End testing
- **Test Categories**: CRUD operations, custom queries, error handling, performance, security
- **Coverage Report**: Available at `target/site/jacoco/index.html`

> 📖 For detailed testing documentation, see [TESTING_GUIDE.md](TESTING_GUIDE.md)

## 🔒 Security Features

### ✅ What's Secure:
- **Environment Variables**: All credentials use environment variables
- **Git Safety**: `.env` files are automatically ignored by git
- **Template Files**: Only placeholder values in version control
- **Multiple Options**: Several secure credential management approaches

### ⚠️ Security Best Practices:
- Never put real passwords in `application.properties`
- Use different passwords for different environments
- Don't use root user in production
- Set secure file permissions: `chmod 600 .env`

## 📚 Learning Features

This project demonstrates:

### JPA/Hibernate Concepts:
- Entity relationships (One-to-One, One-to-Many, Many-to-Many)
- Custom repository methods
- JPQL and native queries
- Database migrations with Hibernate DDL

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

## 🛠️ Development Tools

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

## 📋 Available Profiles

- `default`: H2 in-memory database (testing)
- `mysql`: MySQL database (development/production)
- `test`: H2 with test-specific configuration

## 🎯 Example Usage

```bash
# Development with MySQL
SPRING_PROFILES_ACTIVE=mysql DB_PASSWORD=your_password ./mvnw spring-boot:run

# Testing with H2
SPRING_PROFILES_ACTIVE=test ./mvnw spring-boot:run

# Production (use external configuration)
java -jar target/spring-data-jpa-0.0.1-SNAPSHOT.jar --spring.config.location=classpath:/application.properties,/etc/myapp/application.properties
```

## 📚 API Documentation

This project includes comprehensive REST API documentation powered by **Swagger/OpenAPI 3**:

### 🌐 Interactive Documentation
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **OpenAPI YAML**: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

### 🎯 API Endpoints
- **Students API**: `/api/students` - Student management operations
- **Courses API**: `/api/courses` - Course management and search
- **Departments API**: `/api/departments` - Department CRUD and statistics  
- **Enrollments API**: `/api/enrollments` - Student enrollment management

### ✨ Features
- **Interactive Testing**: Test endpoints directly from the browser
- **Comprehensive Documentation**: Detailed parameter descriptions and examples
- **Schema Validation**: Request/response model documentation
- **Error Handling**: HTTP status codes and error response formats

> 📖 For detailed implementation information, see [SWAGGER_IMPLEMENTATION.md](SWAGGER_IMPLEMENTATION.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Ensure tests pass: `./mvnw test`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**⚠️ Remember**: This is a learning project. In production, use proper secrets management solutions like:
- Spring Cloud Config
- HashiCorp Vault
- Kubernetes Secrets
- Cloud provider secret managers (AWS Secrets Manager, Azure Key Vault, etc.)
