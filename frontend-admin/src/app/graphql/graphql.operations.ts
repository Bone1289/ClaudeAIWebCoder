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
        status
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
      status
    }
  }
`;

// ==================== Admin Queries ====================

export const GET_ALL_USERS = gql`
  query GetAllUsers {
    adminUsers {
      id
      email
      username
      firstName
      lastName
      role
      status
      createdAt
      updatedAt
    }
  }
`;

export const GET_USER_BY_ID = gql`
  query GetUserById($id: ID!) {
    adminUser(id: $id) {
      id
      email
      username
      firstName
      lastName
      role
      status
      createdAt
      updatedAt
    }
  }
`;

export const GET_ALL_ADMIN_ACCOUNTS = gql`
  query GetAllAdminAccounts {
    adminAccounts {
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

export const GET_ADMIN_ACCOUNT_BY_ID = gql`
  query GetAdminAccountById($id: ID!) {
    adminAccount(id: $id) {
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

export const GET_ACCOUNTS_BY_USER_ID = gql`
  query GetAccountsByUserId($userId: ID!) {
    adminUserAccounts(userId: $userId) {
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

// ==================== Admin Mutations ====================

export const SUSPEND_USER = gql`
  mutation SuspendUser($id: ID!) {
    adminSuspendUser(id: $id) {
      id
      status
    }
  }
`;

export const ACTIVATE_USER = gql`
  mutation ActivateUser($id: ID!) {
    adminActivateUser(id: $id) {
      id
      status
    }
  }
`;

export const LOCK_USER = gql`
  mutation LockUser($id: ID!) {
    adminLockUser(id: $id) {
      id
      status
    }
  }
`;

export const DELETE_USER = gql`
  mutation DeleteUser($id: ID!) {
    adminDeleteUser(id: $id)
  }
`;
