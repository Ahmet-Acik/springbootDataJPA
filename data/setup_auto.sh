#!/bin/bash

# Automated Database Setup with Environment Variables
# This script loads credentials from .env file and runs the database setup

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

echo "========================================"
echo "🚀 Automated School Management DB Setup"
echo "========================================"

# Check if .env file exists
if [ -f "$ENV_FILE" ]; then
    print_status "Loading environment variables from .env file..."
    
    # Load environment variables from .env file
    set -a  # automatically export all variables
    source "$ENV_FILE"
    set +a  # stop automatically exporting
    
    print_status "✓ Environment variables loaded successfully"
else
    print_warning ".env file not found at $ENV_FILE"
    print_status "You can create one by copying .env.example:"
    echo "    cp data/.env.example data/.env"
    echo "    # Edit data/.env with your credentials"
    echo
    print_status "Falling back to interactive mode..."
fi

# Validate required environment variables
if [ -z "$DB_PASSWORD" ]; then
    print_warning "DB_PASSWORD not set in environment"
    print_status "Please set it in .env file or export it:"
    echo "    export DB_PASSWORD='your_password'"
    echo
fi

# Run the main setup script
print_step "Running database setup script..."
exec "$SCRIPT_DIR/setup_database.sh"