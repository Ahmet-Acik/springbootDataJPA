#!/bin/bash

# Demo Script: Programmatic Database Setup Options
# This script demonstrates all available ways to provide MySQL credentials programmatically

echo "🚀 Spring Boot JPA - Programmatic Database Setup Demo"
echo "====================================================="
echo

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_option() {
    echo -e "${BLUE}$1${NC}"
    echo -e "${GREEN}$2${NC}"
    echo
}

echo "Choose your preferred method for providing MySQL credentials:"
echo

print_option "🔧 Option 1: Environment Variables (.env file)" \
"   cp data/.env.example data/.env
   # Edit data/.env with your credentials
   ./data/setup_auto.sh"

print_option "🔧 Option 2: Command Line Environment Variables" \
"   DB_PASSWORD='your_password' ./data/setup_database.sh
   # or
   export DB_PASSWORD='your_password'
   ./data/setup_database.sh"

print_option "🔧 Option 3: MySQL Configuration File" \
"   cp data/mysql/my.cnf.example data/mysql/my.cnf
   # Edit data/mysql/my.cnf with your credentials
   ./data/setup_config.sh"

print_option "🔧 Option 4: Spring Boot Application Properties" \
"   # Edit src/main/resources/application-mysql.properties
   spring.datasource.password=your_password
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql"

print_option "🔧 Option 5: Java System Properties" \
"   ./mvnw spring-boot:run \\
     -Dspring-boot.run.profiles=mysql \\
     -Dspring.datasource.password=your_password"

print_option "🔧 Option 6: Docker Environment Variables" \
"   docker run -e DB_PASSWORD=your_password \\
     -e SPRING_PROFILES_ACTIVE=mysql \\
     your-app:latest"

echo -e "${YELLOW}Security Best Practices:${NC}"
echo "All credential files are in .gitignore"
echo "Use dedicated MySQL user (not root) in production"
echo "Use environment variables in production"
echo "Never commit passwords to version control"
echo "Use secrets management in cloud environments"
echo

echo "For quick setup, we recommend Option 1 (.env file approach)"
echo "Run: cp data/.env.example data/.env && ./data/setup_auto.sh"