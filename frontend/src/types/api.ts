import type { LogEntry } from './log';
import type { MetricEntry } from './metric';
import type { Application } from './application';

export interface SortParams {
  sortBy: string;
  sortOrder: 'asc' | 'desc';
}

export interface ApiError {
  message: string;
  code: string;
  details?: Record<string, unknown>;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface ApplicationLogsResponse {
  appName: string;
  paginatedLogs: PaginatedResponse<LogEntry>;
}

export interface ApplicationMetricsResponse {
  appName: string;
  paginatedMetrics: PaginatedResponse<MetricEntry>;
}

// ============================================================================
// Authentication Types
// ============================================================================

export interface User {
  email: string;
  username?: string;
  accountKey: string;
  role: string;
  firstConnection: boolean;
  registrationDate?: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  confirmationPassword: string;
}

export interface LoginRequest {
  username: string; // Can be username, email, or any identifier
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface ApplicationLogsResponse {
  appName: string;
  paginatedLogs: PaginatedResponse<LogEntry>;
}

export interface ApplicationMetricsResponse {
  appName: string;
  paginatedMetrics: PaginatedResponse<MetricEntry>;
}

// ============================================================================
// Application Types for API
// ============================================================================

export interface ApplicationsResponse {
  content: Application[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface ApplicationFilters {
  search?: string;
  owner?: string;
}

export interface MetricDataPoint {
  timestamp: string;
  value: number;
}
