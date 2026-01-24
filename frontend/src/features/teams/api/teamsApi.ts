import { apiClient } from '@/services/api';
import type { Team, TeamWithMembers, CreateTeamDto, UpdateTeamDto, AddTeamMemberDto } from '@/types';

export const teamsApi = {
  getTeams: async (): Promise<Team[]> => {
    const response = await apiClient.get<Team[]>('/teams');
    return response.data;
  },

  getTeamById: async (id: string): Promise<TeamWithMembers> => {
    const response = await apiClient.get<TeamWithMembers>(`/teams/${id}`);
    return response.data;
  },

  createTeam: async (data: CreateTeamDto): Promise<Team> => {
    const response = await apiClient.post<Team>('/teams', data);
    return response.data;
  },

  updateTeam: async (id: string, data: UpdateTeamDto): Promise<Team> => {
    const response = await apiClient.patch<Team>(`/teams/${id}`, data);
    return response.data;
  },

  deleteTeam: async (id: string): Promise<void> => {
    await apiClient.delete(`/teams/${id}`);
  },

  addTeamMember: async (teamId: string, data: AddTeamMemberDto): Promise<TeamWithMembers> => {
    const response = await apiClient.post<TeamWithMembers>(`/teams/${teamId}/members`, data);
    return response.data;
  },

  removeTeamMember: async (teamId: string, userId: string): Promise<void> => {
    await apiClient.delete(`/teams/${teamId}/members/${userId}`);
  },
};
