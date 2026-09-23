/**
 * Component Test: ProfilePage
 * 
 * Tests the ProfilePage component including user information display,
 * logout functionality, and authentication state.
 * 
 * ⚠️ This test MUST FAIL until ProfilePage component is implemented
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { ProfilePage } from './ProfilePage';
import { AuthContext, type AuthContextType } from '@/features/auth/context/AuthContext';
import { ThemeProvider } from '@/contexts/ThemeContext';

const mockLogout = vi.fn();
const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const mockAuthContextValue: AuthContextType = {
  user: {
    email: 'user@example.com',
    username: 'user@example.com',
    accountKey: 'acc-123',
    role: 'USER',
    firstConnection: false,
  },
  isAuthenticated: true,
  isLoading: false,
  login: vi.fn(),
  register: vi.fn(),
  logout: mockLogout,
  updateUser: vi.fn(),
  fetchUserProfile: vi.fn().mockResolvedValue(undefined),
};

describe('ProfilePage Component Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render user profile information', () => {
    render(
      <ThemeProvider>
        <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
        </ThemeProvider>
    );
    
    expect(screen.getByText(/profile/i)).toBeInTheDocument();
    expect(screen.getByText('user@example.com')).toBeInTheDocument();
  });

  it('should display user email', () => {
    render(
      <ThemeProvider>
        <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
        </ThemeProvider>
    );
    
    expect(screen.getByText('user@example.com')).toBeInTheDocument();
  });

  it('should have a logout button', () => {
    render(
      <ThemeProvider>
        <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
        </ThemeProvider>
    );
    
    const logoutButton = screen.getByRole('button', { name: /log out/i });
    expect(logoutButton).toBeInTheDocument();
  });

  it('should call logout when logout button is clicked', async () => {
    const user = userEvent.setup();
    
    render(
      <ThemeProvider>
        <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
        </ThemeProvider>
    );
    
    const logoutButton = screen.getByRole('button', { name: /log out/i });
    await user.click(logoutButton);
    
    expect(mockLogout).toHaveBeenCalledTimes(1);
  });

  it('should redirect to login after logout', async () => {
    const user = userEvent.setup();
    
    render(
      <ThemeProvider>
        <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
        </ThemeProvider>
    );
    
    const logoutButton = screen.getByRole('button', { name: /log out/i });
    await user.click(logoutButton);
    
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  it('should show the account key', () => {
    render(
      <ThemeProvider>
        <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
        </ThemeProvider>
    );
    
    expect(screen.getByText('acc-123')).toBeInTheDocument();
  });

  it('should redirect to login if user is not authenticated', () => {
    const unauthenticatedContext = {
      ...mockAuthContextValue,
      user: null,
      isAuthenticated: false,
    };

    render(
      <ThemeProvider>
        <BrowserRouter>
        <AuthContext.Provider value={unauthenticatedContext}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
        </ThemeProvider>
    );
    
    // Should redirect immediately
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });
});
