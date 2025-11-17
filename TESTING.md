# Testing Guide

This document provides comprehensive information about the test suite for the ClaudeAIWebCoder banking application.

## Overview

The project includes both backend (Spring Boot) and frontend (Angular) tests covering:
- Unit tests for domain entities
- Unit tests for services
- Integration tests for API endpoints
- Frontend component and service tests

## Backend Tests

### Test Structure

```
backend/src/test/java/com/example/demo/
├── domain/
│   ├── AccountTest.java
│   └── TransactionCategoryTest.java
├── application/service/
│   └── BankingServiceTest.java
└── adapter/in/web/banking/
    └── BankingControllerIntegrationTest.java
```

### Running Backend Tests

```bash
cd backend
./gradlew test
```

To run with verbose output:
```bash
./gradlew test --info
```

To run a specific test class:
```bash
./gradlew test --tests AccountTest
```

### Backend Test Coverage

#### Domain Layer Tests
- **AccountTest.java**: 60+ test cases covering:
  - Account creation with various validation scenarios
  - Deposit and withdrawal operations
  - Account status changes (suspend, close)
  - Account type updates
  - Account reconstitution from persistence

- **TransactionCategoryTest.java**: 40+ test cases covering:
  - Category creation and validation
  - Category updates
  - Activation/deactivation
  - Immutability verification
  - Category reconstitution

#### Service Layer Tests
- **BankingServiceTest.java**: 30+ test cases covering:
  - Account CRUD operations
  - Transaction operations (deposit, withdraw)
  - Error handling
  - Business rule validation
  - Mock repository interactions

#### Integration Tests
- **BankingControllerIntegrationTest.java**: 15+ test cases covering:
  - Full HTTP request/response cycle
  - API endpoint testing
  - Status code verification
  - End-to-end account operations
  - Error response handling

### Test Configuration

The backend uses:
- **JUnit 5** (Jupiter) for test framework
- **Mockito** for mocking dependencies
- **Spring Boot Test** for integration tests
- **H2 Database** for in-memory testing
- **MockMvc** for REST API testing

Test configuration is in `application-test.properties`:
- Uses H2 in-memory database
- Auto-creates schema on startup
- Enables SQL logging for debugging

## Frontend Tests

### Test Structure

```
frontend/src/app/
├── services/
│   ├── banking.service.spec.ts
│   └── category.service.spec.ts
└── components/banking/dashboard/
    └── banking-dashboard.component.spec.ts
```

### Running Frontend Tests

```bash
cd frontend
npm test
```

To run tests in headless mode (CI/CD):
```bash
npm test -- --watch=false --browsers=ChromeHeadless
```

To run with coverage:
```bash
npm test -- --code-coverage
```

To run a specific test file:
```bash
npm test -- --include='**/*banking.service.spec.ts'
```

### Frontend Test Coverage

#### Service Tests
- **banking.service.spec.ts**: 15+ test cases covering:
  - Account CRUD operations
  - Transaction operations (deposit, withdraw, transfer)
  - HTTP error handling
  - Network error handling
  - Request/response verification

- **category.service.spec.ts**: 20+ test cases covering:
  - Category loading and filtering
  - Category CRUD operations
  - Type-based filtering (INCOME/EXPENSE)
  - Cache management
  - HTTP request verification

#### Component Tests
- **banking-dashboard.component.spec.ts**: 30+ test cases covering:
  - Component initialization
  - Account loading and display
  - Modal operations (open/close)
  - Create/Update/Delete operations
  - Nationality autocomplete functionality
  - Helper methods and calculations
  - Error handling and success messages

### Test Configuration

The frontend uses:
- **Jasmine** for test framework
- **Karma** for test runner
- **HttpClientTestingModule** for HTTP testing
- **ComponentFixture** for component testing

## Initial Data Seeding

### Data Initializer

The application includes a `DataInitializer` component that seeds the database with initial data:

**Location**: `backend/src/main/java/com/example/demo/config/DataInitializer.java`

**Features**:
- Only runs in `dev` and `test` profiles
- Creates 12 transaction categories (4 income, 8 expense)
- Creates 5 sample accounts with initial balances
- Prevents duplicate data creation

**Categories Created**:

Income Categories:
- SALARY - Monthly salary and wages
- FREELANCE - Freelance and consulting income
- INVESTMENT - Investment returns and dividends
- BONUS - Performance bonuses and rewards

Expense Categories:
- GROCERIES - Food and household supplies
- UTILITIES - Electricity, water, gas, internet
- RENT - Housing rent or mortgage
- TRANSPORTATION - Public transport, fuel, car maintenance
- ENTERTAINMENT - Movies, dining, hobbies
- HEALTHCARE - Medical expenses and insurance
- EDUCATION - Courses, books, training
- SHOPPING - Clothing, electronics, general shopping

**Sample Accounts**:
1. John Doe (USA) - CHECKING - $5,000
2. Jane Smith (UK) - SAVINGS - $10,000
3. Carlos Rodriguez (Spain) - CHECKING - $3,500.50
4. Yuki Tanaka (Japan) - SAVINGS - $7,500
5. Emma Johnson (Canada) - CREDIT - $2,000

### Enabling Data Initialization

Set the active profile to `dev` or `test` in `application.properties`:
```properties
spring.profiles.active=dev
```

Or use environment variable:
```bash
export SPRING_PROFILES_ACTIVE=dev
```

Or run with command line argument:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Continuous Integration

### GitHub Actions Example

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run backend tests
        run: cd backend && ./gradlew test

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '18'
      - name: Install dependencies
        run: cd frontend && npm install
      - name: Run frontend tests
        run: cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
```

## Test Best Practices

### Backend
1. **Unit tests** should be fast and isolated
2. **Use mocks** for external dependencies
3. **Integration tests** should test the full stack
4. **Follow AAA pattern**: Arrange, Act, Assert
5. **Use descriptive test names** with @DisplayName

### Frontend
1. **Test user interactions**, not implementation details
2. **Mock HTTP calls** using HttpClientTestingModule
3. **Test component behavior**, not template rendering
4. **Clean up** after each test with afterEach
5. **Use descriptive test names** with 'it' or 'should'

## Coverage Goals

- **Backend**: Aim for >80% code coverage
- **Frontend**: Aim for >70% code coverage
- **Critical paths**: 100% coverage for core business logic

## Troubleshooting

### Backend Tests Fail with Database Errors
- Ensure H2 dependency is in build.gradle
- Check application-test.properties configuration
- Verify @Transactional annotation on test class

### Frontend Tests Fail with Module Errors
- Run `npm install` to ensure all dependencies are installed
- Check that imports match actual file locations
- Verify Angular version compatibility

### Tests Timeout
- Increase timeout in karma.conf.js (frontend)
- Use `@Timeout` annotation (backend)
- Check for infinite loops or unresolved observables

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Angular Testing Guide](https://angular.io/guide/testing)
- [Jasmine Documentation](https://jasmine.github.io/)
