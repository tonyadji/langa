/**
 * SharedUsersList Component
 * 
 * Displays the list of users and teams who have access to an application.
 * Allows owners to revoke access with confirmation.
 */

import { useState } from 'react';
import { Trash2, User, Users } from 'lucide-react';
import { useShareApplication } from '../hooks/useShareApplication';
import type { ShareWith } from '@/types';

interface SharedUsersListProps {
  applicationId: string;
  sharedWith: ShareWith[];
  isOwner: boolean;
  onRevokeSuccess?: () => void;
}

export const SharedUsersList = ({
  applicationId,
  sharedWith,
  isOwner,
  onRevokeSuccess,
}: SharedUsersListProps) => {
  const [revokeTarget, setRevokeTarget] = useState<ShareWith | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { revokeAccess, isLoading } = useShareApplication();

  const handleRevokeClick = (share: ShareWith) => {
    setRevokeTarget(share);
  };

  const handleConfirmRevoke = async () => {
    if (!revokeTarget) return;

    setError(null);
    try {
      await revokeAccess(applicationId, {
        shareWith: revokeTarget.key,
        profile: revokeTarget.profile,
      });
      
      setRevokeTarget(null);
      // Only call success callback after successful revoke
      if (onRevokeSuccess) {
        await onRevokeSuccess();
      }
    } catch (err: any) {
      console.error('Failed to revoke access:', err);
      // Extract error message from various error formats
      let errorMessage = 'Failed to revoke access. Please try again.';
      
      if (err?.response?.data?.error) {
        errorMessage = err.response.data.error;
      } else if (err instanceof Error) {
        errorMessage = err.message;
      } else if (typeof err === 'string') {
        errorMessage = err;
      }
      
      setError(errorMessage);
    }
  };

  const handleCancelRevoke = () => {
    setRevokeTarget(null);
    setError(null);
  };

  if (!sharedWith || sharedWith.length === 0) {
    return (
      <div className="text-sm text-gray-500 italic">
        Not shared with anyone yet
      </div>
    );
  }

  return (
    <>
      <div className="space-y-2">
        {sharedWith.map((share) => (
          <div
            key={`${share.profile}-${share.key}`}
            className={`flex items-center justify-between p-3 rounded-lg ${
              share.revoked ? 'bg-gray-100 opacity-60' : 'bg-gray-50'
            }`}
          >
            <div className="flex items-center gap-3">
              {share.profile === 'USER' ? (
                <User className="w-4 h-4 text-gray-500" />
              ) : (
                <Users className="w-4 h-4 text-gray-500" />
              )}
              <div>
                <div className="flex items-center gap-2">
                  <p className="text-sm font-medium text-gray-900">
                    {share.key}
                  </p>
                  {share.revoked && (
                    <span className="px-2 py-0.5 text-xs font-medium text-red-700 bg-red-100 rounded-full">
                      Revoked
                    </span>
                  )}
                </div>
                <p className="text-xs text-gray-500">
                  {share.profile} • Shared on{' '}
                  {new Date(share.sharedDate).toLocaleDateString()}
                  {share.revoked && share.revokedDate && (
                    <> • Revoked on {new Date(share.revokedDate).toLocaleDateString()}</>
                  )}
                </p>
              </div>
            </div>
            
            {isOwner && !share.revoked && (
              <button
                onClick={() => handleRevokeClick(share)}
                className="p-2 text-red-600 hover:bg-red-50 rounded-md transition-colors"
                aria-label={`Revoke access from ${share.key}`}
                disabled={isLoading}
              >
                <Trash2 className="w-4 h-4" />
              </button>
            )}
          </div>
        ))}
      </div>

      {/* Revoke Confirmation Dialog */}
      {revokeTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              Revoke Access
            </h3>
            <p className="text-sm text-gray-600 mb-6">
              Are you sure you want to revoke access from{' '}
              <span className="font-medium">{revokeTarget.key}</span>?
              They will no longer be able to view this application.
            </p>
            
            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}
            
            <div className="flex gap-3 justify-end">
              <button
                onClick={handleCancelRevoke}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                disabled={isLoading}
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmRevoke}
                className="px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-md hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
                disabled={isLoading}
              >
                {isLoading ? 'Revoking...' : 'Revoke Access'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
