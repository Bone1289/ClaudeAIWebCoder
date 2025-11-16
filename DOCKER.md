# 🐳 Virtual Bank - Docker Setup

Run the entire application with **just Docker**! No need to install MySQL, Java, Node.js, or anything else.

---

## Prerequisites

### Only Docker Desktop Required!

**Download Docker Desktop:**
- **Windows/Mac**: https://www.docker.com/products/docker-desktop
- **Linux**: Follow official Docker Engine installation guide

**Verify Installation:**
```bash
docker --version
docker compose version
```

That's it! Everything else runs in containers.

---

## 🚀 Quick Start (One Command!)

### Option 1: Using the Startup Script (Recommended)

**macOS/Linux:**
```bash
./docker-start.sh
```

**Windows:**
```cmd
docker-start.bat
```

Then select option **1** to start all services.

### Option 2: Direct Docker Compose

```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f

# Stop all services
docker compose down
```

---

## 🌐 Access the Application

Once started (takes 2-3 minutes on first run):

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost | Angular web application |
| **Backend API** | http://localhost:8080/api | REST API endpoints |
| **Health Check** | http://localhost:8080/actuator/health | Application health |
| **MySQL** | localhost:3306 | Database (user: root, pass: root) |

---

## 📦 What Gets Installed in Containers

The Docker setup creates **3 containers**:

1. **virtualbank-mysql** - MySQL 8.0 database
2. **virtualbank-backend** - Spring Boot application (Java 21)
3. **virtualbank-frontend** - Angular app served by Nginx

All dependencies are automatically installed inside the containers.

---

## 🎯 Common Commands

### Start Services
```bash
# Start all services in background
docker compose up -d

# Start and rebuild images
docker compose up -d --build

# Start and view logs
docker compose up
```

### Stop Services
```bash
# Stop all services (keeps data)
docker compose down

# Stop and remove all data
docker compose down -v
```

### View Logs
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
```

### Restart Services
```bash
# Restart all
docker compose restart

# Restart specific service
docker compose restart backend
```

### Check Status
```bash
# View running containers
docker compose ps

# Detailed container info
docker ps
```

---

## 🔧 Architecture

```
┌─────────────────────────────────────────────────┐
│                  Docker Host                     │
│                                                  │
│  ┌──────────────┐  ┌──────────────┐            │
│  │   Frontend   │  │   Backend    │            │
│  │   (Nginx)    │  │ (Spring Boot)│            │
│  │   Port: 80   │  │  Port: 8080  │            │
│  └───────┬──────┘  └──────┬───────┘            │
│          │                 │                     │
│          │   API Proxy    │                     │
│          └────────┬────────┘                     │
│                   │                              │
│          ┌────────▼─────────┐                   │
│          │      MySQL       │                   │
│          │    Port: 3306    │                   │
│          └──────────────────┘                   │
│                                                  │
│          Volume: mysql-data                     │
│          Network: virtualbank-network           │
└──────────────────────────────────────────────────┘
```

---

## 🛠️ Development Workflow

### Hot Reload (Development Mode)

For development with hot reload, use the startup scripts from SETUP.md instead of Docker, or modify docker-compose to mount volumes:

```yaml
# Add under backend service
volumes:
  - ./backend/src:/app/src

# Add under frontend service
volumes:
  - ./frontend/src:/app/src
```

### Rebuild After Code Changes

```bash
# Rebuild and restart specific service
docker compose up -d --build backend

# Rebuild all
docker compose up -d --build
```

### Access MySQL Database

**Option 1: MySQL Client**
```bash
mysql -h 127.0.0.1 -P 3306 -u root -p
# Password: root
```

**Option 2: Docker Exec**
```bash
docker exec -it virtualbank-mysql mysql -u root -p
# Password: root

USE virtualbank;
SHOW TABLES;
```

**Option 3: MySQL Workbench / DBeaver**
- Host: `localhost`
- Port: `3306`
- User: `root`
- Password: `root`
- Database: `virtualbank`

---

## 📊 Container Details

### MySQL Container
- **Image**: mysql:8.0
- **Port**: 3306
- **Root Password**: root
- **Database**: virtualbank (auto-created)
- **Volume**: mysql-data (persistent storage)
- **Init Script**: database/init-database.sql

### Backend Container
- **Build**: Multi-stage (Gradle build + Java runtime)
- **Base Image**: eclipse-temurin:21-jre-alpine
- **Port**: 8080
- **Profile**: docker
- **Dependencies**: MySQL (waits for health check)

### Frontend Container
- **Build**: Multi-stage (npm build + nginx)
- **Base Image**: nginx:alpine
- **Port**: 80
- **Proxy**: /api/* → backend:8080
- **Config**: frontend/nginx.conf

---

## 🔍 Troubleshooting

### Port Already in Use

**Problem**: Error binding to port 80, 3306, or 8080

**Solution**: Stop conflicting services or change ports in docker-compose.yml:
```yaml
services:
  frontend:
    ports:
      - "8081:80"  # Change left side only
```

### Containers Not Starting

**Check logs:**
```bash
docker compose logs mysql
docker compose logs backend
docker compose logs frontend
```

**Common issues:**
- MySQL takes 30-60 seconds to initialize on first run
- Backend waits for MySQL health check
- Frontend waits for backend health check

### MySQL Connection Refused

**Wait for MySQL to be ready:**
```bash
# Check MySQL health
docker compose ps

# Should show "healthy" status
# Wait until: virtualbank-mysql   healthy
```

### Backend Compilation Errors

**Rebuild from scratch:**
```bash
docker compose down
docker compose build --no-cache backend
docker compose up -d
```

### Out of Disk Space

**Clean up Docker:**
```bash
# Remove unused containers, images, volumes
docker system prune -a --volumes

# Then rebuild
docker compose up -d --build
```

### Slow Performance

**Increase Docker resources:**
- Docker Desktop → Settings → Resources
- Increase CPU and Memory allocation
- Recommended: 4 CPU cores, 4GB RAM

---

## 🧹 Cleanup

### Remove All Containers (Keep Data)
```bash
docker compose down
```

### Remove Everything (Including Data)
```bash
docker compose down -v
```

### Complete Docker Cleanup
```bash
# Stop and remove all
docker compose down -v

# Remove images
docker rmi virtualbank-backend virtualbank-frontend

# Remove unused Docker resources
docker system prune -a
```

---

## 📝 Environment Variables

You can override settings in docker-compose.yml:

```yaml
backend:
  environment:
    SPRING_DATASOURCE_USERNAME: myuser
    SPRING_DATASOURCE_PASSWORD: mypassword
    SPRING_JPA_HIBERNATE_DDL_AUTO: create-drop
```

Or use a `.env` file:
```bash
# .env file
MYSQL_ROOT_PASSWORD=mypassword
MYSQL_DATABASE=virtualbank
```

---

## 🔐 Security Notes

**For Production:**
1. Change default passwords in docker-compose.yml
2. Use Docker secrets for sensitive data
3. Enable SSL/TLS
4. Configure firewall rules
5. Use private Docker registry
6. Scan images for vulnerabilities

**Development Defaults:**
- MySQL root password: `root`
- No SSL/TLS
- All ports exposed to host

---

## 📦 Build Optimization

### Faster Builds with BuildKit

```bash
# Enable BuildKit (faster builds, better caching)
export DOCKER_BUILDKIT=1

# Build with BuildKit
docker compose build
```

### Multi-Stage Build Benefits

- **Smaller Images**: Only runtime dependencies in final image
- **Faster**: Cached layers for dependencies
- **Secure**: No source code or build tools in final image

**Image Sizes:**
- Backend: ~300MB (vs 1GB+ with full JDK)
- Frontend: ~50MB (vs 500MB+ with Node.js)

---

## 🚀 Production Deployment

### Build Production Images

```bash
# Build with production optimizations
docker compose build --no-cache

# Tag images
docker tag virtualbank-backend:latest myregistry/virtualbank-backend:1.0.0
docker tag virtualbank-frontend:latest myregistry/virtualbank-frontend:1.0.0

# Push to registry
docker push myregistry/virtualbank-backend:1.0.0
docker push myregistry/virtualbank-frontend:1.0.0
```

### Use External MySQL

Modify docker-compose.yml to point to external database:
```yaml
backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://production-mysql-host:3306/virtualbank
```

---

## 💡 Tips & Best Practices

1. **Always use named volumes** for persistent data
2. **Health checks** ensure services start in correct order
3. **Use .dockerignore** to speed up builds
4. **Multi-stage builds** reduce image size
5. **Networks** isolate container communication
6. **Restart policies** keep services running

---

## 🆘 Need Help?

**View container logs:**
```bash
docker compose logs -f [service-name]
```

**Access container shell:**
```bash
docker exec -it virtualbank-backend /bin/sh
docker exec -it virtualbank-frontend /bin/sh
docker exec -it virtualbank-mysql /bin/bash
```

**Inspect configuration:**
```bash
docker compose config
```

---

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Angular with Docker](https://angular.io/guide/deployment)

---

**Happy Coding with Docker! 🐳🚀**
