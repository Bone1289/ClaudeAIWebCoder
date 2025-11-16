# Virtual Bank Application - Local Setup Guide

This guide will help you set up and run the Virtual Bank application on your local machine with MySQL database.

## Prerequisites

### Required Software
- **Java 21** (JDK 21 LTS)
- **Node.js** (v18 or higher) and npm
- **MySQL** (v8.0 or higher)
- **Git**

### Verify Installation
```bash
java -version    # Should show Java 21
node -version    # Should show v18+
npm -version     # Should show 9+
mysql --version  # Should show MySQL 8.0+
```

---

## Database Setup

### Option 1: Automatic Database Creation (Recommended)
The application will automatically create the database when you run it for the first time.

Just make sure MySQL is running:
```bash
# On macOS (using Homebrew)
brew services start mysql

# On Linux (systemd)
sudo systemctl start mysql

# On Windows
# Start MySQL from Services or MySQL Workbench
```

### Option 2: Manual Database Creation
```bash
# Login to MySQL
mysql -u root -p

# Run the initialization script
source database/init-database.sql

# Exit MySQL
exit
```

### Configure Database Credentials
The default configuration uses:
- **Database**: `virtualbank`
- **Username**: `root`
- **Password**: `root`

To change these, edit: `backend/src/main/resources/application-local.properties`

---

## Backend Setup

### 1. Navigate to Backend Directory
```bash
cd backend
```

### 2. Build the Project
```bash
# On macOS/Linux
./gradlew clean build

# On Windows
gradlew.bat clean build
```

### 3. Run the Backend
```bash
# Using Gradle with local profile (MySQL)
./gradlew bootRun --args='--spring.profiles.active=local'

# OR using dev profile (H2 in-memory database - no MySQL needed)
./gradlew bootRun --args='--spring.profiles.active=dev'

# OR using the built JAR
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### 4. Verify Backend is Running
- API Base URL: http://localhost:8080/api
- Health Check: http://localhost:8080/actuator/health
- H2 Console (dev profile only): http://localhost:8080/h2-console

You should see:
```json
{
  "status": "UP"
}
```

---

## Frontend Setup

### 1. Navigate to Frontend Directory
```bash
cd frontend
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Run the Frontend
```bash
# Development server
npm start

# The app will automatically open at http://localhost:4200
```

### 4. Build for Production (Optional)
```bash
npm run build
```

---

## Running Full Stack Application

### Option 1: Two Terminal Windows

**Terminal 1 - Backend:**
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm start
```

### Option 2: Using npm-run-all (if configured)
```bash
npm run start:all
```

---

## Application Profiles

The application supports multiple profiles:

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` | H2 (in-memory) | Quick development, no MySQL needed |
| `local` | MySQL | Local development with persistent data |
| `prod` | MySQL | Production deployment |

To switch profiles, modify `backend/src/main/resources/application.properties`:
```properties
spring.profiles.active=local
```

Or pass as argument:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## Accessing the Application

### Frontend
- **URL**: http://localhost:4200
- **Routes**:
  - `/` - Home page
  - `/demo` - API demo
  - `/users` - User management
  - `/banking/dashboard` - Banking dashboard

### Backend API
- **Base URL**: http://localhost:8080/api
- **Swagger/API Docs**: (if configured) http://localhost:8080/swagger-ui.html

### Key API Endpoints
```
GET    /api/health                           # Health check
GET    /api/hello?name=John                  # Hello endpoint

# User Management
GET    /api/users                            # List all users
POST   /api/users                            # Create user
GET    /api/users/{id}                       # Get user by ID
PUT    /api/users/{id}                       # Update user
DELETE /api/users/{id}                       # Delete user

# Banking
GET    /api/banking/accounts                 # List all accounts
POST   /api/banking/accounts                 # Create account
GET    /api/banking/accounts/{id}            # Get account details
POST   /api/banking/accounts/{id}/deposit    # Deposit money
POST   /api/banking/accounts/{id}/withdraw   # Withdraw money
POST   /api/banking/accounts/{id}/transfer   # Transfer money
GET    /api/banking/accounts/{id}/transactions # Get transaction history
GET    /api/banking/accounts/{id}/statement  # Get account statement
GET    /api/banking/accounts/{id}/category-report # Get category report

# Categories
GET    /api/categories                       # List all categories
POST   /api/categories                       # Create category
GET    /api/categories/{id}                  # Get category by ID
PUT    /api/categories/{id}                  # Update category
DELETE /api/categories/{id}                  # Delete category
PATCH  /api/categories/{id}/deactivate       # Deactivate category
```

---

## Sample Data

The application automatically seeds transaction categories on startup via `CategoryDataSeeder.java`.

Default categories include:
- **Income**: SALARY, FREELANCE, INVESTMENT, GIFT, OTHER_INCOME
- **Expense**: GROCERIES, UTILITIES, RENT, TRANSPORT, ENTERTAINMENT, HEALTHCARE, EDUCATION, SHOPPING, OTHER_EXPENSE

To add more sample data (users, accounts, transactions):
```bash
mysql -u root -p virtualbank < database/seed-data.sql
```

---

## Troubleshooting

### Backend Issues

**Problem: Port 8080 already in use**
```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill the process or change server.port in application.properties
```

**Problem: MySQL connection refused**
```bash
# Check MySQL is running
brew services list  # macOS
sudo systemctl status mysql  # Linux

# Start MySQL
brew services start mysql  # macOS
sudo systemctl start mysql  # Linux
```

**Problem: Access denied for user 'root'@'localhost'**
- Update password in `application-local.properties`
- Or reset MySQL root password

**Problem: Compilation errors**
```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies
```

### Frontend Issues

**Problem: npm install fails**
```bash
# Clear cache and retry
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

**Problem: Port 4200 already in use**
```bash
# Kill process on port 4200
lsof -i :4200  # macOS/Linux
kill -9 <PID>

# Or change port in angular.json
```

**Problem: API calls fail (CORS errors)**
- Make sure backend is running on port 8080
- Check proxy configuration in `frontend/proxy.conf.json` (if exists)

---

## Development Tips

### Hot Reload
- **Backend**: Use Spring DevTools (already included) for automatic restart
- **Frontend**: Angular CLI automatically reloads on file changes

### Database Management

**View Database:**
```bash
mysql -u root -p
USE virtualbank;
SHOW TABLES;
SELECT * FROM user;
SELECT * FROM account;
SELECT * FROM transaction;
SELECT * FROM transaction_category;
```

**Reset Database:**
```bash
# Drop and recreate
mysql -u root -p -e "DROP DATABASE virtualbank; CREATE DATABASE virtualbank;"

# Restart backend (tables will be recreated)
```

### Logging
- Backend logs: Console output
- SQL queries: Enabled by default in dev/local profiles
- Change log levels in `application.properties`

---

## Testing

### Backend Tests
```bash
cd backend
./gradlew test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### Integration Tests
```bash
cd backend
./gradlew integrationTest  # if configured
```

---

## Building for Production

### Backend
```bash
cd backend
./gradlew clean build -Pprod
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Frontend
```bash
cd frontend
npm run build
# Output in frontend/dist/
```

---

## Additional Resources

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **Angular Documentation**: https://angular.io/docs
- **MySQL Documentation**: https://dev.mysql.com/doc/

---

## Need Help?

- Check the logs in the terminal
- Review error messages carefully
- Ensure all prerequisites are installed
- Verify database credentials
- Make sure both backend (8080) and frontend (4200) ports are available

---

**Happy Coding! 🚀**
