export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000/api',
  apiTimeout: 30000,
  entra: {
    // "langa-spa" app registration (Application (client) ID)
    clientId: import.meta.env.VITE_ENTRA_CLIENT_ID || '22222222-2222-2222-2222-222222222222',
    // External ID tenant authority: https://<tenant-subdomain>.ciamlogin.com/
    authority: import.meta.env.VITE_ENTRA_AUTHORITY || 'https://langa.ciamlogin.com/',
    // Delegated scope exposed by the "langa-api" app registration
    apiScope:
      import.meta.env.VITE_ENTRA_API_SCOPE || 'api://11111111-1111-1111-1111-111111111111/access_as_user',
    redirectUri: import.meta.env.VITE_ENTRA_REDIRECT_URI || window.location.origin,
    postLogoutRedirectUri:
      import.meta.env.VITE_ENTRA_POST_LOGOUT_REDIRECT_URI || `${window.location.origin}/login`,
  },
} as const;
