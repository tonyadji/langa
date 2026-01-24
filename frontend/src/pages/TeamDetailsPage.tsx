/**
 * TeamDetailsPage
 * 
 * Detailed view of a single team with members list and invite functionality
 */

import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, UserPlus, Mail, Clock, CheckCircle, XCircle } from 'lucide-react';
import { useTeam } from '@/features/teams/hooks/useTeam';
import { TeamMembersList } from '@/features/teams/components/TeamMembersList';
import { InviteMemberModal } from '@/features/teams/components/InviteMemberModal';
import { TeamRole, InvitationStatus } from '@/types/team';

export const TeamDetailsPage: React.FC = () => {
  const { teamId } = useParams<{ teamId: string }>();
  const navigate = useNavigate();
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [currentUserEmail] = useState(() => {
    // Get from localStorage or auth context
    const user = localStorage.getItem('langa_user');
    return user ? JSON.parse(user).username : 'unknown@example.com';
  });

  const { team, isLoading, removeMember, refetch } = useTeam(teamId);

  useEffect(() => {
    if (!isLoading && !team) {
      navigate('/teams');
    }
  }, [team, isLoading, navigate]);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500 dark:text-gray-400">Loading team...</div>
      </div>
    );
  }

  if (!team) {
    return null;
  }

  const members = team.members || [];
  const invitations = team.invitations || [];
  const currentMember = members.find((m) => m.email === currentUserEmail);
  const isOwnerOrAdmin = currentMember && (currentMember.role === TeamRole.OWNER || currentMember.role === TeamRole.ADMIN);
  const isOwner = currentMember && currentMember.role === TeamRole.OWNER;

  // Split invitations into accepted and pending/expired
  const acceptedInvitations = invitations.filter((inv) => inv.status === InvitationStatus.ACCEPTED);
  const pendingInvitations = invitations.filter((inv) => inv.status !== InvitationStatus.ACCEPTED);

  const handleRemoveMember = async (memberEmail: string) => {
    await removeMember(memberEmail);
  };

  const handleInviteSuccess = async () => {
    await refetch();
  };

  return (
    <div className="flex flex-col h-full">
      {/* Fixed Header */}
      <div className="flex-none pb-4 border-b border-gray-200 dark:border-gray-700">
        <button
          onClick={() => navigate('/teams')}
          className="flex items-center gap-2 text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100"
        >
          <ArrowLeft size={20} />
          Back to Teams
        </button>
      </div>

      {/* Scrollable Content */}
      <div className="flex-1 overflow-y-auto scrollbar-light">
        <div className="pt-4 max-w-7xl mx-auto">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm dark:shadow-gray-900/50 border border-gray-200 dark:border-gray-700 p-6">
        <div className="flex items-start justify-between mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">{team.name}</h1>
            <div className="text-sm text-gray-600 dark:text-gray-400 space-y-1">
              <p><span className="font-semibold">Team Key:</span> <span className="font-mono text-xs">{team.key}</span></p>
              <p>Created <span className="font-semibold">by</span> {team.createdBy} <span className="font-semibold">on</span> {new Date(team.createdDate).toLocaleDateString()}</p>
              <p>{members.length} member{members.length !== 1 ? 's' : ''}</p>
            </div>
          </div>

          {isOwnerOrAdmin && (
            <button
              onClick={() => setIsInviteModalOpen(true)}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 dark:bg-blue-500 text-white rounded-md hover:bg-blue-700 dark:hover:bg-blue-600 transition-colors shadow-sm"
              title="Invite a new member to this team"
            >
              <UserPlus size={20} />
              Invite Member
            </button>
          )}
        </div>

        <div>
          <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">Team Members</h2>
          <TeamMembersList
            members={members}
            currentUserEmail={currentUserEmail}

            isOwnerOrAdmin={!!isOwnerOrAdmin}
            onRemoveMember={handleRemoveMember}
          />
        </div>

        {/* Pending Invitations - Only visible to owner */}
        {isOwner && pendingInvitations.length > 0 && (
          <div className="mt-8">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">
              Pending Invitations ({pendingInvitations.length})
            </h2>
            <div className="space-y-3">
              {pendingInvitations.map((invitation) => {
                const isExpired = invitation.expired || new Date(invitation.invitationPeriod.expiryDate) < new Date();
                const statusIcon = isExpired ? (
                  <XCircle className="w-5 h-5 text-red-600 dark:text-red-400" />
                ) : (
                  <Clock className="w-5 h-5 text-yellow-600 dark:text-yellow-400" />
                );

                return (
                  <div
                    key={invitation.identity.invitationToken}
                    className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600"
                  >
                    <div className="flex items-center gap-3">
                      <Mail className="w-5 h-5 text-gray-400 dark:text-gray-500" />
                      <div>
                        <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                          {invitation.stakeHolders.guest}
                        </p>
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          Invited by {invitation.stakeHolders.host} on{' '}
                          {new Date(invitation.invitationPeriod.inviteDate).toLocaleDateString()}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <div className="text-right">
                        <p className="text-xs font-medium text-gray-700 dark:text-gray-300 flex items-center gap-1">
                          {statusIcon}
                          {isExpired ? 'Expired' : 'Pending'}
                        </p>
                        {!isExpired && (
                          <p className="text-xs text-gray-500 dark:text-gray-400">
                            Expires {new Date(invitation.invitationPeriod.expiryDate).toLocaleDateString()}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Accepted Invitations - Only visible to owner */}
        {isOwner && acceptedInvitations.length > 0 && (
          <div className="mt-8">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">
              Accepted Invitations ({acceptedInvitations.length})
            </h2>
            <div className="space-y-3">
              {acceptedInvitations.map((invitation) => (
                <div
                  key={invitation.identity.invitationToken}
                  className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg border border-gray-200 dark:border-gray-600"
                >
                  <div className="flex items-center gap-3">
                    <Mail className="w-5 h-5 text-gray-400 dark:text-gray-500" />
                    <div>
                      <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                        {invitation.stakeHolders.guest}
                      </p>
                      <p className="text-xs text-gray-500 dark:text-gray-400">
                        Invited by {invitation.stakeHolders.host} on{' '}
                        {new Date(invitation.invitationPeriod.inviteDate).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="text-right">
                      <p className="text-xs font-medium text-gray-700 dark:text-gray-300 flex items-center gap-1">
                        <CheckCircle className="w-5 h-5 text-green-600 dark:text-green-400" />
                        Accepted
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
          </div>
        </div>
      </div>

      {isOwnerOrAdmin && (
        <InviteMemberModal
          isOpen={isInviteModalOpen}
          teamId={team.id}
          onClose={() => setIsInviteModalOpen(false)}
          onSuccess={handleInviteSuccess}
        />
      )}
    </div>
  );
};
