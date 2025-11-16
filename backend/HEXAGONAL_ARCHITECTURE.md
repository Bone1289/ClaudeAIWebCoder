# Hexagonal Architecture Implementation

This backend follows **Hexagonal Architecture** (also known as Ports and Adapters architecture), which provides a clean separation of concerns and makes the application more maintainable, testable, and flexible.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     External World                           │
│  (HTTP Requests, Databases, External Services, etc.)        │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┴───────────────┐
        │                              │
   ┌────▼────┐                    ┌────▼────┐
   │  Input  │                    │ Output  │
   │ Adapter │                    │ Adapter │
   │  (Web)  │                    │ (Persist)│
   └────┬────┘                    └────┬────┘
        │                              │
   ┌────▼────────────────────────────▼────┐
   │         Application Layer            │
   │   (Use Cases / Business Logic)       │
   │                                       │
   │   ┌─────────────────────────────┐   │
   │   │      Domain Layer           │   │
   │   │   (Business Entities)       │   │
   │   └─────────────────────────────┘   │
   └──────────────────────────────────────┘
```

## Package Structure

```
com.example.demo/
├── domain/                           # Core business logic (The Hexagon)
│   └── User.java                     # Domain entity with business rules
│
├── application/                      # Application layer
│   ├── ports/                        # Ports (interfaces)
│   │   ├── in/                       # Input ports (use cases)
│   │   │   ├── CreateUserUseCase.java
│   │   │   ├── GetUserUseCase.java
│   │   │   ├── UpdateUserUseCase.java
│   │   │   └── DeleteUserUseCase.java
│   │   └── out/                      # Output ports
│   │       └── UserRepository.java   # Repository interface
│   └── service/                      # Use case implementations
│       └── UserManagementService.java
│
├── adapter/                          # Adapters (implementations)
│   ├── in/                           # Input adapters
│   │   └── web/                      # REST API adapter
│   │       ├── dto/                  # Data Transfer Objects
│   │       │   ├── ApiResponse.java
│   │       │   ├── CreateUserRequest.java
│   │       │   ├── UpdateUserRequest.java
│   │       │   └── UserResponse.java
│   │       ├── UserController.java   # REST controller
│   │       └── HelloController.java
│   └── out/                          # Output adapters
│       └── persistence/              # Persistence adapter
│           └── InMemoryUserRepository.java
│
└── config/                           # Configuration
    └── CorsConfig.java               # CORS configuration
```

## Layers Explained

### 1. Domain Layer (Core/Hexagon)
**Location:** `domain/`

The innermost layer containing:
- **Business entities** with their invariants and rules
- **Domain logic** that is independent of any framework or technology
- **No dependencies** on outer layers

**Example:** `User.java`
- Immutable domain entity
- Factory methods for creation
- Business validation rules
- No framework dependencies

### 2. Application Layer
**Location:** `application/`

Contains the application's business logic:

#### Ports (Interfaces)
- **Input Ports** (`ports/in/`): Define what the application can do (use cases)
  - `CreateUserUseCase`: Interface for creating users
  - `GetUserUseCase`: Interface for retrieving users
  - `UpdateUserUseCase`: Interface for updating users
  - `DeleteUserUseCase`: Interface for deleting users

- **Output Ports** (`ports/out/`): Define how the application interacts with external systems
  - `UserRepository`: Interface for persistence operations

#### Services
- **Use Case Implementations** (`service/`)
  - `UserManagementService`: Implements all user use cases
  - Contains business orchestration logic
  - Depends only on ports (interfaces), not implementations

### 3. Adapter Layer
**Location:** `adapter/`

Implements the ports and connects the application to the external world:

#### Input Adapters (`adapter/in/`)
Convert external requests to use case calls:
- **Web Adapter** (`web/`):
  - `UserController`: REST API endpoints
  - DTOs: Data transfer objects for API communication
  - Converts HTTP requests to use case calls

#### Output Adapters (`adapter/out/`)
Implement the output ports:
- **Persistence Adapter** (`persistence/`):
  - `InMemoryUserRepository`: In-memory implementation of UserRepository
  - Can be easily replaced with JPA, MongoDB, etc.

## Key Principles

### 1. Dependency Rule
Dependencies point **inward**:
```
Adapters → Application → Domain
```
- Adapters depend on Application
- Application depends on Domain
- Domain has no dependencies

### 2. Port Isolation
- Application core defines ports (interfaces)
- Adapters implement these ports
- Core doesn't know about adapter implementations

### 3. Framework Independence
- Domain and Application layers are framework-agnostic
- Only adapters contain framework-specific code (Spring annotations, etc.)

### 4. Testability
Each layer can be tested independently:
- **Domain**: Pure unit tests, no mocks needed
- **Application**: Test with mocked ports
- **Adapters**: Test with real or mocked use cases

## Benefits

### 1. Flexibility
- Easy to replace implementations (e.g., swap in-memory storage for database)
- Add new adapters without changing core logic
- Support multiple interfaces (REST, GraphQL, CLI, etc.)

### 2. Testability
- Test business logic without external dependencies
- Mock adapters easily
- Fast unit tests

### 3. Maintainability
- Clear separation of concerns
- Changes in one layer don't affect others
- Easy to understand and navigate

### 4. Technology Independence
- Core business logic is framework-agnostic
- Can migrate to different frameworks without rewriting business logic
- Database-agnostic

## Example Flow

### Creating a User (POST /api/users)

1. **HTTP Request** arrives at `UserController` (Input Adapter)
2. **Controller** extracts data from `CreateUserRequest` DTO
3. **Controller** calls `CreateUserUseCase.createUser()` (Input Port)
4. **UserManagementService** implements the use case:
   - Creates `User` domain entity (validation happens here)
   - Calls `UserRepository.save()` (Output Port)
5. **InMemoryUserRepository** (Output Adapter) persists the user
6. **Domain User** is returned through the layers
7. **Controller** converts to `UserResponse` DTO
8. **HTTP Response** is sent back

```
HTTP → Controller → UseCase → Service → Repository → Persistence
                                  ↓
                              Domain
                                  ↓
HTTP ← Controller ← Service ← Repository ← Persistence
```

## How to Extend

### Add a New Use Case
1. Create interface in `application/ports/in/`
2. Implement in `UserManagementService`
3. Add endpoint in `UserController`

### Add a Database
1. Create new adapter in `adapter/out/persistence/`
2. Implement `UserRepository` interface
3. Configure Spring to use it (via `@Primary` or profiles)

### Add a New Interface (e.g., GraphQL)
1. Create new adapter in `adapter/in/graphql/`
2. Use existing use cases
3. No changes to core logic needed

## Testing Strategy

### Domain Layer
```java
// Pure unit tests, no frameworks
@Test
void shouldCreateUserWithValidData() {
    User user = User.create("John", "john@example.com", "USER");
    assertEquals("John", user.getName());
}
```

### Application Layer
```java
// Test with mocked repositories
@Test
void shouldCreateUser() {
    UserRepository mockRepo = mock(UserRepository.class);
    UserManagementService service = new UserManagementService(mockRepo);
    // Test business logic
}
```

### Adapter Layer
```java
// Integration tests with real/test adapters
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    // Test REST endpoints
}
```

## References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/) by Alistair Cockburn
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) by Robert C. Martin
- [Ports and Adapters Pattern](https://herbertograca.com/2017/09/14/ports-adapters-architecture/)
