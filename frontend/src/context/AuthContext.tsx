import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { AuthResponse, AuthUser, LoginRequest, SignupRequest } from '../types/auth';
import * as authService from '../services/auth';

interface AuthContextValue {
  user: AuthUser | null;
  token: string | null;
  loading: boolean;
  error: string | null;
  login: (payload: LoginRequest) => Promise<void>;
  signup: (payload: SignupRequest) => Promise<void>;
  logout: () => void;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const AUTH_USER_KEY = 'codeforge_ai_user';

function getPersistedUser(): AuthUser | null {
  const data = localStorage.getItem(AUTH_USER_KEY);
  return data ? JSON.parse(data) : null;
}

function persistUser(user: AuthUser | null) {
  if (user) {
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
  } else {
    localStorage.removeItem(AUTH_USER_KEY);
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const storedToken = authService.getAuthToken();
    const storedUser = getPersistedUser();
    if (storedToken) {
      setToken(storedToken);
    }
    if (storedUser) {
      setUser(storedUser);
    }
    setLoading(false);
  }, []);

  const login = async (payload: LoginRequest) => {
    setLoading(true);
    setError(null);

    try {
      const authResponse = await authService.login(payload);
      const authUser = {
        email: authResponse.email ?? payload.email,
        firstName: authResponse.firstName ?? '',
        lastName: authResponse.lastName ?? '',
        userId: authResponse.userId,
        role: authResponse.role,
        createdAt: authResponse.createdAt ? authResponse.createdAt.toString() : undefined,
      };
      setUser(authUser);
      setToken(authResponse.token);
      persistUser(authUser);
    } catch (err) {
      const responseData = (err as any)?.response?.data;
      let message = 'Unable to login. Please check your credentials.';

      if (responseData) {
        // Handle validation errors with field-specific messages
        if (responseData.errors && typeof responseData.errors === 'object') {
          const fieldErrors = Object.entries(responseData.errors)
            .map(([field, error]) => `${field}: ${error}`)
            .join(', ');
          message = fieldErrors || responseData.message || message;
        } else if (responseData.message) {
          // Handle generic error messages
          message = responseData.message;
        }
      }

      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const signup = async (payload: SignupRequest) => {
    setLoading(true);
    setError(null);

    console.log('Calling signup API with payload:', payload);

    try {
      const authResponse = await authService.signup(payload);
      const authUser = {
        email: authResponse.email ?? payload.email,
        firstName: authResponse.firstName ?? payload.firstName,
        lastName: authResponse.lastName ?? payload.lastName,
        userId: authResponse.userId,
        role: authResponse.role,
        createdAt: authResponse.createdAt ? authResponse.createdAt.toString() : undefined,
      };
      setUser(authUser);
      setToken(authResponse.token);
      persistUser(authUser);
    } catch (err) {
      console.error('Signup error response:', err);
      const responseData = (err as any)?.response?.data;
      let message = 'Signup failed. Please verify your details.';

      if (responseData) {
        // Handle validation errors with field-specific messages
        if (responseData.errors && typeof responseData.errors === 'object') {
          const fieldErrors = Object.entries(responseData.errors)
            .map(([field, error]) => `${field}: ${error}`)
            .join(', ');
          message = fieldErrors || responseData.message || message;
        } else if (responseData.message) {
          // Handle generic error messages
          message = responseData.message;
        }
      }

      setError(message);
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  };

  const clearError = () => {
    setError(null);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setToken(null);
    persistUser(null);
  };

  const value = useMemo(
    () => ({
      user,
      token,
      loading,
      error,
      login,
      signup,
      logout,
      clearError,
    }),
    [user, token, loading, error]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside an AuthProvider');
  }
  return context;
}
