import api from './api';
import type { RegisterRequest, LoginRequest, AuthResponse, RefreshTokenRequest } from '@/types/api';

/**
 * Authentication API client
 * Handles user registration, login, and token refresh
 */
export const authApi = {
  /**
   * Register a new user account
   * 
   * @param data Registration credentials
   * @returns Success message
   */
  async register(data: RegisterRequest): Promise<string> {
    const response = await api.post<string>('/auth/register', data);
    return response.data;
  },
  
  /**
   * Authenticate user and obtain JWT tokens
   * 
   * @param data Login credentials
   * @returns AuthResponse with access and refresh tokens
   */
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', data);
    return response.data;
  },
  
  /**
   * Refresh an expired access token
   * 
   * @param data Refresh token
   * @returns AuthResponse with new access and refresh tokens
   */
  async refreshToken(data: RefreshTokenRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/refresh', data);
    return response.data;
  },

  /**
   * Logout the current user
   */
  async logout(): Promise<void> {
    await api.post('/users/logout');
  },
};
