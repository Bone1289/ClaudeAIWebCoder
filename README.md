# Spring Boot + Angular Full-Stack Application

A full-stack demo application showcasing Spring Boot backend with Angular frontend.

## Project Structure

```
.
├── backend/                 # Spring Boot application (Java + Gradle)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/       # Java source code
│   │   │   └── resources/  # Application properties
│   │   └── test/           # Unit and integration tests
│   ├── build.gradle        # Gradle build configuration
│   ├── settings.gradle     # Gradle settings
│   └── gradlew            # Gradle wrapper
│
├── frontend/               # Angular application (TypeScript + Node.js)
│   ├── src/
│   │   ├── app/           # Angular components and services
│   │   ├── assets/        # Static assets
│   │   └── environments/  # Environment configurations
│   ├── package.json       # NPM dependencies
│   ├── angular.json       # Angular CLI configuration
│   └── proxy.conf.json    # Development proxy configuration
│
└── README.md              # This file
```

## Technology Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.3.5** - Application framework
- **Spring Web** - RESTful API
- **Spring Boot Actuator** - Health monitoring
- **Gradle 8.x** - Build tool

### Frontend
- **Angular 17** - Frontend framework
- **TypeScript 5.4** - Programming language
- **RxJS 7.8** - Reactive programming
- **Angular Router** - Navigation and routing
- **HttpClient** - HTTP communication

## Getting Started

### Prerequisites

**Backend:**
- Java 17 or higher
- Gradle 8.x (or use included wrapper)

**Frontend:**
- Node.js 18.x or higher
- npm 9.x or higher
- Angular CLI 17.x

### Running the Application

#### 1. Start the Backend

```bash
cd backend
./gradlew bootRun
```

The backend will start on `http://localhost:8080`

#### 2. Start the Frontend

In a separate terminal:

```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:4200`

#### 3. Access the Application

Open your browser and navigate to:
- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8080/api

## Available API Endpoints

### Backend Endpoints

- **GET /api/hello?name={name}**
  - Returns a personalized greeting message
  - Example: `http://localhost:8080/api/hello?name=John`

- **GET /api/health**
  - Returns application health status
  - Example: `http://localhost:8080/api/health`

- **GET /actuator/health**
  - Spring Boot Actuator health endpoint
  - Example: `http://localhost:8080/actuator/health`

## Development

### Backend Development

**Build the project:**
```bash
cd backend
./gradlew build
```

**Run tests:**
```bash
./gradlew test
```

**Clean build:**
```bash
./gradlew clean build
```

### Frontend Development

**Install dependencies:**
```bash
cd frontend
npm install
```

**Run development server:**
```bash
npm start
```

**Build for production:**
```bash
npm run build
```

**Run tests:**
```bash
npm test
```

## Frontend-Backend Integration

During development, the Angular application uses a proxy configuration (`frontend/proxy.conf.json`) to forward API requests to the Spring Boot backend. This avoids CORS issues:

- Frontend runs on `http://localhost:4200`
- Backend runs on `http://localhost:8080`
- API requests to `/api/*` are proxied from frontend to backend

## Project Features

- **RESTful API** - Clean REST endpoints with Spring Boot
- **Reactive Programming** - RxJS observables for async operations
- **Lazy Loading** - Angular modules loaded on-demand
- **Responsive Design** - Mobile-friendly UI
- **Error Handling** - Comprehensive error handling on both ends
- **Health Monitoring** - Built-in health checks
- **Development Proxy** - Seamless local development

## Building for Production

### Backend

Build a production JAR:

```bash
cd backend
./gradlew clean build
```

Run the JAR:

```bash
java -jar build/libs/spring-demo-app-0.0.1-SNAPSHOT.jar
```

### Frontend

Build for production:

```bash
cd frontend
npm run build
```

The production files will be in `frontend/dist/spring-demo-frontend/`.

You can:
1. Serve them from a web server (nginx, Apache, etc.)
2. Deploy to cloud platforms (Netlify, Vercel, AWS S3, etc.)
3. Serve them from Spring Boot as static resources

## Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
server.port=8080
spring.application.name=spring-demo-app
```

### Frontend Configuration

Edit `frontend/src/environments/environment.ts` for development:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

## Docker Support (Optional)

You can containerize both applications:

**Backend Dockerfile example:**
```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

**Frontend Dockerfile example:**
```dockerfile
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/spring-demo-frontend /usr/share/nginx/html
```

## Troubleshooting

### Backend Issues

**Port already in use:**
- Change the port in `application.properties`: `server.port=8081`

**Gradle build fails:**
- Ensure Java 17 is installed: `java -version`
- Clean the build: `./gradlew clean`

### Frontend Issues

**npm install fails:**
- Clear npm cache: `npm cache clean --force`
- Delete `node_modules` and reinstall

**Proxy not working:**
- Ensure backend is running on port 8080
- Check `proxy.conf.json` configuration

**API calls fail:**
- Check browser console for errors
- Verify backend is running: `http://localhost:8080/api/health`

## Contributing

This is a demo application for educational purposes. Feel free to extend it with:
- Database integration (Spring Data JPA)
- Authentication (Spring Security)
- Additional REST endpoints
- More Angular components
- Unit and integration tests

## License

This is a demo application for educational purposes.

## Documentation

For more details, see:
- [Backend README](backend/README.md)
- [Frontend README](frontend/README.md)
