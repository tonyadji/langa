/**
 * Port between the application and the identity provider (Entra ID, Cognito, Keycloak...).
 *
 * The rest of the application only depends on this interface (through `useAuth` and the
 * API client): supporting a new provider means adding an implementation and selecting it
 * with `VITE_AUTH_PROVIDER`, see `./index.ts`.
 */
export interface AuthClient {
  /** Provider name, e.g. `entra`. */
  readonly name: string;

  /**
   * Prepares the client and processes the sign-in redirect response, if any.
   * Awaited before the application renders.
   */
  initialize(): Promise<void>;

  /** Whether a user is signed in. Must be cheap: it is read on every render. */
  isAuthenticated(): boolean;

  /** Redirects to the provider sign-in page, then back to `redirectTo` (current page by default). */
  login(redirectTo?: string): Promise<void>;

  /** Redirects to the provider sign-up page, then back to `redirectTo` (current page by default). */
  register(redirectTo?: string): Promise<void>;

  /** Signs out from the application and the provider. */
  logout(): Promise<void>;

  /**
   * Access token for the Langa API, renewed silently when needed.
   * Returns null when nobody is signed in.
   */
  getAccessToken(): Promise<string | null>;

  /** Notifies `listener` when the session changes; returns the unsubscribe function. */
  subscribe(listener: () => void): () => void;
}
