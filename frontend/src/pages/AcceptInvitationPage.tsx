/**
 * AcceptInvitationPage
 * 
 * Page for accepting team invitations via invitation link
 * URL format: /accept-invitation/:teamId/:token
 */

import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { CheckCircle, XCircle, Loader2 } from 'lucide-react';
import { useTeamInvitations } from '@/features/teams/hooks/useTeamInvitations';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { getApiErrorMessage } from '@/services/apiError';

export const AcceptInvitationPage: React.FC = () => {
  const { teamId, token } = useParams<{ teamId: string; token: string }>();
  const navigate = useNavigate();
  const { acceptInvitation } = useTeamInvitations();
  
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [teamName, setTeamName] = useState<string>('');
  const { user, isAuthenticated, isLoading: isAuthLoading, login } = useAuth();
  const currentUserEmail = user?.email ?? '';
  const handledRef = useRef(false);

  useEffect(() => {
    const handleAcceptInvitation = async () => {
      // Wait for the session and the Langa profile to be resolved
      if (isAuthLoading || (isAuthenticated && !currentUserEmail)) {
        return;
      }
      if (handledRef.current) {
        return;
      }
      handledRef.current = true;
      if (!isAuthenticated) {
        // Sign in (or sign up) with the invited email, then come back to this page
        await login(window.location.href);
        return;
      }
      if (!teamId || !token) {
        setStatus('error');
        setErrorMessage('Invalid invitation link');
        return;
      }

      try {
        const response = await acceptInvitation(teamId, token);
        setTeamName(response.team);
        setStatus('success');
        
        // Redirect to teams page after 3 seconds
        setTimeout(() => {
          navigate('/teams');
        }, 3000);
      } catch (err) {
        setStatus('error');
        setErrorMessage(getApiErrorMessage(err, 'Failed to accept invitation'));
      }
    };

    handleAcceptInvitation();
  }, [teamId, token, currentUserEmail, isAuthenticated, isAuthLoading, login, acceptInvitation, navigate]);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-white dark:bg-gray-800 rounded-lg shadow-lg dark:shadow-gray-900/50 border border-gray-200 dark:border-gray-700 p-8">
        {status === 'loading' && (
          <div className="text-center">
            <Loader2 className="w-16 h-16 text-blue-600 dark:text-blue-400 mx-auto mb-4 animate-spin" />
            <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-2">
              Accepting Invitation
            </h1>
            <p className="text-gray-600 dark:text-gray-400">
              Please wait while we process your invitation...
            </p>
          </div>
        )}

        {status === 'success' && (
          <div className="text-center">
            <CheckCircle className="w-16 h-16 text-green-600 dark:text-green-400 mx-auto mb-4" />
            <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-2">
              Invitation Accepted!
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mb-4">
              You have successfully joined the team <span className="font-semibold">{teamName}</span>.
            </p>
            <p className="text-sm text-gray-500 dark:text-gray-500">
              Redirecting to teams page...
            </p>
          </div>
        )}

        {status === 'error' && (
          <div className="text-center">
            <XCircle className="w-16 h-16 text-red-600 dark:text-red-400 mx-auto mb-4" />
            <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-2">
              Invitation Failed
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              {errorMessage}
            </p>
            <div className="flex gap-3 justify-center">
              <button
                onClick={() => navigate('/teams')}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600"
              >
                Go to Teams
              </button>
              <button
                onClick={() => navigate('/dashboard')}
                className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600"
              >
                Go to Dashboard
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
