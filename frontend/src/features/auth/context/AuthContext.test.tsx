import { describe, it, expect, vi, beforeEach } from 'vitest';
import { act, renderHook, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { AuthProvider } from './AuthContext';
import { useAuth } from '../hooks/useAuth';

// Fake identity provider whose session can be changed by the tests
const fake = vi.hoisted(() => {
  const listeners = new Set<() => void>();
  const state = { authenticated: false };
  return {
    state,
    setAuthenticated(value: boolean) {
      state.authenticated = value;
      listeners.forEach(listener => listener());
    },
    authClient: {
      name: 'fake',
      initialize: vi.fn(async () => {}),
      isAuthenticated: () => state.authenticated,
      login: vi.fn(async () => {}),
      register: vi.fn(async () => {}),
      logout: vi.fn(async () => {}),
      getAccessToken: vi.fn(async () => null),
      subscribe: (listener: () => void) => {
        listeners.add(listener);
        return () => listeners.delete(listener);
      },
    },
  };
});

const getMe = vi.hoisted(() => vi.fn());

vi.mock('@/features/auth/providers', () => ({ authClient: fake.authClient }));
vi.mock('@/services/userApi', () => ({ userApi: { getMe } }));

const wrapper = ({ children }: { children: ReactNode }) => <AuthProvider>{children}</AuthProvider>;

describe('AuthProvider', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    fake.state.authenticated = false;
    getMe.mockResolvedValue({ email: 'user@example.com', accountKey: 'acc-1' });
  });

  it('should be signed out without a provider session', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.isLoading).toBe(false);
    expect(result.current.user).toBeNull();
    expect(getMe).not.toHaveBeenCalled();
  });

  it('should load the Langa profile when signed in', async () => {
    fake.state.authenticated = true;

    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.isLoading).toBe(true);
    await waitFor(() => expect(result.current.user).not.toBeNull());
    expect(result.current.isLoading).toBe(false);
    expect(result.current.user).toMatchObject({
      email: 'user@example.com',
      username: 'user@example.com',
      accountKey: 'acc-1',
    });
  });

  it('should stop loading when the profile cannot be fetched', async () => {
    fake.state.authenticated = true;
    getMe.mockRejectedValue(new Error('boom'));
    vi.spyOn(console, 'error').mockImplementation(() => {});

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.user).toBeNull();
    expect(getMe).toHaveBeenCalledTimes(1);
  });

  it('should follow session changes notified by the provider', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    act(() => fake.setAuthenticated(true));
    await waitFor(() => expect(result.current.user?.email).toBe('user@example.com'));

    act(() => fake.setAuthenticated(false));
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
  });

  it('should delegate sign-in, sign-up and sign-out to the provider', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    await result.current.login('http://localhost/teams');
    await result.current.register();
    await result.current.logout();

    expect(fake.authClient.login).toHaveBeenCalledWith('http://localhost/teams');
    expect(fake.authClient.register).toHaveBeenCalledWith(undefined);
    expect(fake.authClient.logout).toHaveBeenCalled();
  });
});
