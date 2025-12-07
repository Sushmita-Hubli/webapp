#!/bin/bash

# Exit on any error
set -e

echo "=========================================="
echo "Starting Application Setup"
echo "=========================================="

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root (use sudo)"
   exit 1
fi

# 1. Update Package Lists
echo "Step 1: Updating package lists..."
apt update

# 2. Upgrade System Packages
echo "Step 2: Upgrading system packages..."
apt upgrade -y

# 3. Install MySQL Server
echo "Step 3: Installing MySQL Server..."
apt install -y mysql-server

# Start and enable MySQL service
systemctl start mysql
systemctl enable mysql

echo "MySQL installed and started successfully"

# 4. Install Java 17 (Required for Spring Boot)
echo "Step 4: Installing Java 17..."
apt install -y openjdk-17-jdk

# Verify Java installation
java -version

# 5. Install Maven (for building the application)
echo "Step 5: Installing Maven..."
apt install -y maven

# Verify Maven installation
mvn -version

# 6. Create Application Database
echo "Step 6: Creating application database..."

# Create database and user
mysql -u root <<MYSQL_SCRIPT
CREATE DATABASE IF NOT EXISTS webapp_db;
CREATE USER IF NOT EXISTS 'webappuser'@'localhost' IDENTIFIED BY 'webapppassword';
GRANT ALL PRIVILEGES ON webapp_db.* TO 'webappuser'@'localhost';
FLUSH PRIVILEGES;
MYSQL_SCRIPT

echo "Database 'webapp_db' created successfully"

# 7. Create Application Linux Group
echo "Step 7: Creating application group..."
groupadd -f csye6225

# 8. Create Application User Account
echo "Step 8: Creating application user..."
# -r: system account
# -g: primary group
# -s: shell (nologin for security)
# -m: create home directory
useradd -r -g csye6225 -s /usr/sbin/nologin -m csye6225 || echo "User csye6225 already exists"

# 9. Deploy Application Files
echo "Step 9: Creating application directory..."
mkdir -p /opt/csye6225

# Note: The actual application JAR file should be copied here
# This script assumes you'll copy your application files separately
# Example: If you have a webapp.jar file, copy it to /opt/csye6225/

echo "Application directory created at /opt/csye6225/"
echo "Please copy your application JAR file to this directory"

# 10. Set File Permissions
echo "Step 10: Setting file permissions..."
chown -R csye6225:csye6225 /opt/csye6225
chmod -R 750 /opt/csye6225

echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "Summary:"
echo "- MySQL Server: Installed and running"
echo "- Database: webapp_db created"
echo "- Database User: webappuser (password: webapppassword)"
echo "- Java 17: Installed"
echo "- Maven: Installed"
echo "- Application Group: csye6225"
echo "- Application User: csye6225"
echo "- Application Directory: /opt/csye6225"
echo ""
echo "Next Steps:"
echo "1. Copy your Spring Boot JAR file to /opt/csye6225/"
echo "2. Update application.properties with database credentials"
echo "3. Run your application as the csye6225 user"
echo ""
