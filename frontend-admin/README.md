# Banking Admin Portal

This is the admin portal for the Banking Application. It provides administrative functionality for managing users and accounts.

## Features

- **Admin Authentication**: Secure login for administrators only
- **User Management**: View, suspend, activate, lock, and delete users
- **Account Management**: View all bank accounts across all users
- **Real-time Updates**: Refresh data to see the latest information

## Prerequisites

- Node.js (v18 or higher)
- npm
- Backend API running on http://localhost:8080

## Installation

```bash
cd frontend-admin
npm install
```

## Running the Application

```bash
npm start
```

The admin portal will be available at `http://localhost:4201`

## Default Admin Credentials

- **Username**: admin
- **Password**: Admin

## Project Structure

```
frontend-admin/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── login/              # Admin login page
│   │   │   ├── dashboard/          # Main dashboard layout
│   │   │   ├── user-management/    # User management page
│   │   │   └── account-management/ # Account management page
│   │   ├── services/
│   │   │   ├── auth.service.ts     # Authentication service
│   │   │   └── admin.service.ts    # Admin API service
│   │   ├── guards/
│   │   │   └── auth.guard.ts       # Route protection
│   │   ├── interceptors/
│   │   │   └── auth.interceptor.ts # JWT token injection
│   │   └── models/
│   │       ├── user.model.ts       # User models
│   │       └── account.model.ts    # Account models
│   └── ...
```

## Available Routes

- `/login` - Admin login page
- `/dashboard/users` - User management
- `/dashboard/accounts` - Account management

## Admin Features

### User Management
- View all users in the system
- Suspend/activate user accounts
- Lock user accounts for security
- Delete users permanently

### Account Management
- View all bank accounts
- See account balances and transaction counts
- Filter accounts by user

## Security

- JWT-based authentication
- Admin role verification on both frontend and backend
- HTTP interceptor for automatic token injection
- Automatic logout on authentication errors

## API Endpoints Used

- `POST /api/auth/login` - Admin login
- `GET /api/admin/users` - Get all users
- `PUT /api/admin/users/{id}/suspend` - Suspend user
- `PUT /api/admin/users/{id}/activate` - Activate user
- `PUT /api/admin/users/{id}/lock` - Lock user
- `DELETE /api/admin/users/{id}` - Delete user
- `GET /api/admin/accounts` - Get all accounts
- `GET /api/admin/users/{userId}/accounts` - Get user accounts
