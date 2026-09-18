import axios, { AxiosError } from 'axios';
import { config } from '@/config';

// Unsecured endpoints that don't require authentication
const UNSECURED_ENDPOINTS = [
  '/auth/',
  '/ingestion/',
  '/team-invitations/',
  '/first-connection',
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

// Request interceptor - add auth token only for secured endpoints
api.interceptors.request.use(
  (requestConfig) => {
    const token = localStorage.getItem(config.tokenKey);
    const url = requestConfig.url || '';
    
    // Skip adding auth header for unsecured endpoints
    if (!isUnsecuredEndpoint(url) && token && requestConfig.headers) {
      requestConfig.headers.Authorization = `Bearer ${token}`;
    }
    
    return requestConfig;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Track if we're currently refreshing to avoid multiple refresh calls
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: AxiosError | null = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });
  failedQueue = [];
};

// Response interceptor - handle errors and token refresh
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as typeof error.config & { _retry?: boolean };
    const url = originalRequest?.url || '';
    const status = error.response?.status;
    
    // Handle 401 or 403 errors on secured endpoints (token expired/invalid)
    if ((status === 401 || status === 403) && !isUnsecuredEndpoint(url)) {
      // Don't retry if this is already a retry attempt or a refresh token request
      if (originalRequest?._retry || url.includes('/auth/refresh')) {
        // Clear auth data and redirect to login
        localStorage.removeItem(config.tokenKey);
        localStorage.removeItem(config.refreshTokenKey);
        window.location.href = '/login';
        return Promise.reject(error);
      }

      const refreshToken = localStorage.getItem(config.refreshTokenKey);
      
      // If no refresh token, logout immediately
      if (!refreshToken) {
        localStorage.removeItem(config.tokenKey);
        window.location.href = '/login';
        return Promise.reject(error);
      }

      // If already refreshing, queue this request
      if (isRefreshing && originalRequest) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(() => {
            return api(originalRequest);
          })
          .catch(err => {
            return Promise.reject(err);
          });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Attempt to refresh the token
        const response = await axios.post<{ accessToken: string; refreshToken: string }>(
          `${config.apiBaseUrl}/auth/refresh`,
          { refreshToken }
        );

        const { accessToken, refreshToken: newRefreshToken } = response.data;

        // Store new tokens
        localStorage.setItem(config.tokenKey, accessToken);
        localStorage.setItem(config.refreshTokenKey, newRefreshToken);

        // Update the Authorization header
        if (originalRequest?.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        }

        // Process the queue of failed requests
        processQueue(null);
        isRefreshing = false;

        // Retry the original request
        return api(originalRequest);
      } catch (refreshError) {
        // Refresh failed - logout user
        processQueue(error);
        isRefreshing = false;
        
        localStorage.removeItem(config.tokenKey);
        localStorage.removeItem(config.refreshTokenKey);
        window.location.href = '/login';
        
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;
export { api as apiClient };
