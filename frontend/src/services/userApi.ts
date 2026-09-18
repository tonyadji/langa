import { apiClient } from './api';

export interface UserProfile {
  email: string;
  accountKey: string;
}

export const userApi = {
  getMe: async (): Promise<UserProfile> => {
    const response = await apiClient.get<UserProfile>('/users/me');
    return response.data;
  },
};
