/**
 * API Client Contracts for Langa Dashboard
 * 
 * ⚠️ THIS FILE DEFINES TYPE CONTRACTS ONLY
 * 
 * This file serves as:
 * 1. TypeScript type definitions for API contracts
 * 2. Documentation of expected API behavior
 * 3. Reference for implementing new API clients
 * 
 * All endpoints correspond to the REST API documented in documents/01-SPECIFICATION.md
 * 
 * Actual implementations are located in:
 * - src/services/applicationApi.ts (Applications, Sharing, Revocation)
 * - src/services/authApi.ts (Authentication, Registration)
 * - src/services/logsApi.ts (Logs Query)
 * - src/services/metricsApi.ts (Metrics Query)
 * - src/services/api.ts (Base Axios client)
 * 
 * Usage:
 *   import { applicationApi } from '@/services/applicationApi';
 *   const response = await applicationApi.getById('app-id');
 */

import type {
  // Auth types
  RegisterRequest,
  LoginRequest,
  AuthResponse,
  RefreshTokenRequest,
  // Application types
  Application,
  ApplicationSecured,
  CreateApplicationRequest,
  ShareApplicationRequest,
  RevokeAccessRequest,
  ApplicationUsage,
  // Log types
  LogEntry,
  LogFilterParams,
  PaginatedResponse,
  // Metric types
  MetricEntry,
  MetricFilterParams,
  // Team types
  Team,
  CreateTeamRequest,
  InviteTeamMemberRequest,
  TeamInvitation,
  AcceptInvitationRequest,
  // Common
  User,
} from '../data-model';

// ============================================================================
// API Configuration
// ============================================================================

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Axios instance with interceptors for auth, error handling, and token refresh
 * Implementation in src/services/api.ts
 */
interface ApiClient {
  get<T>(url: string, config?: RequestConfig): Promise<T>;
  post<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T>;
  put<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T>;
  delete<T>(url: string, config?: RequestConfig): Promise<T>;
}

interface RequestConfig {
  headers?: Record<string, string>;
  params?: Record<string, string | number | boolean | undefined>;
}

// ============================================================================
// Authentication API
// ============================================================================

export const authApi = {
  /**
   * POST /api/auth/register
   * Register a new user account
   */
  register: (data: RegisterRequest): Promise<string> => {
    return apiClient.post<string>('/api/auth/register', data);
  },

  /**
   * POST /api/auth/login
   * Authenticate user and obtain JWT tokens
   */
  login: (data: LoginRequest): Promise<AuthResponse> => {
    return apiClient.post<AuthResponse>('/api/auth/login', data);
  },

  /**
   * POST /api/auth/refresh
   * Refresh an expired access token
   */
  refreshToken: (data: RefreshTokenRequest): Promise<AuthResponse> => {
    return apiClient.post<AuthResponse>('/api/auth/refresh', data);
  },

  /**
   * POST /api/users/logout
   * Logout current user and invalidate tokens
   */
  logout: (): Promise<string> => {
    return apiClient.post<string>('/api/users/logout');
  },
};

// ============================================================================
// Applications API
// ============================================================================

export const applicationsApi = {
  /**
   * POST /api/applications
   * Create a new monitored application
   */
  create: (data: CreateApplicationRequest): Promise<Application> => {
    return apiClient.post<Application>('/api/applications', data);
  },

  /**
   * GET /api/applications
   * List all applications accessible to the user (owned + shared)
   */
  list: (): Promise<Application[]> => {
    return apiClient.get<Application[]>('/api/applications');
  },

  /**
   * GET /api/applications/{appId}/secured-details
   * Get full application details including secrets (owners only)
   */
  getSecuredDetails: (appId: string): Promise<ApplicationSecured> => {
    return apiClient.get<ApplicationSecured>(`/api/applications/${appId}/secured-details`);
  },

  /**
   * POST /api/applications/{appId}/share
   * Share an application with a user or team (owners only)
   */
  share: (appId: string, data: ShareApplicationRequest): Promise<ShareWith> => {
    return apiClient.post<ShareWith>(`/api/applications/${appId}/share`, data);
  },

  /**
   * POST /api/applications/{appId}/revoke
   * Revoke application access from a user or team (owners only)
   */
  revokeAccess: (appId: string, data: RevokeAccessRequest): Promise<void> => {
    return apiClient.post<void>(`/api/applications/${appId}/revoke`, data);
  },

  /**
   * GET /api/applications/{appId}/usage
   * Get storage usage statistics for an application
   */
  getUsage: (appId: string): Promise<ApplicationUsage> => {
    return apiClient.get<ApplicationUsage>(`/api/applications/${appId}/usage`);
  },

  /**
   * DELETE /api/applications/{appId}
   * Delete an application (owners only)
   */
  delete: (appId: string): Promise<string> => {
    return apiClient.delete<string>(`/api/applications/${appId}`);
  },

  /**
   * PUT /api/applications/{appId}/update-retention-policy
   * Update data retention policy for an application (owners only)
   */
  updateRetentionPolicy: (appId: string, data: UpdateRetentionPolicyRequest): Promise<SecuredApplicationDto> => {
    return apiClient.put<SecuredApplicationDto>(`/api/applications/${appId}/update-retention-policy`, data);
  },
};

// ============================================================================
// Logs API
// ============================================================================

export const logsApi = {
  /**
   * GET /api/applications/{appId}/logs
   * Retrieve logs for an application with optional filters
   * Returns wrapped response with app name and paginated logs
   */
  query: (appId: string, filters?: LogFilterParams): Promise<ApplicationLogsResponse> => {
    return apiClient.get<ApplicationLogsResponse>(`/api/applications/${appId}/logs`, {
      params: { filterDto: filters, ...filters } as Record<string, string | number | boolean | undefined>,
    });
  },
};

// ============================================================================
// Metrics API
// ============================================================================

export const metricsApi = {
  /**
   * GET /api/applications/{appId}/metrics
   * Retrieve metrics for an application with optional filters
   * Returns wrapped response with app name and paginated metrics
   */
  query: (appId: string, filters?: MetricFilterParams): Promise<ApplicationMetricsResponse> => {
    return apiClient.get<ApplicationMetricsResponse>(`/api/applications/${appId}/metrics`, {
      params: { filterDto: filters, ...filters } as Record<string, string | number | boolean | undefined>,
    });
  },
};

// ============================================================================
// Teams API
// ============================================================================

export const teamsApi = {
  /**
   * POST /api/teams
   * Create a new team
   */
  create: (data: CreateTeamRequest): Promise<Team> => {
    return apiClient.post<Team>('/api/teams', data);
  },

  /**
   * GET /api/teams
   * List all teams the user is a member of
   */
  list: (): Promise<Team[]> => {
    return apiClient.get<Team[]>('/api/teams');
  },

  /**
   * GET /api/teams/{teamId}
   * Get team details including members
   */
  getDetails: (teamId: string): Promise<Team> => {
    return apiClient.get<Team>(`/api/teams/${teamId}`);
  },
};

// ============================================================================
// Team Invitations API
// ============================================================================

export const teamInvitationsApi = {
  /**
   * POST /api/teams/invite
   * Invite a user to join a team
   */
  invite: (data: InviteTeamMemberRequest): Promise<TeamResponseDto> => {
    return apiClient.post<TeamResponseDto>('/api/teams/invite', data);
  },

  /**
   * GET /api/team-invitations/{teamId}
   * Get invitation details (authenticated)
   */
  getInvitation: (teamId: string, invitationToken: string): Promise<GetInvitationResponse> => {
    return apiClient.get<GetInvitationResponse>(`/api/team-invitations/${teamId}`, {
      params: { invitationToken },
    });
  },

  /**
   * GET /api/team-invitations/{teamId}/public
   * Get invitation details (public, no auth required)
   */
  getPublicInvitation: (teamId: string, invitationToken: string): Promise<GetInvitationResponse> => {
    return apiClient.get<GetInvitationResponse>(`/api/team-invitations/${teamId}/public`, {
      params: { invitationToken },
    });
  },

  /**
   * POST /api/team-invitations/{teamId}/accept
   * Accept a team invitation
   */
  accept: (teamId: string, invitationToken: string, data: AcceptInvitationRequest): Promise<GetInvitationResponse> => {
    return apiClient.post<GetInvitationResponse>(
      `/api/team-invitations/${teamId}/accept?invitationToken=${invitationToken}`,
      data
    );
  },
};

// ============================================================================
// Users API
// ============================================================================

export const usersApi = {
  /**
   * GET /api/users/me
   * Get current user profile
   */
  getProfile: (): Promise<User> => {
    return apiClient.get<User>('/api/users/me');
  },
};

// ============================================================================
// First Connection API
// ============================================================================

export const firstConnectionApi = {
  /**
   * GET /api/first-connection
   * Get user info from first connection token
   */
  getUserInfo: (token: string): Promise<User> => {
    return apiClient.get<User>('/api/first-connection', {
      params: { token },
    });
  },

  /**
   * POST /api/first-connection/complete
   * Complete first connection process and set password
   */
  complete: (data: CompleteFirstConnectionRequest): Promise<string> => {
    return apiClient.post<string>('/api/first-connection/complete', data);
  },
};

// ============================================================================
// API Client Implementation (to be implemented in src/services/api.ts)
// ============================================================================

/**
 * This is a placeholder. The actual implementation will be in src/services/api.ts
 * 
 * Implementation should include:
 * - Axios instance with base URL
 * - Request interceptor to inject Authorization header
 * - Response interceptor for token refresh on 401
 * - Error handling and transformation
 * - AbortController for request cancellation
 */
const apiClient: ApiClient = {
  get: async <T>(_url: string, _config?: RequestConfig): Promise<T> => {
    throw new Error('API client not implemented. See src/services/api.ts');
  },
  post: async <T>(_url: string, _data?: unknown, _config?: RequestConfig): Promise<T> => {
    throw new Error('API client not implemented. See src/services/api.ts');
  },
  put: async <T>(_url: string, _data?: unknown, _config?: RequestConfig): Promise<T> => {
    throw new Error('API client not implemented. See src/services/api.ts');
  },
  delete: async <T>(_url: string, _config?: RequestConfig): Promise<T> => {
    throw new Error('API client not implemented. See src/services/api.ts');
  },
};

// Export for use in actual implementation
export { API_BASE_URL, type ApiClient, type RequestConfig };
