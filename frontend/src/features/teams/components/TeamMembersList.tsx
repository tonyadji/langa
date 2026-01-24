/**
 * TeamMembersList Component
 * 
 * Displays team members with roles and remove functionality for owners/admins
 */

import { Trash2, Crown, Shield, User as UserIcon } from 'lucide-react';
import type { TeamMember } from '@/types/team';
import { TeamRole } from '@/types/team';
import { useState } from 'react';

interface TeamMembersListProps {
  members: TeamMember[];
  currentUserEmail: string;
  isOwnerOrAdmin: boolean;
  onRemoveMember?: (memberEmail: string) => Promise<void>;
}

export const TeamMembersList: React.FC<TeamMembersListProps> = ({
  members,
  currentUserEmail,
  isOwnerOrAdmin,
  onRemoveMember
}) => {
  const [removingEmail, setRemovingEmail] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const getRoleIcon = (role: TeamRole) => {
    switch (role) {
      case TeamRole.OWNER:
        return <Crown size={16} className="text-yellow-600 dark:text-yellow-500" />;
      case TeamRole.ADMIN:
        return <Shield size={16} className="text-blue-600 dark:text-blue-400" />;
      default:
        return <UserIcon size={16} className="text-gray-600 dark:text-gray-400" />;
    }
  };

  const handleRemove = async (memberEmail: string) => {
    if (!onRemoveMember) return;
    
    if (!confirm(`Remove ${memberEmail} from the team?`)) {
      return;
    }

    try {
      setError(null);
      setRemovingEmail(memberEmail);
      await onRemoveMember(memberEmail);
    } catch (err: any) {
      const errorMessage = err?.response?.data?.error || err.message || 'Failed to remove member';
      setError(errorMessage);
    } finally {
      setRemovingEmail(null);
    }
  };

  const canRemove = (member: TeamMember): boolean => {
    // Can't remove yourself
    if (member.email === currentUserEmail) return false;
    // Can't remove owner
    if (member.role === TeamRole.OWNER) return false;
    // Only owners/admins can remove
    return isOwnerOrAdmin;
  };

  return (
    <div>
      {error && (
        <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-md">
          <p className="text-sm text-red-700 dark:text-red-400">{error}</p>
        </div>
      )}

      <div className="space-y-2">
        {members.map((member) => (
          <div
            key={member.email}
            className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-md border border-gray-200 dark:border-gray-600"
          >
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-1">
                {getRoleIcon(member.role)}
              </div>
              <div>
                <p className="font-medium text-gray-900 dark:text-gray-100">
                  {member.email}
                  {member.email === currentUserEmail && (
                    <span className="ml-2 text-xs text-gray-500 dark:text-gray-400">(You)</span>
                  )}
                </p>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  {member.role} • Joined {new Date(member.addedDate).toLocaleDateString()}
                </p>
              </div>
            </div>

            {canRemove(member) && (
              <button
                onClick={() => handleRemove(member.email)}
                disabled={removingEmail === member.email}
                className="p-2 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-md disabled:opacity-50"
                aria-label={`Remove ${member.email}`}
              >
                <Trash2 size={16} />
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
