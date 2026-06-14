import api from './api';
import {
  AuthResponse,
  LoginRequest,
  PasswordResetRequest,
  PasswordResetStartRequest,
  PasswordResetVerifyRequest,
  SecurityQuestionResponse,
  SignupRequest,
} from '../types/auth';

const TOKEN_KEY = 'codeforge_ai_token';

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export function getAuthToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setAuthToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function removeAuthToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function isAuthenticated() {
  return Boolean(getAuthToken());
}

export async function login(request: LoginRequest) {
  const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', request);
  const authResponse = response.data.data;
  setAuthToken(authResponse.token);
  return authResponse;
}

export async function signup(request: SignupRequest) {
  const response = await api.post<ApiResponse<AuthResponse>>('/auth/signup', request);
  const authResponse = response.data.data;
  setAuthToken(authResponse.token);
  return authResponse;
}

export async function startPasswordReset(request: PasswordResetStartRequest) {
  const response = await api.post<ApiResponse<SecurityQuestionResponse>>('/auth/forgot-password/start', request);
  return response.data.data;
}

export async function verifySecurityAnswer(request: PasswordResetVerifyRequest) {
  const response = await api.post<ApiResponse<string>>('/auth/forgot-password/verify', request);
  return response.data.data;
}

export async function resetPassword(request: PasswordResetRequest) {
  const response = await api.post<ApiResponse<string>>('/auth/forgot-password/reset', request);
  return response.data.data;
}

export async function deleteAccount() {
  const response = await api.delete<ApiResponse<string>>('/users/me');
  return response.data.data;
}

export function logout() {
  removeAuthToken();
}
