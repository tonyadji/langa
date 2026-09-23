import { createContext, useCallback, useEffect, useState, type ReactNode } from 'react';
import { InteractionStatus } from '@azure/msal-browser';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';
import type { User } from '@/types/api';
import { loginRequest } from '@/features/auth/msal';

// ============================================================================
// Types
// ============================================================================

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface AuthContextType extends AuthState {
  /** Redirects to the Entra sign-in page, then back to `redirectTo` (current page by default). */
  login: (redirectTo?: string) => Promise<void>;
  /** Redirects to the Entra sign-up page, then back to `redirectTo` (current page by default). */
  register: (redirectTo?: string) => Promise<void>;
  logout: () => Promise<void>;
  updateUser: (user: User) => void;
  fetchUserProfile: () => Promise<void>;
}

// ============================================================================
// Context
// ============================================================================

export const AuthContext = createContext<AuthContextType | null>(null);

// ============================================================================
// Provider
// ============================================================================

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Exposes the Entra ID session (handled by MSAL) and the Langa user profile.
 * Must be rendered inside an MsalProvider.
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const { instance, inProgress } = useMsal();
  const isAuthenticated = useIsAuthenticated();
  const [user, setUser] = useState<User | null>(null);
  const [isProfileLoading, setIsProfileLoading] = useState(false);

  const fetchUserProfile = useCallback(async (): Promise<void> => {
    // Import here to avoid circular dependencies
    const { userApi } = await import('@/services/userApi');
    const userProfile = await userApi.getMe();

    setUser(previous => ({
      role: 'USER',
      firstConnection: false,
      ...previous,
      email: userProfile.email,
      // Applications and teams reference their owner/members by the Langa email
      username: userProfile.email,
      accountKey: userProfile.accountKey,
    }));
  }, []);

  // Load the Langa profile once signed in (this also provisions the user on the backend)
  useEffect(() => {
    if (!isAuthenticated) {
      setUser(null);
      return;
    }
    if (user || inProgress !== InteractionStatus.None) {
      return;
    }

    setIsProfileLoading(true);
    fetchUserProfile()
      .catch(error => console.error('Failed to fetch user profile:', error))
      .finally(() => setIsProfileLoading(false));
  }, [isAuthenticated, inProgress, user, fetchUserProfile]);

  const login = useCallback(async (redirectTo?: string): Promise<void> => {
    await instance.loginRedirect({
      ...loginRequest,
      redirectStartPage: redirectTo ?? window.location.href,
    });
  }, [instance]);

  const register = useCallback(async (redirectTo?: string): Promise<void> => {
    await instance.loginRedirect({
      ...loginRequest,
      // Opens the sign-up page of the External ID user flow
      prompt: 'create',
      redirectStartPage: redirectTo ?? window.location.href,
    });
  }, [instance]);

  const logout = useCallback(async (): Promise<void> => {
    setUser(null);
    await instance.logoutRedirect({ account: instance.getActiveAccount() ?? undefined });
  }, [instance]);

  const updateUser = useCallback((updatedUser: User): void => {
    setUser(updatedUser);
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading: inProgress !== InteractionStatus.None || isProfileLoading,
    login,
    register,
    logout,
    updateUser,
    fetchUserProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
