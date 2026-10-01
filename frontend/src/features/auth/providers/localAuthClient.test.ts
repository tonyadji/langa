import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { createLocalAuthClient, LOCAL_SESSION_KEY } from './localAuthClient';

const settings = { apiBaseUrl: 'http://localhost:8080/api', defaultEmail: 'demo@langa.local' };

const tokenResponse = (expiresIn = 3600) =>
  new Response(JSON.stringify({ accessToken: 'local-token', tokenType: 'Bearer', expiresIn }), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
  });

describe('localAuthClient', () => {
  const fetchMock = vi.fn();
  const assignMock = vi.fn();

  beforeEach(() => {
    localStorage.clear();
    fetchMock.mockReset();
    assignMock.mockReset();
    vi.stubGlobal('fetch', fetchMock);
    vi.spyOn(window, 'location', 'get').mockReturnValue({
      ...window.location,
      href: 'http://localhost:3000/login',
      assign: assignMock,
    } as Location);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('should be signed out without a stored session', async () => {
    const client = createLocalAuthClient(settings);
    await client.initialize();

    expect(client.isAuthenticated()).toBe(false);
    expect(await client.getAccessToken()).toBeNull();
  });

  it('should get a token from the backend for the given email and redirect', async () => {
    fetchMock.mockResolvedValue(tokenResponse());
    const client = createLocalAuthClient(settings);
    const listener = vi.fn();
    client.subscribe(listener);

    await client.login('http://localhost:3000/dashboard', ' alice@example.com ');

    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/auth/local/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: 'alice@example.com' }),
    });
    expect(client.isAuthenticated()).toBe(true);
    expect(await client.getAccessToken()).toBe('local-token');
    expect(listener).toHaveBeenCalled();
    expect(assignMock).toHaveBeenCalledWith('http://localhost:3000/dashboard');
    expect(JSON.parse(localStorage.getItem(LOCAL_SESSION_KEY)!).email).toBe('alice@example.com');
  });

  it('should use the default email and the current page by default', async () => {
    fetchMock.mockResolvedValue(tokenResponse());
    const client = createLocalAuthClient(settings);

    await client.register();

    expect(fetchMock.mock.calls[0][1].body).toBe(JSON.stringify({ email: 'demo@langa.local' }));
    expect(assignMock).toHaveBeenCalledWith('http://localhost:3000/login');
  });

  it('should fail and stay signed out when the backend refuses', async () => {
    fetchMock.mockResolvedValue(new Response('', { status: 404 }));
    const client = createLocalAuthClient(settings);

    await expect(client.login()).rejects.toThrow('HTTP 404');
    expect(client.isAuthenticated()).toBe(false);
    expect(assignMock).not.toHaveBeenCalled();
  });

  it('should restore a stored session and drop it when expired', async () => {
    localStorage.setItem(
      LOCAL_SESSION_KEY,
      JSON.stringify({ accessToken: 'stored', expiresAt: Date.now() + 60_000, email: 'a@b.c' })
    );
    expect(await createLocalAuthClient(settings).getAccessToken()).toBe('stored');

    localStorage.setItem(
      LOCAL_SESSION_KEY,
      JSON.stringify({ accessToken: 'stored', expiresAt: Date.now() - 1, email: 'a@b.c' })
    );
    const expired = createLocalAuthClient(settings);
    await expired.initialize();
    expect(expired.isAuthenticated()).toBe(false);
    expect(localStorage.getItem(LOCAL_SESSION_KEY)).toBeNull();
  });

  it('should sign out', async () => {
    fetchMock.mockResolvedValue(tokenResponse());
    const client = createLocalAuthClient(settings);
    await client.login();

    await client.logout();

    expect(client.isAuthenticated()).toBe(false);
    expect(localStorage.getItem(LOCAL_SESSION_KEY)).toBeNull();
  });
});
