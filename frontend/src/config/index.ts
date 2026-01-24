export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000/api',
  apiTimeout: 30000,
  tokenKey: 'langa_auth_token',
  refreshTokenKey: 'langa_refresh_token',
} as const;
