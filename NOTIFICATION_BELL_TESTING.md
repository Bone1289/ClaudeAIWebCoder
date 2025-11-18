# Testing Real-Time Notification Bell Updates

## Issue
Notification bell badge number not incrementing when deposit/transaction events occur.

## Root Cause Fixed
The frontend was making a **redundant HTTP call** (`refreshUnreadCount()`) which created a race condition - it was fetching the count BEFORE the notification was saved to the database.

## Solution
Rely solely on the backend's **SSE 'unread-count' event**, which is sent AFTER the notification is saved.

## Backend Flow (Correct)
```
1. NotificationConsumer receives notification from Kafka
2. Saves notification to database
3. Sends 'notification' SSE event (the notification data)
4. Queries unread count from database
5. Sends 'unread-count' SSE event (the updated count)
```

## How to Test

### 1. Rebuild and Start
```bash
# Reset Kafka to clear old messages
./reset-kafka-topics.sh

# OR manually
docker-compose down
docker volume rm ClaudeAIWebCoder_kafka_data
docker-compose up --build -d
```

### 2. Open Browser Console
Open DevTools Console (F12) to see the logs

### 3. Login
Login to the application and watch for:
```
✅ Connected (SSE connection established)
📩 SSE notification event received: {title: "New Login Detected", ...}
🔢 SSE unread-count event received: 1
🔔 Notification bell: Unread count updated from 0 to 1
```

### 4. Make a Deposit
Go to Banking Dashboard → Make a deposit

**Expected Console Output:**
```
📩 SSE notification event received: {title: "Deposit Successful", ...}
🔢 SSE unread-count event received: 2
🔔 Notification bell: Unread count updated from 1 to 2
```

**Expected Visual:**
- Bell badge changes from `(1)` to `(2)` **immediately**
- No page refresh needed

### 5. Test Multiple Transactions
Make withdrawal, then transfer

**After withdrawal:**
```
📩 SSE notification event received: {title: "Withdrawal Successful", ...}
🔢 SSE unread-count event received: 3
🔔 Notification bell: Unread count updated from 2 to 3
```

**After transfer (you get TWO notifications if both accounts are yours):**
```
📩 SSE notification event received: {title: "Transfer Sent", ...}
🔢 SSE unread-count event received: 4
🔔 Notification bell: Unread count updated from 3 to 4

📩 SSE notification event received: {title: "Transfer Received", ...}
🔢 SSE unread-count event received: 5
🔔 Notification bell: Unread count updated from 4 to 5
```

## Debugging

### If Badge Doesn't Update

**1. Check SSE Connection**
Look for this in console when you login:
```
SSE connected: SSE connection established
```

If missing:
- Check token is stored: `localStorage.getItem('auth_token')`
- Check SSE endpoint: DevTools Network tab → look for `notifications/stream` (EventStream)

**2. Check Backend Logs**
```bash
docker-compose logs -f backend | grep -i "notification"
```

You should see:
```
Notification sent to Kafka: userId=..., type=TRANSACTION_DEPOSIT
Consuming notification from Kafka: id=..., userId=..., type=TRANSACTION_DEPOSIT
Notification saved to database: id=...
Notification pushed via SSE: id=..., userId=...
Sending unread count via SSE to user: ..., count: 2
```

**3. Check Kafka Consumer**
```bash
docker-compose logs -f backend | grep -i "kafka"
```

Look for:
```
Notification processed from Kafka: id=...
```

If you see serialization errors, run:
```bash
./reset-kafka-topics.sh
```

### If SSE Events Not Received

**Check if EventSource is connected:**
```javascript
// In browser console
localStorage.getItem('auth_token')  // Should show token
```

**Check Network tab:**
- Look for `notifications/stream` request
- Should show as `EventStream` type
- Should stay open (pending)
- Should show events in Messages tab

### Common Issues

**Issue: "No auth token found, cannot connect to SSE"**
- Solution: Make sure you're logged in
- Check: `localStorage.getItem('auth_token')` should return a token

**Issue: SSE connects but no events received**
- Check backend logs for errors
- Verify Kafka is running: `docker-compose ps kafka`
- Check Kafka consumer is working: `docker-compose logs backend | grep -i consumer`

**Issue: Events received but badge doesn't change**
- Check console for the 🔔 log showing update
- Verify Angular change detection is running (try clicking somewhere)
- Check if notification bell component is mounted

## Expected Behavior Summary

✅ **Badge increments immediately** when notification arrives
✅ **No HTTP polling** - pure push via SSE
✅ **Console shows all events** with emoji indicators
✅ **Works across tabs** - all tabs update simultaneously
✅ **Survives network issues** - auto-reconnects up to 5 times

## Performance

- **SSE overhead**: ~1-2KB per connection (minimal)
- **Heartbeat**: Every 30 seconds to keep connection alive
- **Timeout**: 30 minutes of inactivity
- **Reconnection**: Automatic with exponential backoff

## Success Criteria

After fixing, you should see:
1. ✅ Console logs show both notification AND unread-count events
2. ✅ Badge number increments immediately (no delay)
3. ✅ Badge shows correct count matching notifications
4. ✅ Works for login, deposits, withdrawals, transfers
5. ✅ No race conditions or incorrect counts
