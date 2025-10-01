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

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=StudentRepositoryTest
```

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