import type { AuthClient } from './AuthClient';

export interface LocalAuthClientSettings {
  /** Base URL of the Langa API, e.g. http://localhost:8080/api. */
  apiBaseUrl: string;
  /** Email used when none is given to `login`. */
  defaultEmail: string;
}

interface LocalSession {
  accessToken: string;
  /** Epoch milliseconds. */
  expiresAt: number;
  email: string;
}

interface LocalTokenResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export const LOCAL_SESSION_KEY = 'langa.local-auth';

/**
 * Local authentication mode (docker compose, demos): the backend issues the access token for an email,
 * without password nor identity provider. Requires the backend local mode (LOCAL_AUTH_ENABLED=true).
 */
export function createLocalAuthClient(settings: LocalAuthClientSettings): AuthClient {
  const listeners = new Set<() => void>();
  let session = readSession();

  const notify = () => listeners.forEach(listener => listener());

  const setSession = (value: LocalSession | null) => {
    session = value;
    try {
      if (value) {
        localStorage.setItem(LOCAL_SESSION_KEY, JSON.stringify(value));
      } else {
        localStorage.removeItem(LOCAL_SESSION_KEY);
      }
    } catch {
      // Storage unavailable: the session only lasts until the page is reloaded
    }
    notify();
  };

  const isValid = (value: LocalSession | null): value is LocalSession =>
    value !== null && value.expiresAt > Date.now();

  const login = async (redirectTo?: string, loginHint?: string) => {
    const email = (loginHint ?? settings.defaultEmail).trim();
    const response = await fetch(`${settings.apiBaseUrl}/auth/local/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });
    if (!response.ok) {
      throw new Error(`Local sign-in failed (HTTP ${response.status})`);
    }
    const token = (await response.json()) as LocalTokenResponse;
    setSession({ accessToken: token.accessToken, expiresAt: Date.now() + token.expiresIn * 1000, email });
    window.location.assign(redirectTo ?? window.location.href);
  };

  return {
    name: 'local',

    async initialize() {
      if (session && !isValid(session)) {
        setSession(null);
      }
    },

    isAuthenticated() {
      return isValid(session);
    },

    login,

    // Any email can sign in: signing up is signing in
    register: login,

    async logout() {
      setSession(null);
    },

    async getAccessToken() {
      if (!isValid(session)) {
        if (session) {
          setSession(null);
        }
        return null;
      }
      return session.accessToken;
    },

    subscribe(listener) {
      listeners.add(listener);
      return () => {
        listeners.delete(listener);
      };
    },
  };
}

function readSession(): LocalSession | null {
  try {
    const stored = localStorage.getItem(LOCAL_SESSION_KEY);
    return stored ? (JSON.parse(stored) as LocalSession) : null;
  } catch {
    return null;
  }
}
