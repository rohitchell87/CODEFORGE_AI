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
  securityQuestion: string;
  securityAnswer: string;
}

export interface PasswordResetStartRequest {
  email: string;
}

export interface SecurityQuestionResponse {
  securityQuestion: string;
}

export interface PasswordResetVerifyRequest {
  email: string;
  securityAnswer: string;
}

export interface PasswordResetRequest {
  email: string;
  securityAnswer: string;
  newPassword: string;
  confirmPassword: string;
}

export interface AuthUser {
  email: string;
  firstName: string;
  lastName: string;
  userId?: number;
  role?: string;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  type?: string;
  userId?: number;
  email?: string;
  firstName?: string;
  lastName?: string;
  role?: string;
  createdAt?: string;
}
