/**
 * useTeams Hook
 * 
 * Manages team operations: fetch teams, create team, remove members
 */

import { useState, useEffect, useCallback } from 'react';
import api from '@/services/api';
import type { Team, CreateTeamRequest } from '@/types/team';

interface UseTeamsReturn {
  teams: Team[];
  isLoading: boolean;
  error: Error | null;
  createTeam: (data: CreateTeamRequest) => Promise<Team>;
  removeMember: (teamKey: string, memberEmail: string) => Promise<void>;
  refetch: () => Promise<void>;
}

export const useTeams = (): UseTeamsReturn => {
  const [teams, setTeams] = useState<Team[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const fetchTeams = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      const response = await api.get<Team[]>('/teams');
      setTeams(response.data);
    } catch (err) {
      setError(err as Error);
      setTeams([]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTeams();
  }, [fetchTeams]);

  const createTeam = useCallback(async (data: CreateTeamRequest): Promise<Team> => {
    const response = await api.post<Team>('/teams', data);
    await fetchTeams(); // Refetch to update list
    return response.data;
  }, [fetchTeams]);

  const removeMember = useCallback(async (teamKey: string, memberEmail: string): Promise<void> => {
    await api.delete(`/teams/${teamKey}/members/${memberEmail}`);
    await fetchTeams(); // Refetch to update list
  }, [fetchTeams]);

  const refetch = useCallback(async () => {
    await fetchTeams();
  }, [fetchTeams]);

  return {
    teams,
    isLoading,
    error,
    createTeam,
    removeMember,
    refetch
  };
};
