/**
 * PendingInvitations Component
 * 
 * Displays pending team invitations with accept/decline functionality
 */

import { Mail, Check } from 'lucide-react';
import type { TeamInvitation } from '@/types/team';
import { useState } from 'react';

interface PendingInvitationsProps {
  invitations: TeamInvitation[];
  onAccept?: (invitationId: string) => Promise<void>;
}

export const PendingInvitations: React.FC<PendingInvitationsProps> = ({
  invitations,
  onAccept
}) => {
  const [acceptingId, setAcceptingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleAccept = async (invitationId: string) => {
    if (!onAccept) return;

    try {
      setError(null);
      setAcceptingId(invitationId);
      await onAccept(invitationId);
    } catch (err: any) {
      const errorMessage = err?.response?.data?.error || err.message || 'Failed to accept invitation';
      setError(errorMessage);
    } finally {
      setAcceptingId(null);
    }
  };

  if (invitations.length === 0) {
    return (
      <div className="text-center py-8 bg-gray-50 rounded-lg">
        <Mail size={48} className="mx-auto text-gray-400 mb-2" />
        <p className="text-gray-500">No pending invitations</p>
      </div>
    );
  }

  return (
    <div>
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      <div className="space-y-3">
        {invitations.map((invitation) => {
          const sentDate = new Date(invitation.invitationPeriod.inviteDate).toLocaleDateString();
          const expiresDate = new Date(invitation.invitationPeriod.expiryDate).toLocaleDateString();

          return (
            <div
              key={invitation.identity.invitationToken}
              className="p-4 bg-white border border-gray-200 rounded-lg"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <h4 className="font-semibold text-gray-900 mb-1">
                    Team Invitation
                  </h4>
                  <div className="text-sm text-gray-600 space-y-1">
                    <p>Team: <span className="font-mono text-xs">{invitation.stakeHolders.team}</span></p>
                    <p>From: {invitation.stakeHolders.host}</p>
                    <p>Sent: {sentDate} • Expires: {expiresDate}</p>
                  </div>
                </div>

                <button
                  onClick={() => handleAccept(invitation.identity.invitationToken)}
                  disabled={acceptingId === invitation.identity.invitationToken}
                  className="ml-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
                >
                  <Check size={16} />
                  {acceptingId === invitation.identity.invitationToken ? 'Accepting...' : 'Accept'}
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
