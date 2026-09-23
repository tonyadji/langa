import { createContext, useCallback, useEffect, useState, useSyncExternalStore, type ReactNode } from 'react';
import type { User } from '@/types/api';
import { authClient } from '@/features/auth/providers';

// ============================================================================
// Types
// ============================================================================

export interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface AuthContextType extends AuthState {
  /** Redirects to the identity provider sign-in page, then back to `redirectTo` (current page by default). */
  login: (redirectTo?: string) => Promise<void>;
  /** Redirects to the identity provider sign-up page, then back to `redirectTo` (current page by default). */
  register: (redirectTo?: string) => Promise<void>;
  logout: () => Promise<void>;
  updateUser: (user: User) => void;
  fetchUserProfile: () => Promise<void>;
}

// ============================================================================
// Context
// ============================================================================

// eslint-disable-next-line react-refresh/only-export-components -- the context belongs with its provider
export const AuthContext = createContext<AuthContextType | null>(null);

// ============================================================================
// Provider
// ============================================================================

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Exposes the identity provider session (see AuthClient) and the Langa user profile.
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const isAuthenticated = useSyncExternalStore(authClient.subscribe, authClient.isAuthenticated);
  const [user, setUser] = useState<User | null>(null);
  const [profileFailed, setProfileFailed] = useState(false);

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
      setProfileFailed(false);
      return;
    }
    if (user || profileFailed) {
      return;
    }

    fetchUserProfile().catch(error => {
      console.error('Failed to fetch user profile:', error);
      setProfileFailed(true);
    });
  }, [isAuthenticated, user, profileFailed, fetchUserProfile]);

  const login = useCallback((redirectTo?: string) => authClient.login(redirectTo), []);

  const register = useCallback((redirectTo?: string) => authClient.register(redirectTo), []);

  const logout = useCallback(async (): Promise<void> => {
    setUser(null);
    await authClient.logout();
  }, []);

  const updateUser = useCallback((updatedUser: User): void => {
    setUser(updatedUser);
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    // Signed in but the Langa profile is not loaded yet
    isLoading: isAuthenticated && !user && !profileFailed,
    login,
    register,
    logout,
    updateUser,
    fetchUserProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
