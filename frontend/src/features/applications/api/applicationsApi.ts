import { apiClient } from '@/services/api';
import type { Application, ApplicationsResponse, ApplicationFilters, PaginationParams } from '@/types';

export const applicationsApi = {
  getApplications: async (
    params: PaginationParams & ApplicationFilters
  ): Promise<ApplicationsResponse> => {
    const response = await apiClient.get<ApplicationsResponse>('/applications', { params });
    return response.data;
  },

  getApplicationById: async (id: string): Promise<Application> => {
    const response = await apiClient.get<Application>(`/applications/${id}`);
    return response.data;
  },

  createApplication: async (data: Partial<Application>): Promise<Application> => {
    const response = await apiClient.post<Application>('/applications', data);
    return response.data;
  },

  updateApplication: async (id: string, data: Partial<Application>): Promise<Application> => {
    const response = await apiClient.patch<Application>(`/applications/${id}`, data);
    return response.data;
  },

  deleteApplication: async (id: string): Promise<void> => {
    await apiClient.delete(`/applications/${id}`);
  },

  startApplication: async (id: string): Promise<Application> => {
    const response = await apiClient.post<Application>(`/applications/${id}/start`);
    return response.data;
  },

  stopApplication: async (id: string): Promise<Application> => {
    const response = await apiClient.post<Application>(`/applications/${id}/stop`);
    return response.data;
  },

  restartApplication: async (id: string): Promise<Application> => {
    const response = await apiClient.post<Application>(`/applications/${id}/restart`);
    return response.data;
  },
};
