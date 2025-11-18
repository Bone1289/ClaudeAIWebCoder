# Audit Logging and Monitoring Features

This document describes the new audit logging system and monitoring dashboards added to the Virtual Bank application.

## 🎯 Features Implemented

### 1. Comprehensive Audit Logging System

#### Audit Log Domain Model
- **Location**: `backend/src/main/java/com/example/demo/domain/AuditLog.java`
- **Features**:
  - Immutable domain entity with builder pattern
  - Captures user actions with context (IP address, user agent, timestamp)
  - Supports success/failure status tracking
  - Tracks entity type and ID for detailed auditing

#### Audit Actions Tracked:
- `LOGIN` - User login events
- `LOGOUT` - User logout events
- `ACCOUNT_CREATED` - New bank account creation
- `ACCOUNT_CLOSED` - Bank account closure
- `TRANSACTION_DEPOSIT` - Deposit transactions
- `TRANSACTION_WITHDRAWAL` - Withdrawal transactions
- `TRANSACTION_TRANSFER` - Transfer transactions
- `USER_REGISTERED` - New user registration
- `PASSWORD_CHANGED` - Password change events (future)
- `PROFILE_UPDATED` - Profile updates (future)

#### Audit Service
- **Location**: `backend/src/main/java/com/example/demo/application/service/AuditService.java`
- **Features**:
  - Asynchronous logging to prevent performance impact
  - Automatic metrics collection via Micrometer
  - IP address extraction from various proxy headers
  - Query methods for audit log analysis
  - Prometheus metrics: `audit.action` (counter by action and status)

#### Database Schema
- **Table**: `audit_logs`
- **Indexes**:
  - `idx_user_id` - Fast user-based queries
  - `idx_action` - Fast action-type queries
  - `idx_timestamp` - Fast time-range queries
  - `idx_entity` - Fast entity-based queries
- **Auto-created**: Via Hibernate DDL with `ddl-auto=update`

### 2. Integration Points

#### Authentication (Login/Logout/Registration)
- **File**: `backend/src/main/java/com/example/demo/adapter/in/web/auth/AuthController.java`
- **Changes**:
  - Added audit logging for successful and failed login attempts
  - Added audit logging for user registration
  - Added new `/api/auth/logout` endpoint with audit logging
  - Captures IP address and user agent for all auth events

#### Banking Transactions
- **File**: `backend/src/main/java/com/example/demo/application/service/BankingService.java`
- **Changes**:
  - Audit logging for all deposit transactions
  - Audit logging for all withdrawal transactions
  - Audit logging for all transfer transactions
  - Audit logging for account creation
  - Audit logging for account closure
  - Uses `RequestContextHolder` to access HTTP request context

### 3. Account Closure Feature

#### Functionality
- Users can now close their bank accounts
- **Endpoint**: `DELETE /api/banking/accounts/{id}`
- **Business Rules**:
  - Account balance must be zero before closure
  - Proper error message if non-zero balance
  - Audit log created on successful closure
- **Location**: Enhanced existing `deleteAccount` method in `BankingService`

### 4. Grafana Dashboards

#### HTTP Endpoints Metrics Dashboard
- **File**: `monitoring/grafana/dashboards/http-endpoints-dashboard.json`
- **UID**: `http-endpoints`
- **Panels**:
  1. **HTTP Request Rate by Endpoint** - Line chart showing requests/sec per endpoint
  2. **HTTP Response Status Codes** - Line chart for 2xx/4xx/5xx responses
  3. **Success Rate (2xx)** - Gauge showing percentage of successful requests
  4. **Client Error Rate (4xx)** - Gauge showing percentage of client errors
  5. **Server Error Rate (5xx)** - Gauge showing percentage of server errors
  6. **Response Time Percentiles by Endpoint** - p50, p95, p99 latencies
  7. **Audit Actions (Last 5 minutes)** - Line chart of audit events
  8. **Top Endpoints by Request Count** - Pie chart of most used endpoints
- **Refresh**: 10 seconds
- **Time Range**: Last 1 hour

#### Kafka Metrics Dashboard
- **File**: `monitoring/grafana/dashboards/kafka-metrics-dashboard.json`
- **UID**: `kafka-metrics`
- **Panels**:
  1. **Notification Queue Size** - Gauge showing current queue size
  2. **Total Notifications Produced** - Stat showing cumulative count
  3. **Total Notifications Consumed** - Stat showing cumulative count
  4. **Total Failed Notifications** - Stat showing failure count
  5. **Kafka Notification Rate** - Line chart for sent/processed/failed rates
  6. **Kafka Queue Size Over Time** - Line chart of queue size trends
  7. **Kafka Throughput (Last Hour)** - Line chart of production vs consumption
  8. **Kafka Success Rate (Producer)** - Gauge showing producer success rate
  9. **Kafka Consumer Records Rate by Topic** - Line chart per topic
  10. **Kafka Latency Metrics** - Line chart of producer/consumer latencies
- **Refresh**: 10 seconds
- **Time Range**: Last 1 hour

## 🚀 Usage

### Accessing Audit Logs

#### Via Service Layer
```java
@Autowired
private AuditService auditService;

// Get logs for a specific user
Page<AuditLog> userLogs = auditService.getAuditLogsForUser(userId, pageable);

// Get logs by action type
Page<AuditLog> loginAttempts = auditService.getAuditLogsByAction(
    AuditLog.AuditAction.LOGIN,
    pageable
);

// Get logs by date range
List<AuditLog> logs = auditService.getAuditLogsByDateRange(startDate, endDate);

// Get logs for a specific entity
List<AuditLog> accountLogs = auditService.getAuditLogsForEntity("Account", accountId);
```

#### Via Database
```sql
-- Find all failed login attempts
SELECT * FROM audit_logs
WHERE action = 'LOGIN'
AND status = 'FAILURE'
ORDER BY timestamp DESC;

-- Find all actions by a specific user
SELECT * FROM audit_logs
WHERE user_id = ?
ORDER BY timestamp DESC;

-- Find all transactions in the last 24 hours
SELECT * FROM audit_logs
WHERE action IN ('TRANSACTION_DEPOSIT', 'TRANSACTION_WITHDRAWAL', 'TRANSACTION_TRANSFER')
AND timestamp > DATE_SUB(NOW(), INTERVAL 24 HOUR);
```

### Accessing Dashboards

1. **Start the application with Docker Compose**:
   ```bash
   docker-compose up -d
   ```

2. **Access Grafana**:
   - URL: http://localhost:3000
   - Username: `admin`
   - Password: `admin`

3. **View Dashboards**:
   - Navigate to Dashboards → Browse
   - Select "HTTP Endpoints Metrics" or "Kafka Metrics"

### Testing Audit Logging

#### Test Login Audit
```bash
# Successful login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrUsername": "user@example.com", "password": "password123"}'

# Failed login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrUsername": "user@example.com", "password": "wrongpassword"}'
```

#### Test Transaction Audit
```bash
# Deposit
curl -X POST http://localhost:8080/api/banking/accounts/{accountId}/deposit \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00, "description": "Test deposit"}'

# Withdrawal
curl -X POST http://localhost:8080/api/banking/accounts/{accountId}/withdraw \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"amount": 50.00, "description": "Test withdrawal"}'

# Transfer
curl -X POST http://localhost:8080/api/banking/accounts/{fromAccountId}/transfer \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"toAccountId": "{toAccountId}", "amount": 25.00, "description": "Test transfer"}'
```

#### Test Logout Audit
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer {token}"
```

#### Test Account Closure
```bash
# First ensure account balance is zero
curl -X DELETE http://localhost:8080/api/banking/accounts/{accountId} \
  -H "Authorization: Bearer {token}"
```

## 📊 Metrics Exposed

### Audit Metrics
- `audit.action{action, status}` - Counter of audit actions
  - Tags: `action` (LOGIN, LOGOUT, etc.), `status` (SUCCESS, FAILURE)
  - Example: `audit.action{action="LOGIN", status="SUCCESS"}`

### HTTP Metrics (Spring Boot Actuator)
- `http.server.requests` - Histogram of HTTP requests
  - Tags: `uri`, `method`, `status`, `exception`
- `http_server_requests_seconds_count` - Counter of requests
- `http_server_requests_seconds_sum` - Sum of request durations
- `http_server_requests_seconds_bucket` - Request duration buckets

### Kafka Metrics (Custom + Spring Kafka)
- `kafka.notification.queue.size` - Gauge of notification queue size
- `kafka.notification.produced.total` - Counter of produced notifications
- `kafka.notification.consumed.total` - Counter of consumed notifications
- `kafka.notification.failed.total` - Counter of failed notifications
- `kafka.notifications.sent` - Counter from NotificationProducer
- `kafka.notifications.failed` - Counter from NotificationProducer
- `kafka.notifications.processed` - Counter from NotificationConsumer
- `kafka.notifications.processing.failed` - Counter from NotificationConsumer

## 🔒 Security Considerations

1. **Sensitive Data**: Audit logs do NOT store passwords or sensitive financial data
2. **IP Tracking**: Properly handles X-Forwarded-For headers for proxy/load balancer environments
3. **Async Logging**: Prevents audit failures from blocking business operations
4. **Access Control**: Audit log endpoints should be restricted to admin users
5. **Retention**: Consider implementing audit log archival/retention policies

## 📝 Future Enhancements

1. **Audit Log API Endpoints**: Create REST endpoints for querying audit logs
2. **Audit Dashboard**: Create dedicated UI for viewing audit logs
3. **Alert Rules**: Set up Prometheus alerts for suspicious patterns
4. **Log Aggregation**: Integrate with ELK stack for centralized audit log analysis
5. **Compliance Reports**: Generate compliance reports from audit data
6. **Data Retention**: Implement automatic archival of old audit logs
7. **Advanced Filtering**: Add more query capabilities (by date range, action type, etc.)

## 🧪 Testing

To verify the implementation:

1. **Start the application**
2. **Perform various operations** (login, create account, make transactions)
3. **Check the database**:
   ```sql
   SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 10;
   ```
4. **View Prometheus metrics**:
   - http://localhost:9090/graph
   - Query: `audit_action_total`
5. **View Grafana dashboards**:
   - http://localhost:3000
   - Check "HTTP Endpoints Metrics" and "Kafka Metrics" dashboards

## 📚 References

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Metrics](https://micrometer.io/docs)
- [Prometheus](https://prometheus.io/docs/introduction/overview/)
- [Grafana](https://grafana.com/docs/)
- [Spring Kafka](https://spring.io/projects/spring-kafka)
