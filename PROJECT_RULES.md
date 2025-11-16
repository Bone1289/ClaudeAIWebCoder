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

### OWASP API Security Top 10 (2023)
**ALWAYS check for these vulnerabilities in every API endpoint:**

1. **Broken Object Level Authorization (BOLA)** - 40% of all API attacks
   - APIs fail to verify if users should have access to specific objects
   - Always verify user has access before returning or modifying data
   - Check object ownership in every endpoint that accesses resources by ID

2. **Broken Authentication**
   - Authentication mechanisms implemented incorrectly
   - Avoid weak password policies, missing auth on sensitive endpoints
   - Protect JWT tokens, implement proper session management

3. **Broken Object Property Level Authorization**
   - APIs expose sensitive object properties unauthorized users can read/modify
   - Implement property-level access controls
   - Avoid mass assignment vulnerabilities

4. **Unrestricted Resource Consumption**
   - APIs don't limit requests properly (DoS, resource depletion)
   - Implement rate limiting on all endpoints
   - Set max page sizes, request timeouts, payload limits

5. **Broken Function Level Authorization**
   - Complex access control fails to separate admin vs regular functions
   - Always verify function-level permissions
   - Don't rely on HTTP method alone for authorization

6. **Unrestricted Access to Sensitive Business Flows** (NEW 2023)
   - Business logic abuse (bulk operations, automated account creation)
   - Implement proper rate limiting on sensitive operations
   - Detect and prevent automated abuse

7. **Server Side Request Forgery (SSRF)** (NEW 2023)
   - APIs fetch remote resources without validating URIs
   - Validate all user-supplied URIs
   - Use allowlists for external services

8. **Security Misconfiguration**
   - Verbose error messages, unpatched systems, exposed debugging
   - Never expose stack traces to clients
   - Keep dependencies updated

9. **Improper Inventory Management**
   - Lack of proper API inventory, outdated versions running
   - Document all API endpoints
   - Deprecate and remove old API versions

10. **Unsafe Consumption of APIs** (NEW 2023)
    - Trusting third-party API data without validation
    - Validate all external API responses
    - Apply same security standards to third-party data

### Common Vulnerabilities to Avoid
- SQL Injection
- Cross-Site Scripting (XSS)
- Command Injection
- Insecure Direct Object References
- Missing authentication/authorization checks
- Sensitive data exposure
