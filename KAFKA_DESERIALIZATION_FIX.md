# Kafka Deserialization Error Fix

## Problem

You encountered this error:

```
java.lang.IllegalStateException: This error handler cannot process 'SerializationException's directly
...
Caused by: com.fasterxml.jackson.databind.exc.InvalidDefinitionException:
Cannot construct instance of `com.example.demo.domain.notification.Notification`
(no Creators, like default constructor, exist): cannot deserialize from Object value
```

## Root Cause

The `Notification` domain class is **immutable** with:
- All `final` fields
- A **private constructor**
- No default (no-args) constructor

Jackson (used by Kafka for JSON serialization) couldn't deserialize because it had no way to create instances.

## Solution

Added Jackson annotations to the `Notification` class:

```java
@JsonCreator
private Notification(
    @JsonProperty("id") UUID id,
    @JsonProperty("userId") UUID userId,
    @JsonProperty("type") NotificationType type,
    // ... etc
) {
    // constructor body
}
```

**What this does:**
- `@JsonCreator` - Tells Jackson to use this constructor for deserialization
- `@JsonProperty("fieldName")` - Maps JSON fields to constructor parameters

This maintains the immutability while allowing Kafka to deserialize messages.

## How to Apply the Fix

### Option 1: Quick Fix (Recommended)

If you have **old messages** in Kafka that can't be deserialized, reset the Kafka topics:

```bash
./reset-kafka-topics.sh
```

This will:
1. Stop all containers
2. Delete Kafka data volumes (clears old messages)
3. Restart containers with fresh topics

### Option 2: Manual Fix

If you want to keep existing messages:

```bash
# Just restart the backend to pick up the code changes
docker-compose restart backend

# The new code can deserialize new messages
# Old messages will still cause errors until they expire or are manually skipped
```

### Option 3: Complete Rebuild

For a clean slate:

```bash
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

## Verify the Fix

1. Check backend logs for errors:
   ```bash
   docker-compose logs -f backend | grep -i "serialization"
   ```

2. You should **NOT** see any `SerializationException` errors.

3. Test notifications:
   - Login → Should get security alert notification
   - Create account → Should get account created notification
   - Make deposit → Should get transaction notification
   - Check the notification bell 🔔

## What Changed

**File:** `backend/src/main/java/com/example/demo/domain/notification/Notification.java`

**Changes:**
```diff
+ import com.fasterxml.jackson.annotation.JsonCreator;
+ import com.fasterxml.jackson.annotation.JsonProperty;

+ @JsonCreator
  private Notification(
+     @JsonProperty("id") UUID id,
+     @JsonProperty("userId") UUID userId,
+     @JsonProperty("type") NotificationType type,
+     @JsonProperty("channel") NotificationChannel channel,
+     @JsonProperty("title") String title,
+     @JsonProperty("message") String message,
+     @JsonProperty("priority") NotificationPriority priority,
+     @JsonProperty("read") boolean read,
+     @JsonProperty("createdAt") LocalDateTime createdAt,
+     @JsonProperty("readAt") LocalDateTime readAt
  ) {
```

## Why This Happened

When we initially created the notification system, the `Notification` class worked fine for:
- Database persistence (JPA has its own mapping)
- Direct object creation
- SSE (Server-Sent Events) transmission

But when we added notifications to **authentication and transaction events**, they went through **Kafka**, which uses Jackson for JSON serialization. Jackson needs special annotations to work with immutable objects.

## Benefits of This Approach

✅ **Maintains immutability** - All fields still `final`
✅ **Clean architecture** - Domain model unchanged structurally
✅ **Kafka compatible** - Works with JSON serialization
✅ **No DTOs needed** - Can use domain objects directly in Kafka

## Alternative Approaches (Not Used)

We could have:
1. ❌ Added a default constructor - Breaks immutability
2. ❌ Made fields mutable - Breaks domain model integrity
3. ❌ Created separate DTOs - Extra complexity and mapping overhead

## Testing

After applying the fix, test all notification triggers:

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"emailOrUsername": "user@example.com", "password": "password"}'

# Create account (after login)
curl -X POST http://localhost:8080/api/banking/accounts \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "nationality": "US",
    "accountType": "CHECKING"
  }'

# Make deposit
curl -X POST http://localhost:8080/api/banking/accounts/{accountId}/deposit \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00, "description": "Test deposit"}'
```

Each action should:
1. ✅ Complete successfully
2. ✅ Create a notification in the database
3. ✅ Send notification to Kafka
4. ✅ Deliver via SSE to the frontend
5. ✅ Show in the notification bell 🔔

## Monitoring

Check Kafka consumer health:

```bash
# View backend logs
docker-compose logs -f backend

# Should see successful notifications like:
# "Notification sent to Kafka: userId=..., type=LOGIN"
# "Notification processed from Kafka: id=..."
# "SSE notification sent to user: ..."
```

## Related Files

- `backend/src/main/java/com/example/demo/domain/notification/Notification.java` - Domain model (fixed)
- `backend/src/main/java/com/example/demo/application/messaging/NotificationProducer.java` - Kafka producer
- `backend/src/main/java/com/example/demo/application/messaging/NotificationConsumer.java` - Kafka consumer
- `backend/src/main/java/com/example/demo/config/KafkaConfig.java` - Kafka configuration

## Summary

The Kafka deserialization error is now **fixed** by adding Jackson annotations to the immutable `Notification` domain class. You may need to reset Kafka topics to clear old messages. All new notifications will work perfectly! 🎉
