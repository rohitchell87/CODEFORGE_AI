export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
}

export interface AuthUser {
  email: string;
  firstName: string;
  lastName: string;
  userId?: number;
  role?: string;
}

export interface AuthResponse {
  token: string;
  type?: string;
  userId?: number;
  email?: string;
  firstName?: string;
  lastName?: string;
  role?: string;
}
