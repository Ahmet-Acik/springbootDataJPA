#!/bin/bash

# Environment Variables Setup Script
# This script helps you set up secure environment variables for the Spring Boot application

set -e

echo "🔧 Spring Boot Environment Variables Setup"
echo "=========================================="
echo ""

# Check if .env already exists
if [ -f ".env" ]; then
    echo "⚠️  .env file already exists!"
    read -p "Do you want to overwrite it? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "ℹ️  Setup cancelled. Your existing .env file is unchanged."
        exit 0
    fi
fi

echo "📋 Setting up your .env file..."
echo ""

# Copy template
cp .env.example .env
echo "✅ Created .env file from template"

# Get database choice
echo "🗄️  Database Configuration:"
echo "1) MySQL (recommended for development/production)"
echo "2) H2 (in-memory, good for testing)"
echo ""
read -p "Choose database type (1 or 2): " db_choice

if [ "$db_choice" = "1" ]; then
    echo ""
    echo "🔐 MySQL Configuration:"
    read -p "Database host (default: localhost): " db_host
    db_host=${db_host:-localhost}
    
    read -p "Database port (default: 3306): " db_port
    db_port=${db_port:-3306}
    
    read -p "Database name (default: schooldb): " db_name
    db_name=${db_name:-schooldb}
    
    read -p "Database username (default: root): " db_user
    db_user=${db_user:-root}
    
    echo ""
    echo "🔒 Please enter your MySQL password:"
    read -s -p "Password: " db_password
    echo ""
    
    # Update .env file with MySQL settings
    cat > .env << EOF
# MySQL Database Configuration
DB_URL=jdbc:mysql://${db_host}:${db_port}/${db_name}
DB_USERNAME=${db_user}
DB_PASSWORD=${db_password}
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_DIALECT=org.hibernate.dialect.MySQLDialect

# Spring Profile
SPRING_PROFILES_ACTIVE=mysql
EOF

    echo "✅ MySQL configuration saved to .env"
    
elif [ "$db_choice" = "2" ]; then
    # Update .env file with H2 settings
    cat > .env << EOF
# H2 Database Configuration (In-Memory)
DB_URL=jdbc:h2:mem:testdb
DB_USERNAME=sa
DB_PASSWORD=
DB_DRIVER=org.h2.Driver
DB_DIALECT=org.hibernate.dialect.H2Dialect

# Spring Profile
SPRING_PROFILES_ACTIVE=test
EOF

    echo "✅ H2 configuration saved to .env"
else
    echo "❌ Invalid choice. Please run the script again."
    rm -f .env
    exit 1
fi

echo ""
echo "🎉 Environment setup complete!"
echo ""
echo "📝 Next steps:"
echo "1. Load environment variables: source .env"
echo "2. Run the application: ./mvnw spring-boot:run"
echo ""
echo "🔒 Security reminders:"
echo "- Your .env file is automatically ignored by git"
echo "- Never commit real passwords to version control"
echo "- Use different passwords for different environments"
echo ""

# Set appropriate permissions
chmod 600 .env
echo "🔐 Set secure permissions on .env file (600)"