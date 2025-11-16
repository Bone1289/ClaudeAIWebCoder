# Spring Boot Demo Application

A simple Spring Boot application with Gradle build configuration.

## Project Structure

```
.
├── build.gradle                 # Gradle build configuration
├── settings.gradle              # Gradle settings
├── gradle.properties            # Gradle properties
├── gradlew                      # Gradle wrapper script (Unix)
├── gradlew.bat                  # Gradle wrapper script (Windows)
├── gradle/                      # Gradle wrapper files
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/demo/
    │   │       ├── DemoApplication.java          # Main Spring Boot application
    │   │       └── controller/
    │   │           └── HelloController.java       # Sample REST controller
    │   └── resources/
    │       └── application.properties             # Application configuration
    └── test/
        └── java/
            └── com/example/demo/                  # Test directory
```

## Features

- Spring Boot 3.3.5
- Spring Web (REST API)
- Spring Boot Actuator (health checks)
- Gradle 8.x build system
- Java 17

## Building the Application

### Using Gradle Wrapper (Recommended)

```bash
./gradlew clean build
```

### Using Local Gradle Installation

```bash
gradle clean build
```

## Running the Application

```bash
./gradlew bootRun
```

Or after building:

```bash
java -jar build/libs/spring-demo-app-0.0.1-SNAPSHOT.jar
```

## API Endpoints

Once the application is running on port 8080:

- **GET /api/hello** - Returns a hello message
  - Query parameter: `name` (optional, default: "World")
  - Example: `http://localhost:8080/api/hello?name=John`

- **GET /api/health** - Returns application health status
  - Example: `http://localhost:8080/api/health`

- **GET /actuator/health** - Spring Boot Actuator health endpoint
  - Example: `http://localhost:8080/actuator/health`

## Configuration

Application configuration can be modified in `src/main/resources/application.properties`:

- Server port: `server.port` (default: 8080)
- Application name: `spring.application.name`
- Logging levels
- Actuator endpoints

## Development

### Prerequisites

- Java 17 or higher
- Gradle 8.x (or use the included Gradle wrapper)

### Testing

Run all tests:

```bash
./gradlew test
```

### Code Quality

The project includes:
- JUnit 5 for testing
- Spring Boot Test for integration testing

## Notes

This is a basic Spring Boot application template. You can extend it by adding:
- Database connectivity (Spring Data JPA)
- Security (Spring Security)
- Additional REST endpoints
- Business logic and services
- More comprehensive testing
