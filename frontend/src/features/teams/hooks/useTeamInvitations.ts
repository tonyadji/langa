/**
 * useTeamInvitations Hook
 * 
 * Manages team invitation operations: fetch pending, invite member, accept invitation
 */

import { useState, useEffect, useCallback } from 'react';
import api from '@/services/api';
import type { TeamInvitation, AcceptInvitationRequest, AcceptInvitationResponse, InviteMemberRequest, Team } from '@/types/team';

interface UseTeamInvitationsReturn {
  invitations: TeamInvitation[];
  isLoading: boolean;
  error: Error | null;
  inviteMember: (data: InviteMemberRequest) => Promise<Team>;
  acceptInvitation: (teamId: string, invitationToken: string, guest: string) => Promise<AcceptInvitationResponse>;
  refetch: () => Promise<void>;
}

export const useTeamInvitations = (): UseTeamInvitationsReturn => {
  const [invitations, setInvitations] = useState<TeamInvitation[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const fetchInvitations = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      // TODO: Endpoint /team-invitations/pending does not exist in backend
      // const response = await api.get<TeamInvitation[]>('/team-invitations/pending');
      // setInvitations(response.data);
      setInvitations([]);
    } catch (err) {
      setError(err as Error);
      setInvitations([]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchInvitations();
  }, [fetchInvitations]);

  const inviteMember = useCallback(async (data: InviteMemberRequest): Promise<Team> => {
    const response = await api.post<Team>('/teams/invite', data);
    return response.data;
  }, []);

  const acceptInvitation = useCallback(async (teamId: string, invitationToken: string, guest: string): Promise<AcceptInvitationResponse> => {
    const data: AcceptInvitationRequest = { guest, invitationToken };
    const response = await api.post<AcceptInvitationResponse>(`/team-invitations/${teamId}/accept?invitationToken=${invitationToken}`, data);
    await fetchInvitations(); // Refetch to update list
    return response.data;
  }, [fetchInvitations]);

  const refetch = useCallback(async () => {
    await fetchInvitations();
  }, [fetchInvitations]);

  return {
    invitations,
    isLoading,
    error,
    inviteMember,
    acceptInvitation,
    refetch
  };
};
