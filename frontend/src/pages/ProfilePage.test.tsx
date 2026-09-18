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
import { AuthContext } from '@/features/auth/context/AuthContext';

const mockLogout = vi.fn();
const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const mockAuthContextValue = {
  user: {
    id: '123',
    email: 'user@example.com',
    createdAt: '2025-01-01T00:00:00Z',
  },
  isAuthenticated: true,
  isLoading: false,
  accessToken: 'mock-token',
  refreshToken: 'mock-refresh-token',
  login: vi.fn(),
  register: vi.fn(),
  logout: mockLogout,
  refreshTokens: vi.fn(),
};

describe('ProfilePage Component Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render user profile information', () => {
    render(
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
    );
    
    expect(screen.getByText(/profile/i)).toBeInTheDocument();
    expect(screen.getByText('user@example.com')).toBeInTheDocument();
  });

  it('should display user email', () => {
    render(
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
    );
    
    expect(screen.getByText('user@example.com')).toBeInTheDocument();
  });

  it('should have a logout button', () => {
    render(
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
    );
    
    const logoutButton = screen.getByRole('button', { name: /log out/i });
    expect(logoutButton).toBeInTheDocument();
  });

  it('should call logout when logout button is clicked', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
    );
    
    const logoutButton = screen.getByRole('button', { name: /log out/i });
    await user.click(logoutButton);
    
    expect(mockLogout).toHaveBeenCalledTimes(1);
  });

  it('should redirect to login after logout', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
    );
    
    const logoutButton = screen.getByRole('button', { name: /log out/i });
    await user.click(logoutButton);
    
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  it('should show user ID if available', () => {
    render(
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
    );
    
    expect(screen.getByText(/123/)).toBeInTheDocument();
  });

  it('should redirect to login if user is not authenticated', () => {
    const unauthenticatedContext = {
      ...mockAuthContextValue,
      user: null,
      isAuthenticated: false,
    };

    render(
      <BrowserRouter>
        <AuthContext.Provider value={unauthenticatedContext}>
          <ProfilePage />
        </AuthContext.Provider>
      </BrowserRouter>
    );
    
    // Should redirect immediately
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });
});
