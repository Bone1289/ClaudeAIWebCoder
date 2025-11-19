# GraphQL Integration Guide

## Table of Contents
1. [What is GraphQL?](#what-is-graphql)
2. [Why GraphQL Instead of REST?](#why-graphql-instead-of-rest)
3. [Backend: Spring Boot GraphQL Setup](#backend-spring-boot-graphql-setup)
4. [Frontend: Angular Apollo Client Setup](#frontend-angular-apollo-client-setup)
5. [Writing GraphQL Operations](#writing-graphql-operations)
6. [Adding New GraphQL Operations](#adding-new-graphql-operations)
7. [Common Patterns](#common-patterns)
8. [Troubleshooting](#troubleshooting)

---

## What is GraphQL?

GraphQL is a **query language for APIs** and a **runtime for executing those queries**. Unlike REST where you have multiple endpoints, GraphQL has a **single endpoint** and clients specify exactly what data they need.

### Key Concepts

**1. Schema** - Defines your API structure (types, queries, mutations)
```graphql
type User {
  id: ID!
  email: String!
  username: String!
  role: String!
}

type Query {
  me: User!
  getAllUsers: [User!]!
}

type Mutation {
  login(input: LoginInput!): AuthResponse!
}
```

**2. Query** - Read data (like GET in REST)
```graphql
query GetCurrentUser {
  me {
    id
    email
    username
  }
}
```

**3. Mutation** - Modify data (like POST/PUT/DELETE in REST)
```graphql
mutation CreateAccount($input: CreateAccountInput!) {
  createAccount(input: $input) {
    id
    accountNumber
    balance
  }
}
```

**4. Resolver** - Backend function that handles each field/operation

---

## Why GraphQL Instead of REST?

| Feature | REST | GraphQL |
|---------|------|---------|
| **Endpoints** | Multiple (`/users`, `/posts`, `/comments`) | Single (`/graphql`) |
| **Data Fetching** | Fixed response structure (over-fetching) | Client specifies exactly what it needs |
| **Multiple Resources** | Multiple requests | Single request with nested data |
| **Versioning** | `/api/v1/`, `/api/v2/` | Schema evolution (deprecation) |
| **Type Safety** | Optional (OpenAPI/Swagger) | Built-in with schema |

### Example: Fetching User with Posts

**REST** (Over-fetching):
```
GET /api/users/123        → Returns ALL user fields
GET /api/users/123/posts  → Returns ALL post fields
```

**GraphQL** (Exact data):
```graphql
query {
  user(id: "123") {
    username      # Only what you need
    posts {
      title       # Only what you need
    }
  }
}
```

---

## Backend: Spring Boot GraphQL Setup

### 1. Project Structure

```
backend/
├── src/main/resources/graphql/
│   └── schema.graphqls              # GraphQL schema definition
├── adapter/in/graphql/
│   ├── GraphQLConfig.java           # Custom scalars configuration
│   ├── AuthResolver.java            # Authentication operations
│   ├── BankingResolver.java         # Banking operations
│   ├── CategoryResolver.java        # Category operations
│   ├── NotificationResolver.java    # Notification operations
│   └── dto/                         # Data Transfer Objects
└── application/
    └── ports/in/                    # Use cases (business logic)
```

### 2. Schema Definition (`schema.graphqls`)

Your GraphQL schema defines the API contract:

```graphql
# Custom Scalars
scalar Date
scalar DateTime
scalar Decimal

# Types
type User {
    id: ID!
    email: String!
    username: String!
    firstName: String
    lastName: String
    role: String!
    createdAt: DateTime!
}

type Account {
    id: ID!
    userId: ID!
    accountNumber: String!
    firstName: String!
    lastName: String!
    nationality: String
    accountType: String!
    balance: Decimal!
    status: String!
    createdAt: DateTime!
    updatedAt: DateTime!
}

# Input Types (for mutations)
input CreateAccountInput {
    firstName: String!
    lastName: String!
    nationality: String
    accountType: String!
}

input LoginInput {
    username: String!
    password: String!
}

# Queries (Read operations)
type Query {
    # Authentication
    me: User!
    hello(name: String!): String!

    # Banking
    accounts: [Account!]!
    account(id: ID!): Account

    # Categories
    categories(activeOnly: Boolean, type: String): [Category!]!
}

# Mutations (Write operations)
type Mutation {
    # Authentication
    signUp(input: SignUpInput!): AuthResponse!
    login(input: LoginInput!): AuthResponse!
    logout: Boolean!

    # Banking
    createAccount(input: CreateAccountInput!): Account!
    deposit(accountId: ID!, input: DepositInput!): Transaction!
    withdraw(accountId: ID!, input: WithdrawInput!): Transaction!
    transfer(fromAccountId: ID!, input: TransferInput!): Transaction!
}

# Response Types
type AuthResponse {
    token: String!
    user: User!
}
```

### 3. Resolvers (Controllers)

Resolvers handle GraphQL operations. They map to your schema's queries and mutations.

**Example: `AuthResolver.java`**

```java
@Controller
public class AuthResolver {

    private final LoginUseCase loginUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Query: me
     * Returns currently authenticated user
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public User me() {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = getCurrentUserUseCase.getCurrentUser(userId);
        return user;
    }

    /**
     * Mutation: login
     * Authenticates user and returns token
     */
    @MutationMapping
    public AuthResponse login(@Argument("input") LoginInput input) {
        // Call business logic (use case)
        LoginResult result = loginUseCase.login(
            input.getUsername(),
            input.getPassword()
        );

        // Generate JWT token
        String token = jwtTokenProvider.createToken(
            result.getUser().getId().toString(),
            result.getUser().getRole()
        );

        // Return response
        return new AuthResponse(token, result.getUser());
    }

    /**
     * Mutation: logout
     * Logs out current user (for audit purposes)
     */
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean logout() {
        UUID userId = SecurityUtil.getCurrentUserId();
        // Log audit event
        auditService.logSuccess(AuditAction.LOGOUT, userId, ...);
        return true;
    }
}
```

**Key Annotations:**
- `@Controller` - Marks class as GraphQL controller
- `@QueryMapping` - Maps method to a Query in schema
- `@MutationMapping` - Maps method to a Mutation in schema
- `@Argument("name")` - Maps parameter to GraphQL argument
- `@PreAuthorize` - Spring Security authorization

### 4. Custom Scalars

GraphQL has basic scalars (String, Int, Boolean, ID), but you need custom ones for dates and decimals.

**`GraphQLConfig.java`**

```java
@Configuration
public class GraphQLConfig {

    @Bean
    @Order(0) // Highest precedence
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.Date)           // For java.time.LocalDate
                .scalar(createLocalDateTimeScalar())    // For java.time.LocalDateTime
                .scalar(GraphQLScalarType.newScalar()
                        .name("Decimal")
                        .coercing(ExtendedScalars.GraphQLBigDecimal.getCoercing())
                        .build());
    }

    private GraphQLScalarType createLocalDateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("A custom scalar that handles LocalDateTime")
                .coercing(new Coercing<LocalDateTime, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult == null) return null;
                        if (dataFetcherResult instanceof LocalDateTime) {
                            return ((LocalDateTime) dataFetcherResult)
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        throw new CoercingSerializeException("Expected LocalDateTime");
                    }

                    @Override
                    public LocalDateTime parseValue(Object input) {
                        if (input == null) return null;
                        if (input instanceof String) {
                            return LocalDateTime.parse((String) input);
                        }
                        throw new CoercingParseValueException("Expected String");
                    }

                    @Override
                    public LocalDateTime parseLiteral(Object input) {
                        if (input == null) return null;
                        if (input instanceof StringValue) {
                            return LocalDateTime.parse(((StringValue) input).getValue());
                        }
                        throw new CoercingParseLiteralException("Expected StringValue");
                    }
                })
                .build();
    }
}
```

**Why Custom Scalars?**
- `DateTime` - Your entities use `LocalDateTime`, but default GraphQL DateTime expects `OffsetDateTime`
- `Decimal` - For precise financial calculations (`BigDecimal` in Java)
- `Date` - For date-only fields (`LocalDate` in Java)

### 5. Authentication & Security

**JWT Authentication with GraphQL:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/graphql").permitAll()  // Allow all to GraphQL
                .requestMatchers("/graphiql").permitAll() // GraphiQL UI
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                           UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

**In Resolvers:**
```java
@QueryMapping
@PreAuthorize("isAuthenticated()")  // Requires authentication
public List<Account> accounts() {
    UUID userId = SecurityUtil.getCurrentUserId();
    return bankingUseCase.getAllAccounts(userId);
}

@MutationMapping
@PreAuthorize("hasRole('ADMIN')")  // Requires ADMIN role
public Boolean deleteUser(@Argument String userId) {
    adminUseCase.deleteUser(UUID.fromString(userId));
    return true;
}
```

---

## Frontend: Angular Apollo Client Setup

### 1. Installation & Configuration

**Dependencies** (`package.json`):
```json
{
  "dependencies": {
    "apollo-angular": "^7.0.2",
    "@apollo/client": "^3.10.8",
    "graphql": "^16.9.0"
  }
}
```

**GraphQL Module** (`graphql.module.ts`):

```typescript
import { NgModule } from '@angular/core';
import { APOLLO_OPTIONS, ApolloModule } from 'apollo-angular';
import { ApolloClientOptions, InMemoryCache } from '@apollo/client/core';
import { HttpLink } from 'apollo-angular/http';
import { setContext } from '@apollo/client/link/context';
import { environment } from '../environments/environment';

const uri = environment.apiUrl + '/graphql';

export function createApollo(httpLink: HttpLink): ApolloClientOptions<any> {
  // Authentication link - adds JWT token to headers
  const auth = setContext((operation, context) => {
    const token = localStorage.getItem('auth_token');
    if (token === null) {
      return {};
    }

    return {
      headers: {
        Authorization: `Bearer ${token}`
      }
    };
  });

  return {
    link: auth.concat(httpLink.create({ uri })),
    cache: new InMemoryCache(),
  };
}

@NgModule({
  imports: [ApolloModule],
  providers: [
    {
      provide: APOLLO_OPTIONS,
      useFactory: createApollo,
      deps: [HttpLink],
    },
  ],
})
export class GraphQLModule {}
```

**Key Parts:**
- `HttpLink` - Handles HTTP requests to GraphQL endpoint
- `setContext` - Middleware to add JWT token to every request
- `InMemoryCache` - Apollo's caching system
- `Authorization: Bearer ${token}` - JWT authentication header

### 2. GraphQL Operations (`graphql.operations.ts`)

Define your queries and mutations in one place:

```typescript
import { gql } from 'apollo-angular';

// ==================== Authentication ====================

export const LOGIN = gql`
  mutation Login($input: LoginInput!) {
    login(input: $input) {
      token
      user {
        id
        email
        username
        firstName
        lastName
        role
        createdAt
      }
    }
  }
`;

export const SIGN_UP = gql`
  mutation SignUp($input: SignUpInput!) {
    signUp(input: $input) {
      token
      user {
        id
        email
        username
        firstName
        lastName
        role
        createdAt
      }
    }
  }
`;

export const GET_CURRENT_USER = gql`
  query GetCurrentUser {
    me {
      id
      email
      username
      firstName
      lastName
      role
      createdAt
    }
  }
`;

export const LOGOUT = gql`
  mutation Logout {
    logout
  }
`;

// ==================== Banking ====================

export const GET_ACCOUNTS = gql`
  query GetAccounts {
    accounts {
      id
      userId
      accountNumber
      firstName
      lastName
      nationality
      accountType
      balance
      status
      createdAt
      updatedAt
    }
  }
`;

export const CREATE_ACCOUNT = gql`
  mutation CreateAccount($input: CreateAccountInput!) {
    createAccount(input: $input) {
      id
      userId
      accountNumber
      firstName
      lastName
      nationality
      accountType
      balance
      status
      createdAt
    }
  }
`;

export const DEPOSIT = gql`
  mutation Deposit($accountId: ID!, $input: DepositInput!) {
    deposit(accountId: $accountId, input: $input) {
      id
      accountId
      type
      amount
      balance
      description
      categoryId
      createdAt
    }
  }
`;
```

**Template Tag `gql`:**
- Parses GraphQL query string
- Provides syntax highlighting in IDEs
- Enables query validation

### 3. Angular Services

Services wrap Apollo Client and provide typed methods:

**`auth.service.ts`**

```typescript
import { Injectable, Injector } from '@angular/core';
import { BehaviorSubject, Observable, map } from 'rxjs';
import { Apollo } from 'apollo-angular';
import { LOGIN, SIGN_UP, GET_CURRENT_USER, LOGOUT } from '../graphql/graphql.operations';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'current_user';

  private currentUserSubject = new BehaviorSubject<UserResponse | null>(
    this.getCurrentUserFromStorage()
  );
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private apollo: Apollo, private injector: Injector) {}

  /**
   * Safely extract data from GraphQL result
   */
  private extractData<T>(result: any, key: string): T {
    if (!result || !result.data) {
      return null as any;
    }
    return result.data[key];
  }

  /**
   * Login with credentials
   */
  login(request: LoginRequest): Observable<any> {
    return this.apollo.mutate({
      mutation: LOGIN,
      variables: {
        input: {
          username: request.emailOrUsername,
          password: request.password
        }
      }
    }).pipe(
      map(result => {
        const authData = this.extractData<any>(result, 'login');

        // Store token and user
        this.setToken(authData.token);
        this.setCurrentUser(authData.user);
        this.currentUserSubject.next(authData.user);

        return authData;
      })
    );
  }

  /**
   * Logout current user
   */
  logout(): Observable<any> {
    return this.apollo.mutate({
      mutation: LOGOUT
    }).pipe(
      map(result => {
        // Clear auth data
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        this.currentUserSubject.next(null);
        return result.data || true;
      })
    );
  }

  /**
   * Get current authenticated user
   */
  getCurrentUser(): Observable<UserResponse> {
    return this.apollo.query({
      query: GET_CURRENT_USER,
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => {
        const user = this.extractData<UserResponse>(result, 'me');
        this.setCurrentUser(user);
        this.currentUserSubject.next(user);
        return user;
      })
    );
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private setCurrentUser(user: UserResponse): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  private getCurrentUserFromStorage(): UserResponse | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
  }
}
```

**Key Methods:**
- `apollo.mutate()` - Execute mutations (create, update, delete)
- `apollo.query()` - Execute queries (read)
- `variables` - Pass GraphQL variables
- `fetchPolicy` - Control caching behavior
  - `'cache-first'` - Use cache if available (default)
  - `'network-only'` - Always fetch from network
  - `'cache-and-network'` - Use cache then fetch

**`banking.service.ts`**

```typescript
@Injectable({
  providedIn: 'root'
})
export class BankingService {
  constructor(private apollo: Apollo) { }

  /**
   * Create a new account
   */
  createAccount(request: CreateAccountRequest): Observable<Account> {
    return this.apollo.mutate({
      mutation: CREATE_ACCOUNT,
      variables: {
        input: {
          firstName: request.firstName,
          lastName: request.lastName,
          nationality: request.nationality,
          accountType: request.accountType
        }
      }
    }).pipe(
      map(result => this.extractData<Account>(result, 'createAccount')),
      catchError(this.handleError)
    );
  }

  /**
   * Get all accounts
   */
  getAllAccounts(): Observable<Account[]> {
    return this.apollo.query({
      query: GET_ACCOUNTS,
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => this.extractData<Account[]>(result, 'accounts')),
      catchError(this.handleError)
    );
  }

  /**
   * Deposit money
   */
  deposit(accountId: string, request: TransactionRequest): Observable<Transaction> {
    return this.apollo.mutate({
      mutation: DEPOSIT,
      variables: {
        accountId,
        input: {
          amount: request.amount,
          description: request.description,
          categoryId: request.categoryId
        }
      }
    }).pipe(
      map(result => this.extractData<Transaction>(result, 'deposit')),
      catchError(this.handleError)
    );
  }

  /**
   * Handle GraphQL errors
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.graphQLErrors && error.graphQLErrors.length > 0) {
      errorMessage = error.graphQLErrors.map((e: any) => e.message).join(', ');
    } else if (error.networkError) {
      errorMessage = `Network error: ${error.networkError.message}`;
    } else if (error.message) {
      errorMessage = error.message;
    }

    console.error('GraphQL Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
```

### 4. Using Services in Components

**Login Component:**

```typescript
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.formBuilder.group({
      emailOrUsername: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.loading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: (authData) => {
        // authData = { token, user }
        this.router.navigate(['/banking']);
      },
      error: (error) => {
        this.errorMessage = error.message;
        this.loading = false;
      }
    });
  }
}
```

**Banking Dashboard Component:**

```typescript
export class BankingDashboardComponent implements OnInit {
  accounts: Account[] = [];
  loading = false;
  error: string | null = null;

  constructor(private bankingService: BankingService) {}

  ngOnInit() {
    this.loadAccounts();
  }

  loadAccounts() {
    this.loading = true;
    this.bankingService.getAllAccounts().subscribe({
      next: (accounts: Account[]) => {
        this.accounts = accounts;
        this.loading = false;
      },
      error: (error) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  createAccount() {
    const request: CreateAccountRequest = {
      firstName: this.newAccount.firstName,
      lastName: this.newAccount.lastName,
      nationality: this.newAccount.nationality,
      accountType: this.newAccount.accountType
    };

    this.bankingService.createAccount(request).subscribe({
      next: (account: Account) => {
        this.accounts.push(account);
        this.showAddModal = false;
      },
      error: (error) => {
        this.error = error.message;
      }
    });
  }
}
```

---

## Writing GraphQL Operations

### Query Examples

**Simple Query:**
```graphql
query GetAccounts {
  accounts {
    id
    accountNumber
    balance
  }
}
```

**Query with Variables:**
```graphql
query GetAccount($id: ID!) {
  account(id: $id) {
    id
    accountNumber
    firstName
    lastName
    balance
  }
}
```

**Nested Query:**
```graphql
query GetAccountWithTransactions($accountId: ID!) {
  account(id: $accountId) {
    id
    accountNumber
    balance
  }
  transactionHistory(accountId: $accountId) {
    id
    type
    amount
    description
    createdAt
  }
}
```

### Mutation Examples

**Create:**
```graphql
mutation CreateAccount($input: CreateAccountInput!) {
  createAccount(input: $input) {
    id
    accountNumber
    balance
    status
  }
}

# Variables:
{
  "input": {
    "firstName": "John",
    "lastName": "Doe",
    "accountType": "CHECKING"
  }
}
```

**Update:**
```graphql
mutation UpdateAccount($id: ID!, $input: UpdateAccountInput!) {
  updateAccount(id: $id, input: $input) {
    id
    accountType
    updatedAt
  }
}

# Variables:
{
  "id": "123",
  "input": {
    "accountType": "SAVINGS"
  }
}
```

**Delete:**
```graphql
mutation DeleteAccount($id: ID!) {
  deleteAccount(id: $id)
}

# Variables:
{
  "id": "123"
}
```

---

## Adding New GraphQL Operations

### Example: Add "Get Account Balance" Feature

#### Step 1: Define in Schema

**`backend/src/main/resources/graphql/schema.graphqls`**

```graphql
type Query {
  # ... existing queries

  # NEW: Get account balance
  accountBalance(accountId: ID!): AccountBalance!
}

# NEW: Response type
type AccountBalance {
  accountId: ID!
  accountNumber: String!
  balance: Decimal!
  currency: String!
  lastUpdated: DateTime!
}
```

#### Step 2: Create Backend Resolver

**`backend/.../BankingResolver.java`**

```java
@Controller
public class BankingResolver {

    private final BankingUseCase bankingUseCase;

    /**
     * Query: accountBalance
     * Get current balance for an account
     */
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public AccountBalance accountBalance(@Argument String accountId) {
        UUID userId = SecurityUtil.getCurrentUserId();

        // Call business logic
        Account account = bankingUseCase.getAccountById(
            UUID.fromString(accountId),
            userId
        );

        // Map to GraphQL response type
        return AccountBalance.builder()
                .accountId(account.getId().toString())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency("USD")
                .lastUpdated(account.getUpdatedAt())
                .build();
    }
}
```

#### Step 3: Define Frontend Operation

**`frontend/src/app/graphql/graphql.operations.ts`**

```typescript
export const GET_ACCOUNT_BALANCE = gql`
  query GetAccountBalance($accountId: ID!) {
    accountBalance(accountId: $accountId) {
      accountId
      accountNumber
      balance
      currency
      lastUpdated
    }
  }
`;
```

#### Step 4: Add to Angular Service

**`frontend/src/app/services/banking.service.ts`**

```typescript
export interface AccountBalance {
  accountId: string;
  accountNumber: string;
  balance: number;
  currency: string;
  lastUpdated: string;
}

@Injectable({
  providedIn: 'root'
})
export class BankingService {

  /**
   * Get account balance
   */
  getAccountBalance(accountId: string): Observable<AccountBalance> {
    return this.apollo.query({
      query: GET_ACCOUNT_BALANCE,
      variables: { accountId },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => this.extractData<AccountBalance>(result, 'accountBalance')),
      catchError(this.handleError)
    );
  }
}
```

#### Step 5: Use in Component

**`frontend/src/app/components/banking/dashboard.component.ts`**

```typescript
export class BankingDashboardComponent {
  balance: AccountBalance | null = null;

  constructor(private bankingService: BankingService) {}

  checkBalance(accountId: string) {
    this.bankingService.getAccountBalance(accountId).subscribe({
      next: (balance) => {
        this.balance = balance;
        console.log(`Balance: ${balance.currency} ${balance.balance}`);
      },
      error: (error) => {
        console.error('Failed to load balance:', error);
      }
    });
  }
}
```

---

## Common Patterns

### 1. Pagination

**Backend Schema:**
```graphql
type Query {
  notifications(page: Int!, size: Int!): PagedNotifications!
}

type PagedNotifications {
  notifications: [Notification!]!
  currentPage: Int!
  totalPages: Int!
  totalItems: Int!
  hasNext: Boolean!
  hasPrevious: Boolean!
}
```

**Frontend:**
```typescript
getNotifications(page: number = 0, size: number = 20): Observable<PagedNotifications> {
  return this.apollo.query({
    query: GET_NOTIFICATIONS,
    variables: { page, size },
    fetchPolicy: 'network-only'
  }).pipe(
    map(result => this.extractData<PagedNotifications>(result, 'notifications'))
  );
}
```

### 2. Optimistic UI Updates

Update UI immediately, rollback if mutation fails:

```typescript
createCategory(request: CategoryRequest): Observable<Category> {
  return this.apollo.mutate({
    mutation: CREATE_CATEGORY,
    variables: { input: request },
    optimisticResponse: {
      __typename: 'Mutation',
      createCategory: {
        __typename: 'Category',
        id: 'temp-id',
        name: request.name,
        description: request.description,
        type: request.type,
        active: true,
        createdAt: new Date().toISOString()
      }
    }
  }).pipe(
    map(result => this.extractData<Category>(result, 'createCategory'))
  );
}
```

### 3. Cache Updates

Manually update Apollo cache after mutations:

```typescript
createAccount(request: CreateAccountRequest): Observable<Account> {
  return this.apollo.mutate({
    mutation: CREATE_ACCOUNT,
    variables: { input: request },
    update: (cache, { data }) => {
      // Read current cache
      const existing: any = cache.readQuery({ query: GET_ACCOUNTS });

      // Write updated cache
      cache.writeQuery({
        query: GET_ACCOUNTS,
        data: {
          accounts: [...existing.accounts, data.createAccount]
        }
      });
    }
  }).pipe(
    map(result => this.extractData<Account>(result, 'createAccount'))
  );
}
```

### 4. Error Handling

**Global Error Handler:**

```typescript
const errorLink = onError(({ graphQLErrors, networkError }) => {
  if (graphQLErrors) {
    graphQLErrors.forEach(({ message, locations, path }) => {
      console.error(`[GraphQL error]: ${message}`);

      // Handle authentication errors
      if (message.includes('Unauthorized') || message.includes('Access denied')) {
        // Redirect to login
        router.navigate(['/auth/login']);
      }
    });
  }

  if (networkError) {
    console.error(`[Network error]: ${networkError}`);
  }
});

export function createApollo(httpLink: HttpLink): ApolloClientOptions<any> {
  return {
    link: from([errorLink, auth, httpLink.create({ uri })]),
    cache: new InMemoryCache(),
  };
}
```

### 5. Loading States

**Component Pattern:**

```typescript
export class AccountListComponent {
  accounts$ = this.apollo
    .watchQuery({ query: GET_ACCOUNTS })
    .valueChanges
    .pipe(
      map(result => ({
        loading: result.loading,
        error: result.error,
        accounts: result.data?.accounts || []
      }))
    );
}
```

**Template:**
```html
<div *ngIf="accounts$ | async as data">
  <div *ngIf="data.loading">Loading accounts...</div>
  <div *ngIf="data.error">Error: {{ data.error.message }}</div>
  <div *ngIf="!data.loading && !data.error">
    <div *ngFor="let account of data.accounts">
      {{ account.accountNumber }} - {{ account.balance }}
    </div>
  </div>
</div>
```

---

## Troubleshooting

### Common Issues

#### 1. "Can't serialize value: Expected OffsetDateTime but was LocalDateTime"

**Problem:** Custom DateTime scalar not configured properly.

**Solution:** Ensure `GraphQLConfig.java` has `@Order(0)` and custom scalar:

```java
@Bean
@Order(0) // Highest precedence
public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiringBuilder -> wiringBuilder
            .scalar(createLocalDateTimeScalar());
}
```

#### 2. "GraphQL response is null or undefined"

**Problem:** Trying to access `result.data` before checking if it exists.

**Solution:** Use safe extraction helper:

```typescript
private extractData<T>(result: any, key: string): T {
  if (!result || !result.data) {
    return null as any;
  }
  return result.data[key];
}
```

#### 3. "Observable didn't execute"

**Problem:** Not subscribing to Observable (RxJS Observables are lazy).

**Solution:** Always subscribe:

```typescript
// ❌ Wrong - Nothing happens!
this.authService.logout();

// ✅ Correct - Executes the logout
this.authService.logout().subscribe();
```

#### 4. "401 Unauthorized" on Authenticated Requests

**Problem:** JWT token not being sent in headers.

**Solution:** Check `GraphQLModule` auth context:

```typescript
const auth = setContext((operation, context) => {
  const token = localStorage.getItem('auth_token');
  if (token === null) {
    return {};
  }
  return {
    headers: {
      Authorization: `Bearer ${token}`
    }
  };
});
```

#### 5. "Field doesn't exist in schema"

**Problem:** Schema definition doesn't match resolver or query.

**Solution:**
1. Check schema file: `schema.graphqls`
2. Restart backend (schema changes require restart)
3. Match field names exactly (case-sensitive)

#### 6. "Cannot read property 'success' of undefined"

**Problem:** Component expecting old REST API response wrapper.

**Solution:** GraphQL returns unwrapped data:

```typescript
// ❌ Old REST pattern
if (response.success) { ... }

// ✅ GraphQL pattern
next: (authData) => {
  // authData = { token, user }
}
```

### Debugging Tools

**1. GraphiQL (Backend)**
- URL: `http://localhost:8080/graphiql`
- Interactive GraphQL playground
- Auto-complete and documentation
- Test queries and mutations

**2. Apollo DevTools (Frontend)**
- Chrome Extension: "Apollo Client Devtools"
- View GraphQL operations
- Inspect cache
- Monitor network requests

**3. Network Tab**
- Check actual GraphQL requests
- Verify JWT token in headers
- See response data

---

## Best Practices

### Backend

1. **Keep resolvers thin** - Move logic to use cases
2. **Use DTOs** - Don't expose domain entities directly
3. **Validate inputs** - Use `@Valid` and input validation
4. **Handle errors gracefully** - Use `@GraphQLExceptionHandler`
5. **Document your schema** - Add descriptions
6. **Version carefully** - Use `@deprecated` instead of breaking changes

### Frontend

1. **Centralize operations** - Keep all queries/mutations in `graphql.operations.ts`
2. **Type your responses** - Create TypeScript interfaces
3. **Handle errors** - Always provide error callbacks
4. **Use cache wisely** - Choose appropriate `fetchPolicy`
5. **Unsubscribe** - Prevent memory leaks in components
6. **Extract common logic** - Create reusable helper methods

---

## Resources

- **GraphQL Official**: https://graphql.org/
- **Spring GraphQL**: https://spring.io/projects/spring-graphql
- **Apollo Angular**: https://apollo-angular.com/
- **GraphQL Best Practices**: https://graphql.org/learn/best-practices/

---

## Summary

**GraphQL in Your Project:**

1. **Backend (Spring Boot):**
   - Schema: `schema.graphqls`
   - Resolvers: `@QueryMapping` and `@MutationMapping`
   - Custom Scalars: DateTime, Decimal, Date
   - Security: JWT via Spring Security

2. **Frontend (Angular):**
   - Apollo Client for GraphQL communication
   - Operations defined in `graphql.operations.ts`
   - Services wrap Apollo with typed methods
   - Components subscribe to Observables

3. **Key Advantages:**
   - Single endpoint (`/graphql`)
   - Clients request exactly what they need
   - Strong typing on both ends
   - Better developer experience

**Next Steps:**
- Explore GraphiQL at `http://localhost:8080/graphiql`
- Try modifying existing queries
- Add a new query/mutation following the guide
- Install Apollo DevTools for debugging
