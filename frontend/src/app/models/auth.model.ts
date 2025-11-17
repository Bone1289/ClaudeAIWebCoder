/**
 * Authentication-related models
 */

export interface LoginRequest {
  emailOrUsername: string;
  password: string;
}

export interface SignUpRequest {
  email: string;
  username: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface UserResponse {
  id: string;
  email: string;
  username: string;
  firstName: string;
  lastName: string;
  role: string;
  status: string;
  createdAt: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  user: UserResponse;
}
