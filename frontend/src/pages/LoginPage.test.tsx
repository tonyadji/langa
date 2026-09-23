import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthContext, type AuthContextType } from '@/features/auth/context/AuthContext';
import { LoginPage } from './LoginPage';

const buildAuth = (overrides: Partial<AuthContextType> = {}): AuthContextType => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  login: vi.fn().mockResolvedValue(undefined),
  register: vi.fn().mockResolvedValue(undefined),
  logout: vi.fn().mockResolvedValue(undefined),
  updateUser: vi.fn(),
  fetchUserProfile: vi.fn(),
  ...overrides,
});

const renderLogin = (auth: AuthContextType, { url = '/login', signUp = false } = {}) =>
  render(
    <MemoryRouter initialEntries={[url]}>
      <AuthContext.Provider value={auth}>
        <Routes>
          <Route path="/login" element={<LoginPage signUp={signUp} />} />
          <Route path="/dashboard" element={<div>Dashboard</div>} />
        </Routes>
      </AuthContext.Provider>
    </MemoryRouter>
  );

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should redirect to Entra sign-in and come back to the dashboard', async () => {
    const auth = buildAuth();
    renderLogin(auth);

    await userEvent.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(auth.login).toHaveBeenCalledWith(`${window.location.origin}/dashboard`);
    expect(auth.register).not.toHaveBeenCalled();
  });

  it('should redirect to Entra sign-up from the create account button', async () => {
    const auth = buildAuth();
    renderLogin(auth);

    await userEvent.click(screen.getByRole('button', { name: /create an account/i }));

    expect(auth.register).toHaveBeenCalled();
  });

  it('should offer sign-up first in sign-up mode', async () => {
    const auth = buildAuth();
    renderLogin(auth, { signUp: true });

    await userEvent.click(screen.getByRole('button', { name: 'Sign up' }));

    expect(auth.register).toHaveBeenCalled();
  });

  it('should redirect authenticated users to the dashboard', () => {
    renderLogin(buildAuth({ isAuthenticated: true }));

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
  });

  it('should stay on the page and offer sign-out when the backend rejected the session', async () => {
    const auth = buildAuth({ isAuthenticated: true });
    renderLogin(auth, { url: '/login?error=unauthorized' });

    expect(screen.getByText(/could not be validated/i)).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: /sign out/i }));
    expect(auth.logout).toHaveBeenCalled();
  });
});
