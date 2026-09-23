import axios, { AxiosError } from 'axios';
import { config } from '@/config';
import { getAccessToken } from '@/features/auth/msal';

// Unsecured endpoints that don't require authentication
const UNSECURED_ENDPOINTS = [
  '/ingestion/',
  '/team-invitations/',
  '/swagger-ui',
  '/swagger-config',
  '/swagger-resources/',
  '/v2/api-docs',
  '/v3/api-docs/',
  '/configuration/ui',
  '/webjars/',
  '/actuator/',
];

/**
 * Check if the request URL matches any unsecured endpoint patterns
 */
function isUnsecuredEndpoint(url: string = ''): boolean {
  return UNSECURED_ENDPOINTS.some(pattern => url.includes(pattern));
}

// Create axios instance
const api = axios.create({
  baseURL: config.apiBaseUrl,
  timeout: config.apiTimeout,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - add the Entra access token (silently renewed by MSAL) when signed in
api.interceptors.request.use(
  async (requestConfig) => {
    const token = await getAccessToken();

    if (token && requestConfig.headers) {
      requestConfig.headers.Authorization = `Bearer ${token}`;
    }

    return requestConfig;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response interceptor - the backend rejected the session: go back to the login page.
// No automatic sign-in here, to avoid a redirect loop if the token keeps being rejected.
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const url = error.config?.url || '';

    if (error.response?.status === 401 && !isUnsecuredEndpoint(url)) {
      window.location.href = '/login?error=unauthorized';
    }

    return Promise.reject(error);
  }
);

export default api;
export { api as apiClient };
