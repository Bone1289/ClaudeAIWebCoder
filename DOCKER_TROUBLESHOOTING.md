# Docker Troubleshooting Guide

## Common Issues and Solutions

### MySQL Container Fails to Start

**Symptoms:**
- Container exits after 2-3 seconds
- Error: `Container virtualbank-mysql Error 2.3s`

**Solutions:**

1. **Port Conflict (Port 3306)**
   ```bash
   # Check if port 3306 is in use
   lsof -i :3306
   # or
   netstat -tuln | grep 3306

   # Stop the conflicting service or change the port in docker-compose.yml
   ```

2. **Corrupted Volume**
   ```bash
   # Remove the corrupted volume and start fresh
   docker compose down -v
   docker compose up -d
   ```

3. **Permission Issues**
   ```bash
   # Remove volumes and recreate
   docker compose down -v
   docker volume rm virtualbank_mysql-data
   docker compose up -d mysql
   ```

4. **Check Logs**
   ```bash
   docker compose logs mysql
   ```

### Elasticsearch Container Fails to Start

**Symptoms:**
- Container exits after 2-3 seconds
- Error: `Container virtualbank-elasticsearch Error 2.1s`

**Solutions:**

1. **vm.max_map_count Too Low (Most Common - Linux/WSL)**

   Elasticsearch requires `vm.max_map_count` to be at least 262144.

   **Temporary Fix:**
   ```bash
   sudo sysctl -w vm.max_map_count=262144
   ```

   **Permanent Fix:**
   ```bash
   echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
   sudo sysctl -p
   ```

   **For WSL2 (Windows):**
   Create or edit `%USERPROFILE%\.wslconfig`:
   ```ini
   [wsl2]
   kernelCommandLine = sysctl.vm.max_map_count=262144
   ```
   Then restart WSL:
   ```powershell
   wsl --shutdown
   ```

2. **Insufficient Memory**

   Elasticsearch needs at least 512MB RAM. Check Docker settings:
   - Docker Desktop → Settings → Resources → Memory
   - Recommended: At least 4GB for the entire application

3. **Port Conflicts (9200, 9300)**
   ```bash
   # Check if ports are in use
   lsof -i :9200
   lsof -i :9300
   ```

4. **Corrupted Volume**
   ```bash
   docker compose down -v
   docker volume rm virtualbank_elasticsearch-data
   docker compose up -d elasticsearch
   ```

5. **Check Logs**
   ```bash
   docker compose logs elasticsearch
   ```

### All Containers Fail to Start

**Solutions:**

1. **Docker Daemon Not Running**
   - Start Docker Desktop
   - Verify: `docker info`

2. **Insufficient Resources**
   - Allocate more memory/CPU in Docker Desktop settings
   - Recommended minimums:
     - Memory: 4GB
     - CPU: 2 cores

3. **Clean Slate Approach**
   ```bash
   # Stop everything
   docker compose down -v

   # Remove all stopped containers
   docker container prune -f

   # Remove unused volumes
   docker volume prune -f

   # Start fresh
   ./fix-docker-startup.sh
   ```

## Quick Fix Commands

### Use the Fix Script (Recommended)
```bash
./fix-docker-startup.sh
```

### Manual Steps

1. **Stop all services:**
   ```bash
   docker compose down
   ```

2. **Remove volumes (deletes all data):**
   ```bash
   docker compose down -v
   ```

3. **Start with rebuild:**
   ```bash
   docker compose up -d --build
   ```

4. **View logs:**
   ```bash
   docker compose logs -f
   ```

5. **Check service status:**
   ```bash
   docker compose ps
   ```

## Platform-Specific Notes

### Linux
- May need `sudo` for sysctl commands
- Elasticsearch requires vm.max_map_count=262144

### macOS
- Docker Desktop manages vm.max_map_count automatically
- Check Docker Desktop has sufficient resources allocated

### Windows (WSL2)
- Set vm.max_map_count in `.wslconfig` file
- Restart WSL after changes: `wsl --shutdown`
- Docker Desktop must be integrated with WSL2

## Still Having Issues?

1. **View specific container logs:**
   ```bash
   docker compose logs <service-name>
   # Examples:
   docker compose logs mysql
   docker compose logs elasticsearch
   docker compose logs backend
   ```

2. **Check container health:**
   ```bash
   docker compose ps
   docker inspect virtualbank-mysql | grep -A 10 Health
   docker inspect virtualbank-elasticsearch | grep -A 10 Health
   ```

3. **Restart specific service:**
   ```bash
   docker compose restart <service-name>
   ```

4. **Rebuild specific service:**
   ```bash
   docker compose build <service-name>
   docker compose up -d <service-name>
   ```

## Verification Steps

After fixing issues, verify everything is working:

```bash
# 1. Check all containers are running
docker compose ps

# 2. Check MySQL
docker compose exec mysql mysql -uroot -proot -e "SHOW DATABASES;"

# 3. Check Elasticsearch
curl http://localhost:9200

# 4. Check Backend Health
curl http://localhost:8080/actuator/health

# 5. Check Frontend
curl http://localhost/
```

## Resource Requirements

Minimum system requirements:
- **RAM**: 6GB (4GB for Docker)
- **CPU**: 2 cores
- **Disk**: 10GB free space
- **OS**:
  - Linux: kernel 3.10+, vm.max_map_count=262144
  - macOS: 10.15+ with Docker Desktop
  - Windows: Windows 10+ with WSL2 and Docker Desktop
