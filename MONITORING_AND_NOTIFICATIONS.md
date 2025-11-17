# Monitoring, Logging, and Notifications Implementation Guide

## ✅ Implemented Features

### 1. Monitoring Stack (Prometheus + Grafana)

**Prometheus** - Metrics collection from Spring Boot
- **Port**: 9090
- **Configuration**: `monitoring/prometheus/prometheus.yml`
- **Scrapes**: Backend actuator metrics every 15s
- **Access**: http://localhost:9090

**Grafana** - Metrics visualization
- **Port**: 3000
- **Username**: admin
- **Password**: admin
- **Dashboards**: Auto-provisioned Spring Boot dashboard
- **Access**: http://localhost:3000

**Backend Changes**:
- Added `micrometer-registry-prometheus` dependency
- Exposed Prometheus metrics at `/actuator/prometheus`
- Enabled all actuator endpoints for monitoring

### 2. Logging Stack (ELK)

**Elasticsearch** - Log storage
- **Port**: 9200 (HTTP), 9300 (Transport)
- **Storage**: Persistent volume
- **Index Pattern**: `virtual-bank-logs-*`

**Logstash** - Log processing
- **Port**: 5000 (TCP input), 9600 (API)
- **Configuration**: `monitoring/logstash/logstash.conf`
- **Processes**: JSON logs from backend

**Kibana** - Log visualization
- **Port**: 5601
- **Access**: http://localhost:5601
- **Features**: Search, filter, visualize logs

**Backend Changes**:
- Added `logstash-logback-encoder` dependency
- Created `logback-spring.xml` for structured logging
- Logs sent to both console and Logstash

### 3. Email System

**MailHog** - Email testing server
- **SMTP Port**: 1025
- **Web UI Port**: 8025
- **Access**: http://localhost:8025
- **Features**: Catch and display all emails

**Backend Changes**:
- Added `spring-boot-starter-mail` dependency
- Configured Spring Mail properties
- Ready for email notification implementation

### 4. Notification Domain Model

**Created**:
- `Notification` domain entity
- Support for multiple notification types (ACCOUNT_CREATED, TRANSACTION, SECURITY_ALERT, etc.)
- Support for multiple channels (IN_APP, EMAIL, BOTH)
- Priority levels (LOW, MEDIUM, HIGH, URGENT)
- Read/unread status tracking

## 🚧 Pending Implementation

### Notification System Backend
1. **Persistence Layer**:
   - NotificationJpaEntity
   - NotificationRepository interface
   - JpaNotificationRepository implementation
   - NotificationMapper

2. **Application Layer**:
   - NotificationService
   - CreateNotificationUseCase
   - GetNotificationsUseCase
   - MarkAsReadUseCase
   - DeleteNotificationUseCase

3. **Email Service**:
   - EmailService implementation
   - Email templates (Thymeleaf)
   - Async email sending

4. **REST API**:
   - NotificationController
   - GET /api/notifications (list user notifications)
   - POST /api/notifications (create notification - admin only)
   - PUT /api/notifications/{id}/read (mark as read)
   - DELETE /api/notifications/{id} (delete notification)
   - GET /api/notifications/unread-count (get unread count)

### Notification System Frontend
1. **User Frontend**:
   - Notification bell icon in header
   - Notification dropdown/panel
   - Unread count badge
   - Real-time updates (polling or WebSocket)
   - Mark as read functionality

2. **Admin Frontend**:
   - Send notification interface
   - Target user selection
   - Notification type selection
   - Preview before sending

## 📊 Monitoring Dashboards

### Grafana Dashboard Metrics
- HTTP request rate
- JVM heap usage
- Active database connections
- Response time percentiles
- Error rates
- Thread pool usage

### Kibana Log Analysis
- Error logs tracking
- Request/response logs
- Security events
- Application performance

## 🚀 How to Use

### Start All Services
```bash
docker-compose up -d
```

### Access Monitoring Tools
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Kibana**: http://localhost:5601
- **MailHog**: http://localhost:8025
- **Backend Metrics**: http://localhost:8080/actuator/prometheus

### View Logs in Kibana
1. Go to http://localhost:5601
2. Create index pattern: `virtual-bank-logs-*`
3. Go to Discover tab
4. Filter by level, message, or custom fields

### Send Test Email
```java
// In your service class
@Autowired
private JavaMailSender mailSender;

SimpleMailMessage message = new SimpleMailMessage();
message.setTo("user@example.com");
message.setSubject("Test");
message.setText("Hello!");
mailSender.send(message);
```
Then check http://localhost:8025 to see the email

## 📦 Docker Services

| Service | Port | Purpose |
|---------|------|---------|
| MySQL | 3306 | Database |
| Backend | 8080 | Spring Boot API |
| Frontend | 80 | User portal |
| Admin Portal | 4201 | Admin interface |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3000 | Metrics visualization |
| Elasticsearch | 9200 | Log storage |
| Logstash | 5000 | Log processing |
| Kibana | 5601 | Log visualization |
| MailHog | 8025 | Email testing |

## 🔍 Troubleshooting

### Elasticsearch fails to start
- Increase Docker memory to at least 4GB
- Check logs: `docker-compose logs elasticsearch`

### Logs not appearing in Kibana
1. Check Logstash is running: `docker-compose ps logstash`
2. Verify backend can connect: `docker-compose logs backend | grep logstash`
3. Check Elasticsearch indices: `curl http://localhost:9200/_cat/indices`

### Grafana dashboard empty
1. Verify Prometheus is scraping: http://localhost:9090/targets
2. Check backend metrics: http://localhost:8080/actuator/prometheus
3. Generate some traffic to the application

## 📝 Next Steps

1. Complete notification persistence layer
2. Implement email service with templates
3. Create notification REST API
4. Build notification UI components
5. Add WebSocket for real-time notifications
6. Create notification integration tests
7. Add notification documentation
