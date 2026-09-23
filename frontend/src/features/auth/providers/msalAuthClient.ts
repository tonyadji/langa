import {
  EventType,
  InteractionRequiredAuthError,
  PublicClientApplication,
  type AuthenticationResult,
  type Configuration,
  type RedirectRequest,
} from '@azure/msal-browser';
import type { AuthClient } from './AuthClient';

export interface MsalAuthClientSettings {
  /** SPA app registration (Application (client) ID). */
  clientId: string;
  /** Tenant authority, e.g. https://<tenant-subdomain>.ciamlogin.com/ for External ID. */
  authority: string;
  /** Delegated scope exposed by the API app registration. */
  apiScope: string;
  redirectUri: string;
  postLogoutRedirectUri: string;
}

/** Events after which the signed-in state may have changed. */
const SESSION_EVENTS: EventType[] = [
  EventType.LOGIN_SUCCESS,
  EventType.ACQUIRE_TOKEN_SUCCESS,
  EventType.ACTIVE_ACCOUNT_CHANGED,
  EventType.LOGOUT_SUCCESS,
];

/**
 * Microsoft Entra ID (workforce or External ID) implementation, based on MSAL.
 */
export function createMsalAuthClient(settings: MsalAuthClientSettings): AuthClient {
  const msalConfig: Configuration = {
    auth: {
      clientId: settings.clientId,
      authority: settings.authority,
      // External ID (ciamlogin.com) authorities must be declared as known authorities
      knownAuthorities: [new URL(settings.authority).hostname],
      redirectUri: settings.redirectUri,
      postLogoutRedirectUri: settings.postLogoutRedirectUri,
    },
    cache: {
      // Shared between tabs so users stay signed in when opening a new tab
      cacheLocation: 'localStorage',
    },
  };

  const msalInstance = new PublicClientApplication(msalConfig);

  /** Scopes requested at sign-in so the access token for the Langa API is obtained right away. */
  const loginRequest: RedirectRequest = {
    scopes: [settings.apiScope],
  };

  const redirect = (request: Partial<RedirectRequest>, redirectTo?: string) =>
    msalInstance.loginRedirect({
      ...loginRequest,
      ...request,
      redirectStartPage: redirectTo ?? window.location.href,
    });

  return {
    name: 'entra',

    async initialize() {
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
    },

    isAuthenticated() {
      return msalInstance.getActiveAccount() !== null;
    },

    login(redirectTo) {
      return redirect({}, redirectTo);
    },

    register(redirectTo) {
      // Opens the sign-up page of the External ID user flow
      return redirect({ prompt: 'create' }, redirectTo);
    },

    logout() {
      return msalInstance.logoutRedirect({ account: msalInstance.getActiveAccount() ?? undefined });
    },

    async getAccessToken() {
      const account = msalInstance.getActiveAccount();
      if (!account) {
        return null;
      }

      try {
        const result = await msalInstance.acquireTokenSilent({ ...loginRequest, account });
        return result.accessToken;
      } catch (error) {
        if (error instanceof InteractionRequiredAuthError) {
          // The session can no longer be renewed silently: sign in again
          await msalInstance.acquireTokenRedirect({ ...loginRequest, account });
          return null;
        }
        throw error;
      }
    },

    subscribe(listener) {
      const callbackId = msalInstance.addEventCallback(() => listener(), SESSION_EVENTS);
      return () => {
        if (callbackId) {
          msalInstance.removeEventCallback(callbackId);
        }
      };
    },
  };
}
