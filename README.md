Web Application - Cloud Native REST API
A cloud-native web application built with Spring Boot that provides RESTful APIs for user and product management.

Prerequisites
Before running this application, ensure you have the following installed:

Required Software:
Java Development Kit (JDK): Version 17 or 21

Download from: https://www.oracle.com/java/technologies/downloads/
Verify installation: java -version
Maven: Version 3.6 or higher

Download from: https://maven.apache.org/download.cgi
Verify installation: mvn -version
MySQL: Version 8.0 or higher

Download from: https://dev.mysql.com/downloads/mysql/
Verify installation: mysql --version
IDE (Optional but recommended):

IntelliJ IDEA, Eclipse, or VS Code
Database Setup
1. Start MySQL Server
   Make sure MySQL server is running on your system.

2. Create Database and User
   Connect to MySQL and run these commands:

CREATE DATABASE webapp_db;

CREATE USER 'webapp_user'@'localhost' IDENTIFIED BY 'webapp_pass';

GRANT ALL PRIVILEGES ON webapp_db.* TO 'webapp_user'@'localhost';

FLUSH PRIVILEGES;
3. Verify Database
   SHOW DATABASES;
   USE webapp_db;
   Build Instructions

1. Clone the Repository
   git clone <repository-url>
   cd webapp
2. Configure Database Connection
   Open src/main/resources/application.properties and update if needed:

spring.datasource.url=jdbc:mysql://localhost:3306/webapp_db
spring.datasource.username=webapp_user
spring.datasource.password=webapp_pass
3. Build the Application
   mvn clean install
   This will:

Download all dependencies
Compile the code
Run tests
Create a JAR file in target/ directory
Deploy Instructions
Method 1: Run with Maven
mvn spring-boot:run
Method 2: Run the JAR file
java -jar target/webapp-0.0.1-SNAPSHOT.jar
Method 3: Run from IDE
Open project in IntelliJ IDEA
Navigate to src/main/java/com/example/webapp/WebappApplication.java
Right-click → Run 'WebappApplication'
Verify Application is Running
The application will start on port 8080.

Check Status:
Open browser or use curl:

curl http://localhost:8080/v1/user/health
Expected response: "User API is running"

API Endpoints

User Endpoints
Method	Endpoint	Auth Required	Description
POST	/v1/user	No	Create new user
GET	/v1/user/self	Yes	Get user info
PUT	/v1/user/self	Yes	Update user info

Product Endpoints
Method	Endpoint	Auth Required	Description
POST	/v1/product	Yes	Create product
GET	/v1/product/{id}	Yes	Get product by ID
GET	/v1/product	Yes	Get all products
PUT	/v1/product/{id}	Yes (Owner)	Update product
PATCH	/v1/product/{id}	Yes (Owner)	Update product
DELETE	/v1/product/{id}	Yes (Owner)	Delete product
Testing with Postman
1. Create User
   POST http://localhost:8080/v1/user
   Content-Type: application/json

{
"email": "test@example.com",
"password": "Test123",
"firstName": "Test",
"lastName": "User"
}
2. Get User Info (with Authentication)
   GET http://localhost:8080/v1/user/self
   Authorization: Basic Auth
   Username: test@example.com
   Password: Test123
3. Create Product (with Authentication)
   POST http://localhost:8080/v1/product
   Authorization: Basic Auth
   Username: test@example.com
   Password: Test123
   Content-Type: application/json

{
"name": "Laptop",
"description": "Gaming laptop",
"sku": "LAP-001",
"manufacturer": "Dell",
"quantity": 50
}
Technologies Used
Java 17/21
Spring Boot 3.2.0
Spring Security (Basic Authentication)
Spring Data JPA (Database access)
MySQL 8.0 (Database)
BCrypt (Password hashing)
Maven (Build tool)
Lombok (Reduce boilerplate code)
Database Schema
Users Table
CREATE TABLE users (
id VARCHAR(36) PRIMARY KEY,
email VARCHAR(255) UNIQUE NOT NULL,
password VARCHAR(255) NOT NULL,
first_name VARCHAR(255) NOT NULL,
last_name VARCHAR(255) NOT NULL,
account_created DATETIME NOT NULL,
account_updated DATETIME NOT NULL
);
Products Table
CREATE TABLE products (
id VARCHAR(36) PRIMARY KEY,
name VARCHAR(255) NOT NULL,
description VARCHAR(1000),
sku VARCHAR(255) UNIQUE NOT NULL,
manufacturer VARCHAR(255) NOT NULL,
quantity INT NOT NULL,
date_added DATETIME NOT NULL,
date_last_updated DATETIME NOT NULL,
owner_user_id VARCHAR(36) NOT NULL,
FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

Troubleshooting
Application won't start
Error: Port 8080 already in use
Solution: Stop other applications using port 8080 or change port in application.properties
Error: Cannot connect to database
Solution: Verify MySQL is running and credentials are correct
Build fails
Error: Tests failing
Solution: Make sure MySQL is running and database is created

Author
Sushmita Hubli


