# GraphQL Migration Guide

This guide explains how to migrate from REST API to GraphQL in the Virtual Bank application.

## Overview

The application has been migrated to use GraphQL instead of REST for all API communication. GraphQL provides several advantages:

- **Single endpoint**: All operations go through `/graphql` instead of multiple REST endpoints
- **Flexible queries**: Request exactly the data you need, nothing more, nothing less
- **Type safety**: Strong typing with schema validation
- **Better tooling**: GraphiQL playground for testing queries at `http://localhost:8080/graphiql`

## Backend Changes

### 1. GraphQL Schema

The GraphQL schema is defined in `/backend/src/main/resources/graphql/schema.graphqls`. This schema includes:

- **Types**: User, Account, Transaction, TransactionCategory, Notification, etc.
- **Queries**: For fetching data (me, accounts, transactions, notifications, etc.)
- **Mutations**: For modifying data (signUp, login, deposit, withdraw, etc.)
- **Custom Scalars**: Date, DateTime, Decimal for proper data type handling

### 2. GraphQL Resolvers

Resolvers are located in `/backend/src/main/java/com/example/demo/adapter/in/graphql/`:

- **AuthResolver**: Authentication (signUp, login, logout, me)
- **BankingResolver**: Banking operations (accounts, transactions, deposits, withdrawals, transfers)
- **CategoryResolver**: Category management
- **NotificationResolver**: Notifications
- **AdminResolver**: Admin operations (user management)

### 3. Security Configuration

- GraphQL endpoint (`/graphql`) is publicly accessible
- Authentication is handled at the resolver level using `@PreAuthorize` annotations
- JWT tokens are passed in the `Authorization` header (same as REST)

### 4. Dependencies Added

```gradle
// GraphQL
implementation 'org.springframework.boot:spring-boot-starter-graphql'
implementation 'com.graphql-java:graphql-java-extended-scalars:21.0'
testImplementation 'org.springframework.graphql:spring-graphql-test'
```

## Frontend Changes

### 1. Dependencies Added

Both frontends now include Apollo Client:

```json
"apollo-angular": "^6.0.0",
"@apollo/client": "^3.9.0",
"graphql": "^16.8.1"
```

### 2. GraphQL Module Setup

Each frontend has a `GraphQLModule` that configures Apollo Client:

- **User Frontend**: `/frontend/src/app/graphql/graphql.module.ts`
- **Admin Frontend**: `/frontend-admin/src/app/graphql/graphql.module.ts`

The module automatically adds JWT tokens to GraphQL requests.

### 3. GraphQL Operations

Pre-defined queries and mutations:

- **User Frontend**: `/frontend/src/app/graphql/graphql.operations.ts`
- **Admin Frontend**: `/frontend-admin/src/app/graphql/graphql.operations.ts`

## Migration Steps

### Step 1: Install Dependencies

```bash
# User Frontend
cd frontend
npm install

# Admin Frontend
cd frontend-admin
npm install

# Backend (will download dependencies on next build)
cd backend
./gradlew build
```

### Step 2: Import GraphQLModule

In `app.module.ts` (or `app.config.ts` for standalone apps), import the GraphQL module:

```typescript
import { GraphQLModule } from './graphql/graphql.module';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  imports: [
    // ... other imports
    HttpClientModule,  // Required by Apollo
    GraphQLModule,
  ],
  // ...
})
export class AppModule { }
```

### Step 3: Migrate Services to Use GraphQL

Here's how to migrate each service from REST to GraphQL:

#### Example 1: Authentication Service (Before)

```typescript
// OLD REST approach
login(request: LoginRequest): Observable<ApiResponse<LoginResponse>> {
  return this.http.post<ApiResponse<LoginResponse>>(
    `${this.API_URL}/login`,
    request
  );
}
```

#### Example 1: Authentication Service (After)

```typescript
import { Apollo } from 'apollo-angular';
import { LOGIN } from '../graphql/graphql.operations';

// NEW GraphQL approach
constructor(private apollo: Apollo) {}

login(username: string, password: string): Observable<any> {
  return this.apollo.mutate({
    mutation: LOGIN,
    variables: {
      input: { username, password }
    }
  }).pipe(
    map(result => result.data)
  );
}
```

#### Example 2: Banking Service - Get Accounts (Before)

```typescript
// OLD REST approach
getAllAccounts(): Observable<ApiResponse<AccountResponse[]>> {
  return this.http.get<ApiResponse<AccountResponse[]>>(
    `${this.API_URL}/accounts`
  );
}
```

#### Example 2: Banking Service - Get Accounts (After)

```typescript
import { GET_ACCOUNTS } from '../graphql/graphql.operations';

// NEW GraphQL approach
getAllAccounts(): Observable<any> {
  return this.apollo.query({
    query: GET_ACCOUNTS
  }).pipe(
    map(result => result.data.accounts)
  );
}
```

#### Example 3: Banking Service - Deposit (Before)

```typescript
// OLD REST approach
deposit(accountId: string, request: TransactionRequest): Observable<ApiResponse<AccountResponse>> {
  return this.http.post<ApiResponse<AccountResponse>>(
    `${this.API_URL}/accounts/${accountId}/deposit`,
    request
  );
}
```

#### Example 3: Banking Service - Deposit (After)

```typescript
import { DEPOSIT } from '../graphql/graphql.operations';

// NEW GraphQL approach
deposit(accountId: string, amount: number, description: string, categoryId?: string): Observable<any> {
  return this.apollo.mutate({
    mutation: DEPOSIT,
    variables: {
      accountId,
      input: { amount, description, categoryId }
    }
  }).pipe(
    map(result => result.data.deposit)
  );
}
```

#### Example 4: Notification Service - Get Notifications with Pagination (Before)

```typescript
// OLD REST approach
getNotifications(page: number, size: number): Observable<ApiResponse<Page<NotificationResponse>>> {
  return this.http.get<ApiResponse<Page<NotificationResponse>>>(
    `${this.API_URL}?page=${page}&size=${size}`
  );
}
```

#### Example 4: Notification Service - Get Notifications with Pagination (After)

```typescript
import { GET_NOTIFICATIONS } from '../graphql/graphql.operations';

// NEW GraphQL approach
getNotifications(page: number, size: number): Observable<any> {
  return this.apollo.query({
    query: GET_NOTIFICATIONS,
    variables: { page, size }
  }).pipe(
    map(result => result.data.notifications)
  );
}
```

### Step 4: Update Components

Components that use services will continue to work with minimal changes:

```typescript
// Component code remains mostly the same
this.authService.login(username, password).subscribe({
  next: (response) => {
    // GraphQL returns data directly, no need to unwrap ApiResponse
    this.token = response.login.token;
    this.user = response.login.user;
  },
  error: (error) => {
    console.error('Login failed', error);
  }
});
```

## GraphQL Query Examples

### Query with Variables

```graphql
query GetAccount($id: ID!) {
  account(id: $id) {
    id
    accountNumber
    balance
    accountType
    status
  }
}
```

Variables:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Mutation Example

```graphql
mutation Deposit($accountId: ID!, $input: DepositInput!) {
  deposit(accountId: $accountId, input: $input) {
    id
    amount
    balanceAfter
    description
  }
}
```

Variables:
```json
{
  "accountId": "550e8400-e29b-41d4-a716-446655440000",
  "input": {
    "amount": 1000.00,
    "description": "Salary deposit",
    "categoryId": "123e4567-e89b-12d3-a456-426614174000"
  }
}
```

### Nested Query Example

```graphql
query GetAccountWithTransactions($id: ID!) {
  account(id: $id) {
    id
    accountNumber
    balance
    transactions {
      id
      type
      amount
      description
      category {
        name
        type
      }
    }
  }
}
```

## Testing with GraphiQL

1. Start the backend server: `./gradlew bootRun` (in `/backend`)
2. Open browser: `http://localhost:8080/graphiql`
3. Test queries and mutations with the interactive playground

Example test query:
```graphql
query {
  health {
    status
    timestamp
  }
}
```

## REST vs GraphQL Comparison

### REST Approach (Old)

Multiple endpoints, multiple requests:

```typescript
// Fetch account
this.http.get('/api/banking/accounts/123')

// Fetch transactions
this.http.get('/api/banking/accounts/123/transactions')

// Fetch categories
this.http.get('/api/categories')
```

### GraphQL Approach (New)

Single request, single endpoint:

```graphql
query {
  account(id: "123") {
    id
    balance
    transactions {
      id
      amount
      category {
        name
        type
      }
    }
  }
}
```

## Important Notes

### 1. Authentication

- JWT tokens work the same way
- Token is stored in `localStorage` as before
- Apollo automatically adds `Authorization: Bearer <token>` header

### 2. Error Handling

GraphQL errors are returned in the `errors` array:

```typescript
this.apollo.query({ query: GET_ACCOUNTS }).subscribe({
  next: (result) => {
    if (result.errors) {
      console.error('GraphQL errors:', result.errors);
    } else {
      this.accounts = result.data.accounts;
    }
  },
  error: (error) => {
    console.error('Network or server error:', error);
  }
});
```

### 3. Real-time Notifications (SSE)

Server-Sent Events (SSE) for real-time notifications continue to work via the REST endpoint `/api/notifications/stream`. This has NOT been migrated to GraphQL subscriptions yet.

If you want to migrate to GraphQL subscriptions, you'll need to:
1. Add WebSocket support to Spring GraphQL
2. Define subscription types in the schema
3. Update the frontend to use Apollo subscriptions

### 4. Backward Compatibility

- **All REST endpoints are still available** and functional
- You can migrate services one at a time
- Both REST and GraphQL work simultaneously

### 5. Cache Management

Apollo Client caches responses by default. To refresh data:

```typescript
// Disable cache for specific query
this.apollo.query({
  query: GET_ACCOUNTS,
  fetchPolicy: 'network-only'  // Bypass cache
});

// Or clear the entire cache
this.apollo.client.cache.reset();
```

## Common Migration Patterns

### Pattern 1: Simple GET Request

```typescript
// REST
this.http.get('/api/resource')

// GraphQL
this.apollo.query({ query: GET_RESOURCE })
```

### Pattern 2: POST with Body

```typescript
// REST
this.http.post('/api/resource', { data })

// GraphQL
this.apollo.mutate({
  mutation: CREATE_RESOURCE,
  variables: { input: data }
})
```

### Pattern 3: Pagination

```typescript
// REST
this.http.get(`/api/resource?page=${page}&size=${size}`)

// GraphQL
this.apollo.query({
  query: GET_RESOURCES,
  variables: { page, size }
})
```

## Next Steps

1. **Run npm install** in both frontend directories
2. **Import GraphQLModule** in app modules
3. **Migrate services** one at a time using the examples above
4. **Test with GraphiQL** to verify queries work correctly
5. **Update error handling** to work with GraphQL error format

## Support

For issues or questions:
- Check GraphiQL playground: `http://localhost:8080/graphiql`
- Review GraphQL schema: `/backend/src/main/resources/graphql/schema.graphqls`
- Check resolver implementations in `/backend/src/main/java/com/example/demo/adapter/in/graphql/`

## Additional Resources

- [Apollo Angular Documentation](https://apollo-angular.com/docs/)
- [GraphQL Documentation](https://graphql.org/learn/)
- [Spring for GraphQL Documentation](https://docs.spring.io/spring-graphql/reference/)
