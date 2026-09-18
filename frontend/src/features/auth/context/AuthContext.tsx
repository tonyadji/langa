import { createContext, useReducer, useEffect, type ReactNode } from 'react';
import type { User, AuthResponse } from '@/types/api';
import { config } from '@/config';

// ============================================================================
// Types
// ============================================================================

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

type AuthAction =
  | { type: 'LOGIN_SUCCESS'; payload: { user: User; accessToken: string; refreshToken: string } }
  | { type: 'LOGOUT' }
  | { type: 'REFRESH_TOKEN'; payload: { accessToken: string; refreshToken: string } }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'UPDATE_USER'; payload: User }
  | { type: 'RESTORE_SESSION'; payload: { accessToken: string; refreshToken: string; user: User | null } };

export interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, confirmationPassword: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshTokens: (refreshToken: string) => Promise<void>;
  updateUser: (user: User) => void;
  fetchUserProfile: () => Promise<void>;
}

// ============================================================================
// Initial State
// ============================================================================

const initialState: AuthState = {
  user: null,
  accessToken: null,
  refreshToken: null,
  isAuthenticated: false,
  isLoading: true, // Start loading to check localStorage
};

// ============================================================================
// Reducer
// ============================================================================

function authReducer(state: AuthState, action: AuthAction): AuthState {
  switch (action.type) {
    case 'LOGIN_SUCCESS':
      return {
        ...state,
        user: action.payload.user,
        accessToken: action.payload.accessToken,
        refreshToken: action.payload.refreshToken,
        isAuthenticated: true,
        isLoading: false,
      };
    
    case 'LOGOUT':
      return {
        ...initialState,
        isLoading: false,
      };
    
    case 'REFRESH_TOKEN':
      return {
        ...state,
        accessToken: action.payload.accessToken,
        refreshToken: action.payload.refreshToken,
      };
    
    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    
    case 'UPDATE_USER':
      return {
        ...state,
        user: action.payload,
      };
    
    case 'RESTORE_SESSION':
      return {
        ...state,
        user: action.payload.user,
        accessToken: action.payload.accessToken,
        refreshToken: action.payload.refreshToken,
        isAuthenticated: true,
        isLoading: false,
      };
    
    default:
      return state;
  }
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

export function AuthProvider({ children }: AuthProviderProps) {
  const [state, dispatch] = useReducer(authReducer, initialState);
  
  // Restore session from localStorage on mount
  useEffect(() => {
    const storedAccessToken = localStorage.getItem(config.tokenKey);
    const storedRefreshToken = localStorage.getItem(config.refreshTokenKey);
    const storedUser = localStorage.getItem('langa_user');
    
    if (storedAccessToken && storedRefreshToken) {
      // Try to restore user from localStorage
      let user: User | null = null;
      if (storedUser) {
        try {
          user = JSON.parse(storedUser);
        } catch (e) {
          console.error('Failed to parse stored user:', e);
        }
      }
      
      dispatch({
        type: 'RESTORE_SESSION',
        payload: {
          accessToken: storedAccessToken,
          refreshToken: storedRefreshToken,
          user,
        },
      });
    } else {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  }, []);
  
  // Persist tokens to localStorage
  useEffect(() => {
    if (state.accessToken && state.refreshToken) {
      localStorage.setItem(config.tokenKey, state.accessToken);
      localStorage.setItem(config.refreshTokenKey, state.refreshToken);
    }
  }, [state.accessToken, state.refreshToken]);

  // Persist user to localStorage
  useEffect(() => {
    if (state.user) {
      localStorage.setItem('langa_user', JSON.stringify(state.user));
    }
  }, [state.user]);
  
  const login = async (email: string, password: string): Promise<void> => {
    dispatch({ type: 'SET_LOADING', payload: true });
    
    try {
      // Import here to avoid circular dependencies
      const { authApi } = await import('@/services/authApi');
      const { userApi } = await import('@/services/userApi');
      
      const response: AuthResponse = await authApi.login({
        username: email,
        password,
      });
      
      // Store tokens first to make authenticated requests
      localStorage.setItem(config.tokenKey, response.accessToken);
      localStorage.setItem(config.refreshTokenKey, response.refreshToken);
      
      // Fetch user profile from /users/me
      const userProfile = await userApi.getMe();
      
      const user: User = {
        email: userProfile.email,
        username: email,
        accountKey: userProfile.accountKey,
        role: 'USER',
        firstConnection: true,
        registrationDate: new Date().toISOString(),
      };
      
      dispatch({
        type: 'LOGIN_SUCCESS',
        payload: {
          user,
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
        },
      });
    } catch (error) {
      dispatch({ type: 'SET_LOADING', payload: false });
      throw error;
    }
  };
  
  const register = async (
    email: string,
    password: string,
    confirmationPassword: string
  ): Promise<void> => {
    dispatch({ type: 'SET_LOADING', payload: true });
    
    try {
      const { authApi } = await import('@/services/authApi');
      
      await authApi.register({
        username: email,
        password,
        confirmationPassword,
      });
      
      dispatch({ type: 'SET_LOADING', payload: false });
    } catch (error) {
      dispatch({ type: 'SET_LOADING', payload: false });
      throw error;
    }
  };
  
  const logout = async (): Promise<void> => {
    try {
      const { authApi } = await import('@/services/authApi');
      await authApi.logout();
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      localStorage.removeItem(config.tokenKey);
      localStorage.removeItem(config.refreshTokenKey);
      localStorage.removeItem('langa_user');
      dispatch({ type: 'LOGOUT' });
    }
  };
  
  const refreshTokens = async (refreshToken: string): Promise<void> => {
    try {
      const { authApi } = await import('@/services/authApi');
      
      const response: AuthResponse = await authApi.refreshToken({ refreshToken });
      
      dispatch({
        type: 'REFRESH_TOKEN',
        payload: {
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
        },
      });
    } catch (error) {
      // If refresh fails, logout the user
      logout();
      throw error;
    }
  };
  
  const updateUser = (user: User): void => {
    dispatch({ type: 'UPDATE_USER', payload: user });
  };
  
  const fetchUserProfile = async (): Promise<void> => {
    try {
      const { userApi } = await import('@/services/userApi');
      const userProfile = await userApi.getMe();
      
      if (state.user) {
        const updatedUser: User = {
          ...state.user,
          email: userProfile.email,
          accountKey: userProfile.accountKey,
        };
        dispatch({ type: 'UPDATE_USER', payload: updatedUser });
      }
    } catch (error) {
      console.error('Failed to fetch user profile:', error);
      throw error;
    }
  };
  
  const value: AuthContextType = {
    ...state,
    login,
    register,
    logout,
    refreshTokens,
    updateUser,
    fetchUserProfile,
  };
  
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
