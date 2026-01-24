/**
 * useTeam Hook
 * 
 * Fetches a single team by its key/ID
 */

import { useState, useEffect, useCallback } from 'react';
import api from '@/services/api';
import type { Team } from '@/types/team';

interface UseTeamReturn {
  team: Team | null;
  isLoading: boolean;
  error: Error | null;
  refetch: () => Promise<void>;
  removeMember: (memberEmail: string) => Promise<void>;
}

export const useTeam = (teamId: string | undefined): UseTeamReturn => {
  const [team, setTeam] = useState<Team | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const fetchTeam = useCallback(async () => {
    if (!teamId) {
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);
      setError(null);
      const response = await api.get<Team>(`/teams/${teamId}`);
      setTeam(response.data);
    } catch (err) {
      setError(err as Error);
      setTeam(null);
    } finally {
      setIsLoading(false);
    }
  }, [teamId]);

  useEffect(() => {
    fetchTeam();
  }, [fetchTeam]);

  const removeMember = useCallback(async (memberEmail: string): Promise<void> => {
    if (!teamId) return;
    await api.delete(`/teams/${teamId}/members/${memberEmail}`);
    await fetchTeam(); // Refetch to update team data
  }, [teamId, fetchTeam]);

  const refetch = useCallback(async () => {
    await fetchTeam();
  }, [fetchTeam]);

  return {
    team,
    isLoading,
    error,
    refetch,
    removeMember
  };
};
