# Project Rules and Guidelines

## Database & Entity Design

### Primary Keys
- **ALL entities MUST use UUID as the primary key**
- UUID generation strategy: `@GeneratedValue(strategy = GenerationType.UUID)`
- MySQL storage: `BINARY(16)` for optimal storage and performance
- Auto-generated and incremental (via UUID v7 or database generation)

### In-Memory Database
- **DO NOT use H2 or any in-memory database**
- Application requires MySQL database for all environments
- No H2 dependency in build.gradle

## Frontend Guidelines

### UUID Display
- **NEVER display UUIDs in the frontend UI**
- UUIDs should only be used internally for API calls and data identification
- Display user-friendly identifiers instead (e.g., account numbers, names, etc.)

## Application Focus

### Banking Application Only
- Application focuses exclusively on banking functionality
- User/Customer management is handled within account creation (First Name, Last Name, Nationality)
- No separate User entity or user management pages
- Default page: Banking Dashboard

### Removed Features
- Home page (removed)
- API Demo page (removed)
- Users CRUD page (removed)
- User entity and all related code (removed)

## Architecture

### Hexagonal Architecture (Ports and Adapters)
- Domain models are immutable
- Use factory methods for domain object creation
- Separate ports (interfaces) from adapters (implementations)
- Domain layer has no dependencies on infrastructure

### Database
- Production database: MySQL 8.0
- All JPA code must be database-agnostic
- Use standard JPA annotations only

## Security

### API Security
- Follow **OWASP API Security Top 10** guidelines
- Reference: https://www.isaca.org/resources/news-and-trends/industry-news/2023/reviewing-the-2023-owasp-api-top-10
- Key areas to address:
  - API1: Broken Object Level Authorization
  - API2: Broken Authentication
  - API3: Broken Object Property Level Authorization
  - API4: Unrestricted Resource Consumption
  - API5: Broken Function Level Authorization
  - API6: Unrestricted Access to Sensitive Business Flows
  - API7: Server Side Request Forgery
  - API8: Security Misconfiguration
  - API9: Improper Inventory Management
  - API10: Unsafe Consumption of APIs

### Common Vulnerabilities to Avoid
- SQL Injection
- Cross-Site Scripting (XSS)
- Command Injection
- Insecure Direct Object References
- Missing authentication/authorization checks
- Sensitive data exposure
