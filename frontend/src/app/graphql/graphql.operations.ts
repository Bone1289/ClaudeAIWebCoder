import { gql } from 'apollo-angular';

// ==================== Authentication ====================

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
        status
      }
    }
  }
`;

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
      createdAt
      updatedAt
    }
  }
`;

export const LOGOUT = gql`
  mutation Logout {
    logout
  }
`;

// ==================== Banking ====================

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

export const GET_ACCOUNT = gql`
  query GetAccount($id: ID!) {
    account(id: $id) {
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

export const UPDATE_ACCOUNT = gql`
  mutation UpdateAccount($id: ID!, $input: UpdateAccountInput!) {
    updateAccount(id: $id, input: $input) {
      id
      accountNumber
      firstName
      lastName
      nationality
      accountType
      balance
      status
    }
  }
`;

export const DELETE_ACCOUNT = gql`
  mutation DeleteAccount($id: ID!) {
    deleteAccount(id: $id)
  }
`;

export const DEPOSIT = gql`
  mutation Deposit($accountId: ID!, $input: DepositInput!) {
    deposit(accountId: $accountId, input: $input) {
      id
      accountId
      type
      amount
      balanceAfter
      description
      createdAt
    }
  }
`;

export const WITHDRAW = gql`
  mutation Withdraw($accountId: ID!, $input: WithdrawInput!) {
    withdraw(accountId: $accountId, input: $input) {
      id
      accountId
      type
      amount
      balanceAfter
      description
      createdAt
    }
  }
`;

export const TRANSFER = gql`
  mutation Transfer($fromAccountId: ID!, $input: TransferInput!) {
    transfer(fromAccountId: $fromAccountId, input: $input) {
      id
      accountId
      type
      amount
      balanceAfter
      description
      relatedAccountId
      createdAt
    }
  }
`;

export const GET_TRANSACTION_HISTORY = gql`
  query GetTransactionHistory($accountId: ID!) {
    transactionHistory(accountId: $accountId) {
      id
      accountId
      type
      categoryId
      amount
      balanceAfter
      description
      relatedAccountId
      createdAt
    }
  }
`;

export const GET_ALL_TRANSACTIONS = gql`
  query GetAllTransactions {
    transactions {
      id
      accountId
      type
      categoryId
      amount
      balanceAfter
      description
      relatedAccountId
      createdAt
    }
  }
`;

export const GET_ACCOUNT_STATEMENT = gql`
  query GetAccountStatement($accountId: ID!, $startDate: Date!, $endDate: Date!) {
    accountStatement(accountId: $accountId, startDate: $startDate, endDate: $endDate) {
      account {
        id
        accountNumber
        firstName
        lastName
      }
      startDate
      endDate
      totalDeposits
      totalWithdrawals
      netChange
      startingBalance
      endingBalance
      transactions {
        id
        type
        amount
        description
        createdAt
      }
    }
  }
`;

export const GET_CATEGORY_REPORT = gql`
  query GetCategoryReport($accountId: ID!, $type: CategoryType) {
    categoryReport(accountId: $accountId, type: $type) {
      categoryId
      categoryName
      categoryType
      totalAmount
      transactionCount
      percentage
    }
  }
`;

// ==================== Categories ====================

export const GET_CATEGORIES = gql`
  query GetCategories($activeOnly: Boolean, $type: CategoryType) {
    categories(activeOnly: $activeOnly, type: $type) {
      id
      name
      description
      type
      color
      active
      createdAt
    }
  }
`;

export const GET_CATEGORY = gql`
  query GetCategory($id: ID!) {
    category(id: $id) {
      id
      name
      description
      type
      color
      active
      createdAt
    }
  }
`;

export const CREATE_CATEGORY = gql`
  mutation CreateCategory($input: CreateCategoryInput!) {
    createCategory(input: $input) {
      id
      name
      description
      type
      color
      active
      createdAt
    }
  }
`;

export const UPDATE_CATEGORY = gql`
  mutation UpdateCategory($id: ID!, $input: UpdateCategoryInput!) {
    updateCategory(id: $id, input: $input) {
      id
      name
      description
      type
      color
      active
    }
  }
`;

export const DEACTIVATE_CATEGORY = gql`
  mutation DeactivateCategory($id: ID!) {
    deactivateCategory(id: $id) {
      id
      active
    }
  }
`;

export const DELETE_CATEGORY = gql`
  mutation DeleteCategory($id: ID!) {
    deleteCategory(id: $id)
  }
`;

// ==================== Notifications ====================

export const GET_NOTIFICATIONS = gql`
  query GetNotifications($page: Int, $size: Int) {
    notifications(page: $page, size: $size) {
      notifications {
        id
        userId
        type
        channel
        title
        message
        priority
        read
        createdAt
        readAt
      }
      currentPage
      totalPages
      totalItems
      hasNext
      hasPrevious
    }
  }
`;

export const GET_UNREAD_NOTIFICATIONS = gql`
  query GetUnreadNotifications($page: Int, $size: Int) {
    unreadNotifications(page: $page, size: $size) {
      notifications {
        id
        userId
        type
        channel
        title
        message
        priority
        read
        createdAt
      }
      currentPage
      totalPages
      totalItems
      hasNext
    }
  }
`;

export const GET_UNREAD_COUNT = gql`
  query GetUnreadCount {
    unreadCount
  }
`;

export const MARK_AS_READ = gql`
  mutation MarkAsRead($id: ID!) {
    markNotificationAsRead(id: $id) {
      id
      read
      readAt
    }
  }
`;

export const MARK_ALL_AS_READ = gql`
  mutation MarkAllAsRead {
    markAllNotificationsAsRead
  }
`;

export const DELETE_NOTIFICATION = gql`
  mutation DeleteNotification($id: ID!) {
    deleteNotification(id: $id)
  }
`;

export const DELETE_ALL_NOTIFICATIONS = gql`
  mutation DeleteAllNotifications {
    deleteAllNotifications
  }
`;

// ==================== Health Check ====================

export const GET_HEALTH = gql`
  query GetHealth {
    health {
      status
      timestamp
    }
  }
`;

export const GET_HELLO = gql`
  query GetHello($name: String) {
    hello(name: $name)
  }
`;
