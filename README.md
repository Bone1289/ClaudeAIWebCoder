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
├── frontend/               # Angular user application (TypeScript + Node.js)
│   ├── src/
│   │   ├── app/           # Angular components and services
│   │   ├── assets/        # Static assets
│   │   └── environments/  # Environment configurations
│   ├── package.json       # NPM dependencies
│   ├── angular.json       # Angular CLI configuration
│   └── proxy.conf.json    # Development proxy configuration
│
├── frontend-admin/         # Angular admin portal (TypeScript + Node.js)
│   ├── src/
│   │   ├── app/           # Admin components and services
│   │   ├── assets/        # Static assets
│   │   └── environments/  # Environment configurations
│   ├── package.json       # NPM dependencies
│   └── angular.json       # Angular CLI configuration
│
├── docker-compose.yml      # Docker Compose configuration
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

#### 3. Start the Admin Portal (Optional)

In a separate terminal:

```bash
cd frontend-admin
npm install
npm start
```

The admin portal will start on `http://localhost:4201`

#### 4. Access the Application

Open your browser and navigate to:
- **User Frontend:** http://localhost:4200
- **Admin Portal:** http://localhost:4201
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

## Docker Support

The complete application stack can be run using Docker Compose:

### Quick Start with Docker

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Services Included

The Docker Compose setup includes:

1. **MySQL Database** (port 3306)
   - Pre-configured with database schema
   - Persistent data storage

2. **Spring Boot Backend** (port 8080)
   - Automatic database connection
   - Health checks enabled

3. **Angular User Frontend** (port 80)
   - Production-optimized build
   - Nginx web server

4. **Angular Admin Portal** (port 4201)
   - Admin-only interface
   - Separate deployment

### Access Points

Once Docker Compose is running:
- **User Application:** http://localhost
- **Admin Portal:** http://localhost:4201
- **Backend API:** http://localhost:8080/api
- **MySQL:** localhost:3306

### Default Credentials

**Regular User:**
- Email: demo@example.com
- Password: password123

**Admin User:**
- Username: admin
- Password: Admin

### Docker Commands

```bash
# Build without cache
docker-compose build --no-cache

# Start specific service
docker-compose up -d backend

# View service logs
docker-compose logs -f frontend-admin

# Restart a service
docker-compose restart backend

# Remove everything including volumes
docker-compose down -v
```

### Docker Troubleshooting

If you experience issues starting the Docker containers, use the fix script:

```bash
./fix-docker-startup.sh
```

**Common Issues:**

1. **MySQL or Elasticsearch Containers Fail to Start**
   - Run: `./fix-docker-startup.sh`
   - See: [DOCKER_TROUBLESHOOTING.md](DOCKER_TROUBLESHOOTING.md) for detailed solutions

2. **Elasticsearch Fails on Linux/WSL** (most common)
   ```bash
   # Fix vm.max_map_count (required for Elasticsearch)
   sudo sysctl -w vm.max_map_count=262144

   # Make it permanent
   echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
   ```

3. **Port Conflicts**
   - Check if ports are in use: `lsof -i :3306` or `lsof -i :9200`
   - Stop conflicting services or change ports in docker-compose.yml

4. **Corrupted Volumes**
   ```bash
   # Remove all volumes and start fresh
   docker-compose down -v
   docker-compose up -d --build
   ```

For comprehensive troubleshooting, see [DOCKER_TROUBLESHOOTING.md](DOCKER_TROUBLESHOOTING.md)

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
