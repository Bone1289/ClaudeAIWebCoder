# gRPC Integration Guide

## Table of Contents
1. [Introduction to gRPC](#introduction-to-grpc)
2. [Architecture Overview](#architecture-overview)
3. [Backend Implementation](#backend-implementation)
4. [Frontend Implementation](#frontend-implementation)
5. [Adding New Services](#adding-new-services)
6. [Testing gRPC Services](#testing-grpc-services)
7. [Troubleshooting](#troubleshooting)

---

## Introduction to gRPC

### What is gRPC?

**gRPC** (gRPC Remote Procedure Call) is a modern, high-performance RPC framework developed by Google. It uses **HTTP/2** for transport and **Protocol Buffers** (protobuf) for serialization.

### Key Benefits

- **Performance**: Binary serialization is faster and smaller than JSON
- **Type Safety**: Strongly-typed contracts defined in `.proto` files
- **Code Generation**: Auto-generate client and server code from proto files
- **Streaming**: Built-in support for bidirectional streaming
- **Cross-Platform**: Works across multiple languages and platforms

### gRPC vs REST

| Feature | REST | gRPC |
|---------|------|------|
| Protocol | HTTP/1.1 | HTTP/2 |
| Data Format | JSON (text) | Protocol Buffers (binary) |
| Payload Size | Larger | Smaller (30-50% reduction) |
| Speed | Slower | Faster (7-10x in some cases) |
| Browser Support | Native | Requires gRPC-Web proxy |
| Streaming | Limited (SSE) | Full bidirectional |

### What is gRPC-Web?

**gRPC-Web** is a JavaScript implementation of gRPC for browser clients. Since browsers don't support HTTP/2 in the way gRPC needs, gRPC-Web uses:
- HTTP/1.1 or HTTP/2 transport
- A proxy (Envoy) to translate between gRPC-Web and native gRPC
- JSON or binary protobuf encoding

---

## Architecture Overview

### System Architecture

```
┌─────────────┐
│   Browser   │
│  (Angular)  │
└──────┬──────┘
       │ HTTP/1.1 + JSON
       │
┌──────▼──────┐
│    nginx    │ (Port 80)
│   Reverse   │
│    Proxy    │
└──────┬──────┘
       │ /grpc → Envoy
       │
┌──────▼──────┐
│    Envoy    │ (Port 8080)
│    Proxy    │
│  - gRPC-Web │
│  - JSON→Binary │
└──────┬──────┘
       │ HTTP/2 + Binary Protobuf
       │
┌──────▼──────┐
│   Backend   │ (Port 9090)
│ Spring Boot │
│  gRPC Server│
└─────────────┘
```

### Request Flow

1. **Frontend (Angular)** makes HTTP POST request with JSON payload to `/grpc/ServiceName/MethodName`
2. **nginx** proxies the request to Envoy
3. **Envoy** performs two transformations:
   - Converts JSON to binary Protocol Buffers
   - Converts HTTP/1.1 to HTTP/2 with gRPC framing
4. **Backend (Spring Boot)** receives native gRPC request, processes it, and returns gRPC response
5. **Envoy** converts response back to JSON
6. **nginx** forwards response to frontend
7. **Frontend** receives JSON response

### Components

- **Proto Files** (`backend/src/main/proto/*.proto`) - Service contracts
- **gRPC Server** (Spring Boot on port 9090) - Backend services
- **Envoy Proxy** (Port 8080) - gRPC-Web to gRPC translator
- **nginx** (Port 80) - Frontend web server and reverse proxy
- **Angular Client** - Frontend application using gRPC-Web

---

## Backend Implementation

### 1. Protocol Buffer Definitions

Proto files define the service contracts. They use **Protocol Buffers version 3** (proto3).

**Example: `auth_service.proto`**

```protobuf
syntax = "proto3";

package com.example.demo.grpc;

option java_multiple_files = true;
option java_package = "com.example.demo.grpc.auth";

// Service definition
service AuthService {
  rpc Login(LoginRequest) returns (LoginResponse);
  rpc Register(RegisterRequest) returns (RegisterResponse);
}

// Request message
message LoginRequest {
  string email_or_username = 1;  // Field number 1
  string password = 2;            // Field number 2
}

// Response message
message LoginResponse {
  bool success = 1;
  string message = 2;
  string token = 3;
  UserResponse user = 4;
}

// Nested message
message UserResponse {
  string id = 1;
  string username = 2;
  string email = 3;
  string first_name = 4;
  string last_name = 5;
  string role = 6;
  string status = 7;
  bool account_non_locked = 8;
  string created_at = 9;
  string last_login = 10;
}
```

**Key Points:**
- Field names use **snake_case** (protobuf convention)
- Each field has a unique **number** (not reusable, even if deleted)
- Services define RPC methods: `rpc MethodName(RequestType) returns (ResponseType)`
- Use `import` to include other proto files

### 2. Maven Configuration

**In `pom.xml`:**

```xml
<dependencies>
    <!-- gRPC dependencies -->
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-netty-shaded</artifactId>
        <version>1.58.0</version>
    </dependency>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-protobuf</artifactId>
        <version>1.58.0</version>
    </dependency>
    <dependency>
        <groupId>io.grpc</groupId>
        <artifactId>grpc-stub</artifactId>
        <version>1.58.0</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Protobuf compiler plugin -->
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.24.0:exe:${os.detected.classifier}</protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.58.0:exe:${os.detected.classifier}</pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Run `mvn clean compile` to generate Java classes from proto files.

### 3. gRPC Service Implementation

**Example: `AuthServiceGrpc.java`**

```java
@GrpcService  // Spring Boot annotation for gRPC services
public class AuthServiceGrpc extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthenticationUseCase authenticationUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceGrpc(AuthenticationUseCase authenticationUseCase,
                          JwtTokenProvider jwtTokenProvider) {
        this.authenticationUseCase = authenticationUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            // 1. Extract data from gRPC request
            String emailOrUsername = request.getEmailOrUsername();
            String password = request.getPassword();

            // 2. Call business logic
            User user = authenticationUseCase.login(emailOrUsername, password);

            // 3. Generate JWT token
            String token = jwtTokenProvider.generateToken(user);

            // 4. Build gRPC response
            UserResponse userResponse = UserResponse.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setRole(user.getRole().name())
                .setStatus(user.getStatus().name())
                .setAccountNonLocked(user.isAccountNonLocked())
                .setCreatedAt(user.getCreatedAt().toString())
                .build();

            LoginResponse response = LoginResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Login successful")
                .setToken(token)
                .setUser(userResponse)
                .build();

            // 5. Send response and complete
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            // 6. Handle errors
            LoginResponse errorResponse = LoginResponse.newBuilder()
                .setSuccess(false)
                .setMessage(e.getMessage())
                .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
}
```

**Key Points:**
- Extend `{ServiceName}ImplBase` generated from proto file
- Use `@GrpcService` annotation for Spring Boot auto-configuration
- Methods receive a `StreamObserver` for sending responses
- Call `onNext()` to send response, `onCompleted()` to finish
- Use `onError()` for exceptions (or send error in response like above)

### 4. gRPC Server Configuration

**`application.properties`:**

```properties
# gRPC server configuration
grpc.server.port=9090
grpc.server.max-inbound-message-size=4194304
```

The gRPC server starts automatically when Spring Boot starts.

### 5. Authentication with gRPC

**JWT Interceptor:**

```java
@Component
public class JwtAuthenticationInterceptor implements ServerInterceptor {

    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Extract token from metadata
        String authHeader = headers.get(Metadata.Key.of(AUTHORIZATION_HEADER, ASCII_STRING_MARSHALLER));

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());

            // Validate and extract user from token
            User user = jwtTokenProvider.validateTokenAndGetUser(token);

            // Store in context for use in service methods
            Context context = Context.current()
                .withValue(USER_CONTEXT_KEY, user);

            return Contexts.interceptCall(context, call, headers, next);
        }

        return next.startCall(call, headers);
    }
}
```

**Access authenticated user in service:**

```java
@Override
public void getUserProfile(Empty request, StreamObserver<UserProfileResponse> responseObserver) {
    User currentUser = GrpcSecurityContext.getCurrentUser();

    if (currentUser == null) {
        responseObserver.onError(Status.UNAUTHENTICATED
            .withDescription("Authentication required")
            .asRuntimeException());
        return;
    }

    // Use currentUser...
}
```

---

## Frontend Implementation

### 1. Package Dependencies

**`package.json`:**

```json
{
  "dependencies": {
    "google-protobuf": "^3.21.2",
    "grpc-web": "^1.5.0"
  },
  "devDependencies": {
    "@types/google-protobuf": "^3.15.12",
    "grpc-tools": "^1.12.4",
    "grpc_tools_node_protoc_ts": "^5.3.3"
  }
}
```

### 2. gRPC Client Service

**`grpc-client.service.ts`:**

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GrpcClientService {
  private readonly GRPC_WEB_URL = '/grpc';  // Proxied by nginx
  private readonly TOKEN_KEY = 'auth_token';

  constructor(private http: HttpClient) { }

  /**
   * Make a gRPC call via JSON over HTTP
   * @param serviceName - Full service name (e.g., 'com.example.demo.grpc.AuthService')
   * @param methodName - Method name (e.g., 'Login')
   * @param request - Request object (will be sent as JSON)
   * @param requiresAuth - Whether to include JWT token
   */
  call<TRequest, TResponse>(
    serviceName: string,
    methodName: string,
    request: TRequest,
    requiresAuth: boolean = true
  ): Observable<TResponse> {
    const url = `${this.GRPC_WEB_URL}/${serviceName}/${methodName}`;
    const headers = this.buildHeaders(requiresAuth);

    return this.http.post<TResponse>(url, request, { headers });
  }

  private buildHeaders(requiresAuth: boolean): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    if (requiresAuth) {
      const token = localStorage.getItem(this.TOKEN_KEY);
      if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
      }
    }

    return headers;
  }
}
```

**Key Points:**
- URL pattern: `/grpc/{ServiceName}/{MethodName}`
- Send requests as JSON (Envoy converts to protobuf)
- Include JWT token in `Authorization` header for authenticated requests

### 3. Service Implementation

**`auth.service.ts`:**

```typescript
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { GrpcClientService } from '../grpc/grpc-client.service';

// gRPC request/response interfaces (snake_case to match proto)
interface GrpcLoginRequest {
  email_or_username: string;
  password: string;
}

interface GrpcLoginResponse {
  success: boolean;
  message: string;
  token: string;
  user: {
    id: string;
    username: string;
    email: string;
    first_name: string;
    last_name: string;
    role: string;
    status: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly SERVICE_NAME = 'com.example.demo.grpc.AuthService';

  constructor(private grpcClient: GrpcClientService) {}

  login(emailOrUsername: string, password: string): Observable<LoginResponse> {
    // Create gRPC request with snake_case field names
    const grpcRequest: GrpcLoginRequest = {
      email_or_username: emailOrUsername,
      password: password
    };

    return this.grpcClient.call<GrpcLoginRequest, GrpcLoginResponse>(
      this.SERVICE_NAME,
      'Login',
      grpcRequest,
      false  // No auth required for login
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        token: response.token,
        user: this.mapGrpcUserToModel(response.user)
      })),
      tap(response => {
        if (response.success) {
          localStorage.setItem('auth_token', response.token);
        }
      })
    );
  }

  // Convert snake_case gRPC response to camelCase Angular model
  private mapGrpcUserToModel(grpcUser: any): User {
    return {
      id: grpcUser.id,
      username: grpcUser.username,
      email: grpcUser.email,
      firstName: grpcUser.first_name,      // snake_case → camelCase
      lastName: grpcUser.last_name,
      role: grpcUser.role,
      status: grpcUser.status
    };
  }
}
```

**Key Points:**
- Use **snake_case** for gRPC field names (protobuf convention)
- Use **camelCase** for Angular models (TypeScript convention)
- Map between the two naming conventions
- Service name must match proto package + service name

### 4. nginx Configuration

**`nginx.conf`:**

```nginx
server {
    listen 80;

    # Proxy gRPC requests to Envoy
    location /grpc {
        rewrite ^/grpc/(.*) /$1 break;
        proxy_pass http://envoy:8080;
        proxy_http_version 1.1;

        proxy_set_header Content-Type application/grpc-web+proto;
        proxy_set_header Authorization $http_authorization;
        proxy_buffering off;

        add_header Access-Control-Allow-Origin * always;
    }

    # Serve Angular app
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 5. Envoy Configuration

**`envoy.yaml`:**

```yaml
static_resources:
  listeners:
    - name: listener_0
      address:
        socket_address:
          address: 0.0.0.0
          port_value: 8080
      filter_chains:
        - filters:
            - name: envoy.filters.network.http_connection_manager
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                codec_type: AUTO
                stat_prefix: ingress_http
                http_filters:
                  # JSON to Protobuf transcoding (MUST be before grpc_web)
                  - name: envoy.filters.http.grpc_json_transcoder
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_json_transcoder.v3.GrpcJsonTranscoder
                      proto_descriptor: "/etc/envoy/proto_descriptor.pb"
                      services:
                        - "com.example.demo.grpc.AuthService"
                        - "com.example.demo.grpc.BankingService"
                      preserve_proto_field_names: true  # Keep snake_case
                      auto_mapping: true
                      convert_grpc_status: true

                  # gRPC-Web filter
                  - name: envoy.filters.http.grpc_web
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_web.v3.GrpcWeb

                  # Router (MUST be last)
                  - name: envoy.filters.http.router
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router

                route_config:
                  name: local_route
                  virtual_hosts:
                    - name: backend
                      domains: ["*"]
                      routes:
                        - match:
                            prefix: "/"
                          route:
                            cluster: backend_grpc

  clusters:
    - name: backend_grpc
      connect_timeout: 5s
      type: LOGICAL_DNS
      lb_policy: ROUND_ROBIN
      dns_lookup_family: V4_ONLY
      typed_extension_protocol_options:
        envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
          "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
          explicit_http_config:
            http2_protocol_options: {}  # Use HTTP/2 for gRPC
      load_assignment:
        cluster_name: backend_grpc
        endpoints:
          - lb_endpoints:
              - endpoint:
                  address:
                    socket_address:
                      address: backend
                      port_value: 9090
```

**Key Points:**
- `grpc_json_transcoder` converts JSON ↔ Binary Protobuf
- `grpc_web` handles gRPC-Web protocol
- Filter order matters: transcoder → grpc_web → router
- `preserve_proto_field_names: true` keeps snake_case field names

---

## Adding New Services

### Step 1: Define Proto File

**`backend/src/main/proto/product_service.proto`:**

```protobuf
syntax = "proto3";

package com.example.demo.grpc;

option java_multiple_files = true;
option java_package = "com.example.demo.grpc.product";

import "common.proto";  // For shared types like Empty

service ProductService {
  rpc GetAllProducts(Empty) returns (GetAllProductsResponse);
  rpc GetProduct(IdRequest) returns (GetProductResponse);
  rpc CreateProduct(CreateProductRequest) returns (CreateProductResponse);
  rpc UpdateProduct(UpdateProductRequest) returns (UpdateProductResponse);
  rpc DeleteProduct(IdRequest) returns (DeleteProductResponse);
}

message CreateProductRequest {
  string name = 1;
  string description = 2;
  string price = 3;  // Use string for decimal values
  int32 stock = 4;
}

message ProductResponse {
  string id = 1;
  string name = 2;
  string description = 3;
  string price = 4;
  int32 stock = 5;
  string created_at = 6;
}

message GetAllProductsResponse {
  bool success = 1;
  string message = 2;
  repeated ProductResponse products = 3;  // repeated = array
}

message GetProductResponse {
  bool success = 1;
  string message = 2;
  ProductResponse product = 3;
}

message CreateProductResponse {
  bool success = 1;
  string message = 2;
  ProductResponse product = 3;
}

message UpdateProductRequest {
  string id = 1;
  string name = 2;
  string description = 3;
  string price = 4;
  int32 stock = 5;
}

message UpdateProductResponse {
  bool success = 1;
  string message = 2;
  ProductResponse product = 3;
}

message DeleteProductResponse {
  bool success = 1;
  string message = 2;
}
```

### Step 2: Generate Code

```bash
mvn clean compile
```

This generates:
- `ProductServiceGrpc.java` (service stub)
- `CreateProductRequest.java`
- `ProductResponse.java`
- All other message classes

### Step 3: Implement Backend Service

**`ProductServiceGrpc.java`:**

```java
package com.example.demo.adapter.in.grpc;

import com.example.demo.grpc.product.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ProductServiceGrpc extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductUseCase productUseCase;

    public ProductServiceGrpc(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @Override
    public void getAllProducts(Empty request, StreamObserver<GetAllProductsResponse> responseObserver) {
        try {
            List<Product> products = productUseCase.getAllProducts();

            List<ProductResponse> productResponses = products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

            GetAllProductsResponse response = GetAllProductsResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Products retrieved successfully")
                .addAllProducts(productResponses)  // addAll for repeated fields
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            GetAllProductsResponse response = GetAllProductsResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Error: " + e.getMessage())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<CreateProductResponse> responseObserver) {
        try {
            Product product = productUseCase.createProduct(
                request.getName(),
                request.getDescription(),
                new BigDecimal(request.getPrice()),  // Convert string to BigDecimal
                request.getStock()
            );

            CreateProductResponse response = CreateProductResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Product created successfully")
                .setProduct(mapToProductResponse(product))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            CreateProductResponse response = CreateProductResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Error: " + e.getMessage())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.newBuilder()
            .setId(product.getId())
            .setName(product.getName())
            .setDescription(product.getDescription())
            .setPrice(product.getPrice().toString())  // Convert BigDecimal to string
            .setStock(product.getStock())
            .setCreatedAt(product.getCreatedAt().toString())
            .build();
    }
}
```

### Step 4: Update Envoy Configuration

**`envoy.yaml`:**

```yaml
# Add new service to grpc_json_transcoder services list
services:
  - "com.example.demo.grpc.AuthService"
  - "com.example.demo.grpc.BankingService"
  - "com.example.demo.grpc.ProductService"  # Add this
```

### Step 5: Rebuild Envoy Image

```bash
docker-compose up --build envoy
```

### Step 6: Implement Frontend Service

**`product.service.ts`:**

```typescript
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { GrpcClientService } from '../grpc/grpc-client.service';

interface GrpcProductResponse {
  id: string;
  name: string;
  description: string;
  price: string;
  stock: number;
  created_at: string;
}

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stock: number;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly SERVICE_NAME = 'com.example.demo.grpc.ProductService';

  constructor(private grpcClient: GrpcClientService) {}

  getAllProducts(): Observable<Product[]> {
    return this.grpcClient.call<{}, any>(
      this.SERVICE_NAME,
      'GetAllProducts',
      {}
    ).pipe(
      map(response =>
        response.products.map((p: GrpcProductResponse) => this.mapToProduct(p))
      )
    );
  }

  createProduct(name: string, description: string, price: number, stock: number): Observable<Product> {
    const request = {
      name,
      description,
      price: price.toString(),  // Convert number to string
      stock
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'CreateProduct',
      request
    ).pipe(
      map(response => this.mapToProduct(response.product))
    );
  }

  private mapToProduct(grpcProduct: GrpcProductResponse): Product {
    return {
      id: grpcProduct.id,
      name: grpcProduct.name,
      description: grpcProduct.description,
      price: parseFloat(grpcProduct.price),  // Convert string to number
      stock: grpcProduct.stock,
      createdAt: grpcProduct.created_at
    };
  }
}
```

---

## Testing gRPC Services

### 1. Using grpcurl (Command Line)

Install grpcurl:
```bash
brew install grpcurl  # macOS
# or
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest
```

**List services:**
```bash
grpcurl -plaintext localhost:9090 list
```

**Describe service:**
```bash
grpcurl -plaintext localhost:9090 describe com.example.demo.grpc.AuthService
```

**Call method:**
```bash
grpcurl -plaintext \
  -d '{"email_or_username": "admin", "password": "Admin"}' \
  localhost:9090 \
  com.example.demo.grpc.AuthService/Login
```

**With authentication:**
```bash
grpcurl -plaintext \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  localhost:9090 \
  com.example.demo.grpc.BankingService/GetAllAccounts
```

### 2. Using Postman

Postman now supports gRPC!

1. Create new gRPC request
2. Enter server URL: `localhost:9090`
3. Select method from dropdown
4. Fill in request fields
5. Add metadata for auth: `authorization: Bearer TOKEN`
6. Click Invoke

### 3. Using curl (via Envoy/JSON)

Test through the JSON transcoding layer:

```bash
# Login
curl -X POST http://localhost:8080/com.example.demo.grpc.AuthService/Login \
  -H "Content-Type: application/json" \
  -d '{
    "email_or_username": "admin",
    "password": "Admin"
  }'

# Get accounts (with auth)
curl -X POST http://localhost:8080/com.example.demo.grpc.BankingService/GetAllAccounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{}'
```

### 4. Frontend Testing

Use browser DevTools Network tab:
1. Look for requests to `/grpc/...`
2. Check Request Payload (JSON)
3. Check Response (JSON)
4. Verify headers include Authorization

---

## Troubleshooting

### Common Issues

#### 1. "UNIMPLEMENTED: Method not found"

**Cause:** Service or method doesn't exist, or proto mismatch

**Solutions:**
- Verify service name matches proto exactly: `package.ServiceName`
- Ensure method name is correct (case-sensitive)
- Rebuild backend after proto changes: `mvn clean compile`
- Restart backend to load new service

#### 2. "UNAUTHENTICATED: Authentication required"

**Cause:** Missing or invalid JWT token

**Solutions:**
- Check Authorization header is set: `Bearer {token}`
- Verify token hasn't expired
- Check token is valid JWT format
- Ensure interceptor is registered

#### 3. "gRPC frame header malformed"

**Cause:** Backend receiving wrong format (JSON instead of binary)

**Solutions:**
- Ensure Envoy is running and configured correctly
- Check `grpc_json_transcoder` filter is in envoy.yaml
- Verify frontend sends to `/grpc/...` not directly to backend
- Rebuild Envoy with proto descriptor: `docker-compose up --build envoy`

#### 4. "Failed to load accounts: Http failure during parsing"

**Cause:** Response format mismatch or CORS issue

**Solutions:**
- Check nginx is proxying `/grpc` to Envoy
- Verify Envoy CORS headers are set
- Check response format matches expected TypeScript interface
- Look for field name mismatches (snake_case vs camelCase)

#### 5. "Type 'string' is not assignable to type 'USER' | 'ADMIN'"

**Cause:** TypeScript strict type checking

**Solution:**
```typescript
role: grpcUser.role as 'USER' | 'ADMIN'  // Type assertion
```

#### 6. Services not showing in Envoy

**Cause:** Proto descriptor not built or missing services

**Solutions:**
- Check `envoy.yaml` lists all services under `grpc_json_transcoder.services`
- Rebuild Envoy image: `docker-compose build envoy`
- Verify proto files copied to Envoy build context

### Debugging Tips

**1. Check Backend Logs:**
```bash
docker-compose logs -f backend
```

Look for:
- gRPC server started on port 9090
- Method calls and errors
- JWT validation errors

**2. Check Envoy Logs:**
```bash
docker-compose logs -f envoy
```

Look for:
- Transcoding errors
- Upstream connection errors
- Missing service definitions

**3. Check nginx Logs:**
```bash
docker-compose logs -f frontend  # or frontend-admin
```

**4. Enable gRPC Logging:**

Add to `application.properties`:
```properties
logging.level.io.grpc=DEBUG
logging.level.net.devh.boot.grpc=DEBUG
```

**5. Test Direct gRPC Connection:**

Bypass Envoy and nginx:
```bash
grpcurl -plaintext localhost:9090 list
```

If this works but browser doesn't, issue is in Envoy/nginx config.

### Performance Monitoring

**Check gRPC metrics:**

Backend exposes metrics on port 9091 (Prometheus):
```
http://localhost:9091/metrics
```

Look for:
- `grpc_server_handled_total` - Total requests
- `grpc_server_handling_seconds` - Latency
- `grpc_server_msg_received_total` - Messages received

---

## Best Practices

### 1. Proto Design

✅ **Do:**
- Use semantic versioning for proto files
- Never reuse field numbers, even if deleted
- Use reserved fields for deprecated fields
- Keep messages small and focused
- Use `repeated` for lists
- Use `string` for decimals and timestamps

❌ **Don't:**
- Change field types (breaks compatibility)
- Remove fields (mark as deprecated instead)
- Use `bytes` for large payloads (use streaming)

### 2. Error Handling

**Backend:**
```java
try {
    // Business logic
} catch (InvalidInputException e) {
    responseObserver.onError(Status.INVALID_ARGUMENT
        .withDescription(e.getMessage())
        .asRuntimeException());
} catch (NotFoundException e) {
    responseObserver.onError(Status.NOT_FOUND
        .withDescription(e.getMessage())
        .asRuntimeException());
} catch (Exception e) {
    responseObserver.onError(Status.INTERNAL
        .withDescription("Internal server error")
        .asRuntimeException());
}
```

**Frontend:**
```typescript
this.grpcClient.call(...).pipe(
  catchError(error => {
    console.error('gRPC error:', error);
    return throwError(() => new Error('Failed to fetch data'));
  })
)
```

### 3. Type Safety

Always define TypeScript interfaces for gRPC messages:

```typescript
// Good
interface GrpcLoginRequest {
  email_or_username: string;
  password: string;
}

const request: GrpcLoginRequest = {
  email_or_username: email,
  password: password
};

// Bad
const request = {
  email_or_username: email,
  password: password
};
```

### 4. Field Naming

- **Proto files:** snake_case (protobuf convention)
- **Java code:** camelCase (generated automatically)
- **TypeScript gRPC interfaces:** snake_case (match proto)
- **TypeScript models:** camelCase (Angular convention)

Map between conventions:
```typescript
private mapGrpcToModel(grpc: GrpcType): ModelType {
  return {
    firstName: grpc.first_name,  // snake_case → camelCase
    lastName: grpc.last_name
  };
}
```

### 5. Security

- Always validate JWT tokens in backend interceptor
- Use HTTPS in production
- Don't expose gRPC port (9090) publicly
- Rate limit gRPC endpoints
- Sanitize user input

---

## Summary

This application uses **gRPC** for high-performance, type-safe communication between backend and frontend:

**Flow:**
1. Frontend sends JSON to `/grpc/ServiceName/MethodName`
2. nginx proxies to Envoy
3. Envoy converts JSON → Binary Protobuf
4. Backend processes native gRPC request
5. Response flows back through Envoy (Binary → JSON)
6. Frontend receives JSON response

**Key Files:**
- Proto definitions: `backend/src/main/proto/*.proto`
- Backend services: `backend/src/main/java/.../adapter/in/grpc/`
- Frontend client: `frontend/src/app/grpc/grpc-client.service.ts`
- Frontend services: `frontend/src/app/services/*.service.ts`
- Envoy config: `envoy.yaml`
- nginx config: `frontend/nginx.conf`, `frontend-admin/nginx.conf`

**Benefits:**
- 🚀 7-10x faster than REST
- 📦 30-50% smaller payloads
- 🔒 Type-safe contracts
- ✅ Auto-generated code
- 🌐 Cross-platform support

For questions or issues, refer to the [Troubleshooting](#troubleshooting) section or check the official docs:
- gRPC: https://grpc.io/docs/
- gRPC-Web: https://github.com/grpc/grpc-web
- Protocol Buffers: https://protobuf.dev/
