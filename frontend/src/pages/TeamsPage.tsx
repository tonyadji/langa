/**
 * TeamsPage
 * 
 * Main page for teams management - lists teams, create team, pending invitations
 */

import { useState } from 'react';
import { Plus } from 'lucide-react';
import { useTeams } from '@/features/teams/hooks/useTeams';
import { useTeamInvitations } from '@/features/teams/hooks/useTeamInvitations';
import { TeamList } from '@/features/teams/components/TeamList';
import { PendingInvitations } from '@/features/teams/components/PendingInvitations';
import { CreateTeamModal } from '@/features/teams/components/CreateTeamModal';

export const TeamsPage: React.FC = () => {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const { teams, isLoading: teamsLoading, refetch } = useTeams();
  const { invitations, acceptInvitation, refetch: refetchInvitations } = useTeamInvitations();

  const handleAcceptInvitation = async (invitationToken: string) => {
    // Extract teamId and guest from the first invitation
    const invitation = invitations.find(inv => inv.identity.invitationToken === invitationToken);
    if (invitation) {
      await acceptInvitation(
        invitation.identity.teamId,
        invitationToken,
        invitation.stakeHolders.guest
      );
      await refetchInvitations();
    }
  };

  const handleCreateSuccess = async () => {
    await refetch();
    setIsCreateModalOpen(false);
  };

  if (teamsLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Loading teams...</div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      {/* Fixed Header */}
      <div className="flex-none flex items-center justify-between pb-4 border-b border-gray-200 dark:border-gray-700">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Teams</h1>
        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          <Plus size={20} />
          Create Team
        </button>
      </div>

      {/* Scrollable Content */}
      <div className="flex-1 overflow-y-auto scrollbar-light">
        <div className="pt-4 max-w-7xl mx-auto">
          {invitations.length > 0 && (
            <div className="mb-8">
              <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">
                Pending Invitations ({invitations.length})
              </h2>
              <PendingInvitations
                invitations={invitations}
                onAccept={handleAcceptInvitation}
              />
            </div>
          )}

          <div>
            <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">
              My Teams ({teams.length})
            </h2>
            <TeamList teams={teams} />
          </div>
        </div>
      </div>

      <CreateTeamModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={handleCreateSuccess}
      />
    </div>
  );
};

