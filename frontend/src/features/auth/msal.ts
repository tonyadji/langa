import {
  EventType,
  InteractionRequiredAuthError,
  PublicClientApplication,
  type AuthenticationResult,
  type Configuration,
  type RedirectRequest,
} from '@azure/msal-browser';
import { config } from '@/config';

const msalConfig: Configuration = {
  auth: {
    clientId: config.entra.clientId,
    authority: config.entra.authority,
    // External ID (ciamlogin.com) authorities must be declared as known authorities
    knownAuthorities: [new URL(config.entra.authority).hostname],
    redirectUri: config.entra.redirectUri,
    postLogoutRedirectUri: config.entra.postLogoutRedirectUri,
  },
  cache: {
    // Shared between tabs so users stay signed in when opening a new tab
    cacheLocation: 'localStorage',
  },
};

export const msalInstance = new PublicClientApplication(msalConfig);

/** Scopes requested at sign-in so the access token for the Langa API is obtained right away. */
export const loginRequest: RedirectRequest = {
  scopes: [config.entra.apiScope],
};

/**
 * Initializes MSAL, processes the redirect response (if any) and selects the active account.
 * Must be awaited before rendering the application.
 */
export async function initializeMsal(): Promise<void> {
  await msalInstance.initialize();

  msalInstance.addEventCallback(event => {
    if (
      (event.eventType === EventType.LOGIN_SUCCESS ||
        event.eventType === EventType.ACQUIRE_TOKEN_SUCCESS) &&
      event.payload
    ) {
      const { account } = event.payload as AuthenticationResult;
      if (account) {
        msalInstance.setActiveAccount(account);
      }
    }
  });

  const result = await msalInstance.handleRedirectPromise();
  if (result?.account) {
    msalInstance.setActiveAccount(result.account);
  } else if (!msalInstance.getActiveAccount()) {
    const [firstAccount] = msalInstance.getAllAccounts();
    if (firstAccount) {
      msalInstance.setActiveAccount(firstAccount);
    }
  }
}

/**
 * Returns an access token for the Langa API, silently refreshed by MSAL when needed.
 * Returns null when nobody is signed in; starts an interactive sign-in when the session
 * can no longer be renewed silently.
 */
export async function getAccessToken(): Promise<string | null> {
  const account = msalInstance.getActiveAccount();
  if (!account) {
    return null;
  }

  try {
    const result = await msalInstance.acquireTokenSilent({ ...loginRequest, account });
    return result.accessToken;
  } catch (error) {
    if (error instanceof InteractionRequiredAuthError) {
      await msalInstance.acquireTokenRedirect({ ...loginRequest, account });
      return null;
    }
    throw error;
  }
}
