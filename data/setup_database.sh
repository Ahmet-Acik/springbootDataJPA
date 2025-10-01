#!/bin/bash

# Database Setup Script for School Management System
# This script sets up the MySQL database for the Spring Boot JPA application

echo "==================================="
echo "School Management System DB Setup"
echo "==================================="

# Configuration
DB_NAME="schooldb"
DB_USER="root"
DB_HOST="localhost"
DB_PORT="3306"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if MySQL is running
print_status "Checking MySQL service..."
if ! command -v mysql &> /dev/null; then
    print_error "MySQL client not found. Please install MySQL."
    exit 1
fi

# Test MySQL connection
print_status "Testing MySQL connection..."
if ! mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p -e "SELECT 1;" &> /dev/null; then
    print_error "Cannot connect to MySQL. Please check your connection settings."
    print_warning "Make sure MySQL is running and credentials are correct."
    exit 1
fi

print_status "MySQL connection successful!"

# Create database and run initialization scripts
print_status "Creating database and running initialization scripts..."

echo 
echo "Please enter MySQL password for user '$DB_USER':"

# Run all SQL scripts in order
for sql_file in data/sql/*.sql; do
    if [ -f "$sql_file" ]; then
        print_status "Executing $(basename $sql_file)..."
        if mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p < "$sql_file"; then
            print_status "✓ $(basename $sql_file) executed successfully"
        else
            print_error "✗ Failed to execute $(basename $sql_file)"
            exit 1
        fi
    fi
done

echo
print_status "Database setup completed successfully!"
print_status "Database: $DB_NAME"
print_status "Host: $DB_HOST:$DB_PORT"
print_status "User: $DB_USER"

echo
print_status "You can now run the Spring Boot application with the 'mysql' profile:"
print_status "mvn spring-boot:run -Dspring-boot.run.profiles=mysql"
print_status "or"  
print_status "java -jar target/spring-data-jpa-0.0.1-SNAPSHOT.jar --spring.profiles.active=mysql"

echo
print_status "To view the data, you can run the analysis queries:"
print_status "mysql -u $DB_USER -p $DB_NAME < data/sql/03_analysis_queries.sql"

echo "==================================="