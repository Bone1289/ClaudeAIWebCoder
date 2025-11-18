# gRPC Migration Guide

## Overview

This application has been migrated from REST to gRPC with gRPC-Web support for browser clients. This document provides comprehensive information about the migration and how to use the new gRPC APIs.

## Architecture

### Components

```
Browser Client (Angular/React/Vue)
    ↓ (gRPC-Web over HTTP/1.1)
Envoy Proxy (Port 8081)
    ↓ (gRPC over HTTP/2)
Spring Boot Backend (Port 9090)
```

### Ports

- **8080**: REST API (deprecated, will be removed in future versions)
- **8081**: gRPC-Web proxy (Envoy) - **Use this for browser clients**
- **9090**: gRPC server (native gRPC, for backend-to-backend communication)
- **9901**: Envoy admin interface

## gRPC Services

### 1. AuthService
**Package**: `com.example.demo.grpc.auth`

| Method | Description | Auth Required |
|--------|-------------|---------------|
| SignUp | Register a new user | No |
| Login | Authenticate and get JWT token | No |
| GetMe | Get current user profile | Yes |
| Logout | Logout user | Yes |

### 2. BankingService
**Package**: `com.example.demo.grpc.banking`

| Method | Description |
|--------|-------------|
| CreateAccount | Create a new bank account |
| GetAccount | Get account by ID |
| GetAllAccounts | Get all accounts for current user |
| UpdateAccount | Update account type |
| DeleteAccount | Delete account (must have zero balance) |
| Deposit | Deposit money |
| Withdraw | Withdraw money |
| Transfer | Transfer money between accounts |
| GetAccountTransactions | Get transaction history |
| GetAllTransactions | Get all transactions for user |
| GetAccountStatement | Generate account statement |
| GetCategoryReport | Generate category spending report |

### 3. NotificationService
**Package**: `com.example.demo.grpc.notification`

| Method | Description | Type |
|--------|-------------|------|
| CreateNotification | Create notification | Unary |
| GetNotification | Get notification by ID | Unary |
| GetAllNotifications | Get paginated notifications | Unary |
| GetUnreadNotifications | Get unread notifications | Unary |
| GetUnreadCount | Get unread count | Unary |
| MarkAsRead | Mark notification as read | Unary |
| MarkAllAsRead | Mark all as read | Unary |
| DeleteNotification | Delete notification | Unary |
| **StreamNotifications** | Real-time notification stream | **Server Streaming** |

### 4. CategoryService
**Package**: `com.example.demo.grpc.category`

| Method | Description |
|--------|-------------|
| CreateCategory | Create transaction category |
| GetCategory | Get category by ID |
| GetAllCategories | Get all categories (with filters) |
| UpdateCategory | Update category |
| DeleteCategory | Delete category |
| ActivateCategory | Activate category |
| DeactivateCategory | Deactivate category |

### 5. AdminService
**Package**: `com.example.demo.grpc.admin`

| Method | Description | Role Required |
|--------|-------------|---------------|
| GetAllUsers | Get all users | ADMIN |
| GetUser | Get user by ID | ADMIN |
| SuspendUser | Suspend user | ADMIN |
| ActivateUser | Activate user | ADMIN |
| LockUser | Lock user | ADMIN |
| DeleteUser | Delete user | ADMIN |
| GetAllAdminAccounts | Get all accounts | ADMIN |
| GetAdminAccount | Get account by ID | ADMIN |
| GetUserAccounts | Get accounts for a user | ADMIN |

## Browser Client Setup

### Prerequisites

```bash
npm install google-protobuf grpc-web
npm install --save-dev @types/google-protobuf
```

### Generate JavaScript/TypeScript Client Code

You have two options:

#### Option 1: Using protoc (Recommended)

```bash
# Install protoc compiler
# macOS: brew install protobuf
# Linux: apt-get install protobuf-compiler

# Install protoc-gen-grpc-web plugin
npm install -g protoc-gen-grpc-web

# Generate code
protoc -I=backend/src/main/proto \
  --js_out=import_style=commonjs:frontend/src/generated \
  --grpc-web_out=import_style=typescript,mode=grpcwebtext:frontend/src/generated \
  backend/src/main/proto/*.proto
```

#### Option 2: Using Docker

```bash
docker run --rm -v $(pwd):/workspace \
  namely/protoc-all:1.51_0 \
  -f backend/src/main/proto/*.proto \
  -l web \
  -o frontend/src/generated
```

### Angular Example

#### 1. Install Dependencies

```bash
npm install google-protobuf grpc-web
npm install --save-dev @types/google-protobuf
```

#### 2. Create gRPC Service

```typescript
// src/app/services/auth-grpc.service.ts
import { Injectable } from '@angular/core';
import { AuthServiceClient } from '../../generated/auth_service_grpc_web_pb';
import { LoginRequest, LoginResponse } from '../../generated/auth_service_pb';

@Injectable({
  providedIn: 'root'
})
export class AuthGrpcService {
  private client: AuthServiceClient;

  constructor() {
    // Connect to Envoy proxy
    this.client = new AuthServiceClient('http://localhost:8081');
  }

  async login(emailOrUsername: string, password: string): Promise<LoginResponse> {
    const request = new LoginRequest();
    request.setEmailOrUsername(emailOrUsername);
    request.setPassword(password);

    return new Promise((resolve, reject) => {
      this.client.login(request, {}, (err, response) => {
        if (err) {
          reject(err);
        } else {
          resolve(response);
        }
      });
    });
  }
}
```

#### 3. Use in Component

```typescript
// src/app/components/login/login.component.ts
import { Component } from '@angular/core';
import { AuthGrpcService } from '../../services/auth-grpc.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  constructor(private authService: AuthGrpcService) {}

  async onLogin(username: string, password: string) {
    try {
      const response = await this.authService.login(username, password);

      if (response.getSuccess()) {
        const token = response.getToken();
        const user = response.getUser();

        // Store token
        localStorage.setItem('jwt_token', token);

        console.log('Login successful:', user.getUsername());
      }
    } catch (error) {
      console.error('Login failed:', error);
    }
  }
}
```

#### 4. Add JWT Token to Requests

```typescript
// src/app/services/grpc-interceptor.service.ts
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GrpcInterceptorService {
  getMetadata(): { [key: string]: string } {
    const token = localStorage.getItem('jwt_token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }
}

// Usage in service
async getMe() {
  const request = new Empty();
  const metadata = this.interceptor.getMetadata();

  return new Promise((resolve, reject) => {
    this.client.getMe(request, metadata, (err, response) => {
      if (err) reject(err);
      else resolve(response);
    });
  });
}
```

### React Example

```typescript
// src/services/AuthService.ts
import { AuthServiceClient } from '../generated/auth_service_grpc_web_pb';
import { LoginRequest } from '../generated/auth_service_pb';

const client = new AuthServiceClient('http://localhost:8081');

export const login = async (username: string, password: string) => {
  const request = new LoginRequest();
  request.setEmailOrUsername(username);
  request.setPassword(password);

  return new Promise((resolve, reject) => {
    client.login(request, {}, (err, response) => {
      if (err) {
        reject(err);
      } else {
        resolve(response);
      }
    });
  });
};

// Usage in component
import React, { useState } from 'react';
import { login } from './services/AuthService';

function LoginForm() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await login(username, password);
      const token = response.getToken();
      localStorage.setItem('jwt_token', token);
      console.log('Login successful!');
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input value={username} onChange={(e) => setUsername(e.target.value)} />
      <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
      <button type="submit">Login</button>
    </form>
  );
}
```

### Server-Side Streaming (Notifications)

```typescript
// Real-time notification streaming
import { NotificationServiceClient } from '../generated/notification_service_grpc_web_pb';
import { Empty } from '../generated/common_pb';

const client = new NotificationServiceClient('http://localhost:8081');

function subscribeToNotifications() {
  const request = new Empty();
  const metadata = { 'Authorization': `Bearer ${getToken()}` };

  const stream = client.streamNotifications(request, metadata);

  stream.on('data', (response) => {
    const notification = response.getNotification();
    const eventType = response.getEventType(); // NEW, UPDATED, DELETED

    console.log(`New notification (${eventType}):`, notification.getTitle());

    // Update UI with new notification
    showNotification(notification);
  });

  stream.on('error', (err) => {
    console.error('Stream error:', err);
  });

  stream.on('end', () => {
    console.log('Stream ended');
    // Reconnect logic here
  });

  return stream;
}

// Usage
const notificationStream = subscribeToNotifications();

// Cleanup on component unmount
notificationStream.cancel();
```

## Error Handling

gRPC uses status codes instead of HTTP status codes:

| gRPC Status | Meaning | HTTP Equivalent |
|-------------|---------|-----------------|
| OK | Success | 200 |
| INVALID_ARGUMENT | Validation error | 400 |
| UNAUTHENTICATED | Auth required | 401 |
| PERMISSION_DENIED | Access denied | 403 |
| NOT_FOUND | Resource not found | 404 |
| FAILED_PRECONDITION | Business rule violation | 412 |
| INTERNAL | Server error | 500 |

### Error Handling Example

```typescript
try {
  const response = await createAccount(accountData);
  console.log('Account created:', response.getAccount());
} catch (error: any) {
  switch (error.code) {
    case grpc.Code.INVALID_ARGUMENT:
      alert('Invalid input: ' + error.message);
      break;
    case grpc.Code.UNAUTHENTICATED:
      // Redirect to login
      router.push('/login');
      break;
    case grpc.Code.PERMISSION_DENIED:
      alert('Access denied');
      break;
    case grpc.Code.FAILED_PRECONDITION:
      alert('Business rule violation: ' + error.message);
      break;
    default:
      alert('An error occurred: ' + error.message);
  }
}
```

## Testing with grpcurl

```bash
# Install grpcurl
brew install grpcurl  # macOS
# or download from https://github.com/fullstorydev/grpcurl

# List services
grpcurl -plaintext localhost:9090 list

# Describe a service
grpcurl -plaintext localhost:9090 describe com.example.demo.grpc.AuthService

# Call a method (without auth)
grpcurl -plaintext -d '{
  "email": "user@example.com",
  "username": "testuser",
  "password": "password123",
  "first_name": "Test",
  "last_name": "User"
}' localhost:9090 com.example.demo.grpc.AuthService/SignUp

# Call a method (with auth)
grpcurl -plaintext \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  localhost:9090 com.example.demo.grpc.BankingService/GetAllAccounts
```

## Performance Benefits

### REST vs gRPC Comparison

| Feature | REST | gRPC |
|---------|------|------|
| Protocol | HTTP/1.1 | HTTP/2 |
| Format | JSON | Protocol Buffers (binary) |
| Payload Size | ~100% | ~30% (70% reduction) |
| Streaming | SSE (one-way) | Bidirectional |
| Type Safety | Runtime | Compile-time |
| Code Generation | Manual | Automatic |

### Example Size Comparison

**REST JSON Response** (243 bytes):
```json
{
  "success": true,
  "message": "Account created",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "accountNumber": "1234567890",
    "balance": "1000.00"
  }
}
```

**gRPC Protobuf** (~70 bytes binary):
- 70% smaller payload
- Faster serialization/deserialization
- Lower bandwidth usage

## Migration Checklist for Frontend Teams

- [ ] Install gRPC-Web dependencies
- [ ] Generate client code from .proto files
- [ ] Update API service to use gRPC clients
- [ ] Update environment config to point to Envoy (port 8081)
- [ ] Add JWT token to gRPC metadata
- [ ] Implement error handling for gRPC status codes
- [ ] Replace SSE with gRPC streaming for notifications
- [ ] Test all API endpoints
- [ ] Update API documentation
- [ ] Remove REST API calls (after testing)

## Troubleshooting

### CORS Issues

If you encounter CORS errors, ensure Envoy is running and configured correctly. The Envoy config in `envoy.yaml` handles CORS.

### Connection Refused

- Ensure backend is running on port 9090
- Ensure Envoy is running on port 8081
- Check Docker logs: `docker logs virtualbank-envoy`

### Authentication Errors

- Ensure JWT token is included in metadata: `{ 'Authorization': 'Bearer TOKEN' }`
- Check token expiration
- Verify token is valid

### Streaming Not Working

- gRPC-Web streaming requires Envoy proxy
- Ensure you're connecting to Envoy (8081), not directly to backend (9090)
- Check browser console for stream errors

## Additional Resources

- [gRPC-Web Documentation](https://github.com/grpc/grpc-web)
- [Protocol Buffers Guide](https://developers.google.com/protocol-buffers)
- [Envoy Proxy Documentation](https://www.envoyproxy.io/docs)
- [grpcurl Documentation](https://github.com/fullstorydev/grpcurl)

## Support

For issues or questions, please open a GitHub issue or contact the backend team.
