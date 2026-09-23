import { Navigate, useLocation, useSearchParams } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { Card } from '@/components/common/Card';
import { Button } from '@/components/common/Button';
import { Alert } from '@/components/common/Alert';

interface LoginPageProps {
  /** Show the sign-up call to action first (used by the /register route). */
  signUp?: boolean;
}

/**
 * Entry point of the authentication: sign-in and sign-up are handled by
 * Microsoft Entra External ID, this page only redirects to it.
 */
export function LoginPage({ signUp = false }: LoginPageProps) {
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const { isAuthenticated, isLoading, login, register, logout } = useAuth();

  const sessionRejected = searchParams.get('error') === 'unauthorized';
  const from = (location.state as { from?: string } | null)?.from ?? '/dashboard';
  const redirectTo = `${window.location.origin}${from}`;

  if (isAuthenticated && !sessionRejected) {
    return <Navigate to={from} replace />;
  }

  const primaryAction = signUp ? () => register(redirectTo) : () => login(redirectTo);
  const secondaryAction = signUp ? () => login(redirectTo) : () => register(redirectTo);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center p-4">
      <Card className="w-full max-w-md p-8">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">
            {signUp ? 'Create your account' : 'Welcome Back'}
          </h1>
          <p className="text-gray-600 dark:text-gray-300">
            {signUp ? 'Sign up to start monitoring with Langa' : 'Sign in to your Langa account'}
          </p>
        </div>

        {sessionRejected && (
          <Alert variant="error" className="mb-6">
            Your session could not be validated. Please sign in again.
          </Alert>
        )}

        <div className="space-y-4">
          <Button fullWidth size="lg" isLoading={isLoading} onClick={primaryAction}>
            {signUp ? 'Sign up' : 'Sign in'}
          </Button>
          <Button fullWidth variant="secondary" disabled={isLoading} onClick={secondaryAction}>
            {signUp ? 'I already have an account' : 'Create an account'}
          </Button>
          {sessionRejected && isAuthenticated && (
            <Button fullWidth variant="ghost" disabled={isLoading} onClick={logout}>
              Sign out
            </Button>
          )}
        </div>
      </Card>
    </div>
  );
}
