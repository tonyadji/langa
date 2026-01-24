/**
 * Application API Service
 * 
 * Handles all HTTP requests for application management operations.
 */

import { apiClient } from './api';
import type {
  Application,
  ApplicationSecured,
  ShareApplicationRequest,
  RevokeAccessRequest,
  ShareWith,
  UpdateRetentionPolicyRequest,
} from '@/types';

interface GetApplicationsResponse {
  applications: Application[];
  total: number;
  page: number;
  limit: number;
}

interface GetApplicationsParams {
  page?: number;
  limit?: number;
}

export const applicationApi = {
  /**
   * Get list of applications (owned and shared)
   */
  async getApplications(params: GetApplicationsParams = {}): Promise<GetApplicationsResponse> {
    // Backend expects 0-based page index, frontend uses 1-based
    const frontendPage = params.page || 1;
    const page = frontendPage > 0 ? frontendPage - 1 : 0;
    const limit = params.limit || 10;
    
    const response = await apiClient.get<GetApplicationsResponse | Application[]>('/applications', {
      params: { page, limit },
    });
    
    // Handle both array response (backend v1) and paginated response (backend v2)
    if (Array.isArray(response.data)) {
      return {
        applications: response.data,
        total: response.data.length,
        page: frontendPage,
        limit: response.data.length,
      };
    }
    
    // Convert backend 0-based page back to frontend 1-based
    return {
      ...response.data,
      page: frontendPage,
    };
  },

  /**
   * Create a new application
   */
  async createApplication(name: string): Promise<Application> {
    const response = await apiClient.post<Application>('/applications', { name });
    return response.data;
  },

  /**
   * Get application details (basic info available to all users)
   */
  async getApplication(id: string): Promise<Application> {
    const response = await apiClient.get<Application>(`/applications/${id}`);
    return response.data;
  },

  /**
   * Get secured application details (credentials only available to owners)
   */
  async getSecuredDetails(id: string): Promise<ApplicationSecured> {
    const response = await apiClient.get<ApplicationSecured>(
      `/applications/${id}/secured-details`
    );
    return response.data;
  },

  /**
   * Update application
   */
  async updateApplication(id: string, data: Partial<Application>): Promise<Application> {
    const response = await apiClient.patch<Application>(`/applications/${id}`, data);
    return response.data;
  },

  /**
   * Delete application
   */
  async deleteApplication(id: string): Promise<void> {
    await apiClient.delete(`/applications/${id}`);
  },

  /**
   * Share application with a user or team (owners only)
   */
  async shareApplication(
    id: string,
    data: ShareApplicationRequest
  ): Promise<ShareWith> {
    const response = await apiClient.post<ShareWith>(`/applications/${id}/share`, data);
    return response.data;
  },

  /**
   * Revoke application access from a user or team (owners only)
   */
  async revokeAccess(id: string, data: RevokeAccessRequest): Promise<void> {
    await apiClient.post(`/applications/${id}/revoke-sharing`, data);
  },

  /**
   * Update retention policy (owners only)
   */
  async updateRetentionPolicy(id: string, data: UpdateRetentionPolicyRequest): Promise<ApplicationSecured> {
    const response = await apiClient.put<ApplicationSecured>(`/applications/${id}/update-retention-policy`, data);
    return response.data;
  }
};
