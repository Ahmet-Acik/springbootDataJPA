#!/bin/bash

# Database Setup Using MySQL Configuration File
# This approach uses a MySQL config file for credentials

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MYSQL_CONFIG="$SCRIPT_DIR/mysql/my.cnf"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_status() { echo -e "${GREEN}[INFO]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }

echo "============================================"
echo "🔐 MySQL Config File Database Setup"
echo "============================================"

# Check if MySQL config file exists
if [ ! -f "$MYSQL_CONFIG" ]; then
    print_error "MySQL configuration file not found: $MYSQL_CONFIG"
    print_status "Please create it from the example:"
    echo "    cp data/mysql/my.cnf.example data/mysql/my.cnf"
    echo "    # Edit data/mysql/my.cnf with your credentials"
    exit 1
fi

print_status "Using MySQL configuration file: $MYSQL_CONFIG"

# Test connection
print_status "Testing MySQL connection..."
if ! mysql --defaults-extra-file="$MYSQL_CONFIG" -e "SELECT 1;" &> /dev/null; then
    print_error "Cannot connect to MySQL using config file"
    print_warning "Please check credentials in $MYSQL_CONFIG"
    exit 1
fi

print_status "✓ MySQL connection successful!"

# Run SQL scripts
print_status "Running database setup scripts..."

for sql_file in "$SCRIPT_DIR/sql"/*.sql; do
    if [ -f "$sql_file" ]; then
        print_status "Executing $(basename "$sql_file")..."
        if mysql --defaults-extra-file="$MYSQL_CONFIG" < "$sql_file"; then
            print_status "✓ $(basename "$sql_file") executed successfully"
        else
            print_error "✗ Failed to execute $(basename "$sql_file")"
            exit 1
        fi
    fi
done

print_status "🎉 Database setup completed successfully!"
echo "============================================"