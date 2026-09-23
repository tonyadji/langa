import { config } from '@/config';
import type { AuthClient } from './AuthClient';
import { createMsalAuthClient } from './msalAuthClient';

export type { AuthClient } from './AuthClient';

/**
 * Builds the client of the identity provider selected with VITE_AUTH_PROVIDER.
 * To support another provider (e.g. Cognito with oidc-client-ts), implement AuthClient
 * and add it here: the rest of the application does not change.
 */
function createAuthClient(): AuthClient {
  switch (config.auth.provider) {
    case 'entra':
      return createMsalAuthClient(config.auth.entra);
    default:
      throw new Error(`Unsupported authentication provider: ${config.auth.provider}`);
  }
}

export const authClient: AuthClient = createAuthClient();
