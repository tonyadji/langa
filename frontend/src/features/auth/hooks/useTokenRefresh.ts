import { useEffect, useRef } from 'react';
import { config } from '@/config';

/**
 * JWT Token payload structure
 */
interface JWTPayload {
  exp: number; // Expiration time (Unix timestamp in seconds)
  iat: number; // Issued at time
  sub: string; // Subject (usually user ID)
}

/**
 * Decode JWT token to extract payload
 * Note: This is NOT validation - server must validate tokens
 */
function decodeJWT(token: string): JWTPayload | null {
  try {
    const base64Url = token.split('.')[1];
    if (!base64Url) return null;
    
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('Failed to decode JWT:', error);
    return null;
  }
}

/**
 * Check if token is expired or will expire soon
 * @param token JWT token
 * @param bufferSeconds Time buffer before actual expiration (default 60s)
 * @returns true if token is expired or will expire within buffer time
 */
function isTokenExpiringSoon(token: string, bufferSeconds = 60): boolean {
  const payload = decodeJWT(token);
  if (!payload?.exp) return true;
  
  const now = Math.floor(Date.now() / 1000);
  const expiresIn = payload.exp - now;
  
  return expiresIn <= bufferSeconds;
}

/**
 * Hook to automatically refresh access token before it expires
 * 
 * This hook:
 * 1. Checks token expiration proactively every minute
 * 2. Refreshes token if it will expire within 5 minutes
 * 3. Handles refresh failures by logging out the user
 * 
 * Combined with the axios interceptor in api.ts that handles 401/403 errors,
 * this provides a robust token refresh mechanism.
 * 
 * @example
 * ```tsx
 * function App() {
 *   useTokenRefresh(); // Enable automatic token refresh
 *   return <Routes>...</Routes>;
 * }
 * ```
 */
export function useTokenRefresh(): void {
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const isRefreshingRef = useRef(false);

  useEffect(() => {
    const checkAndRefreshToken = async () => {
      // Skip if already refreshing
      if (isRefreshingRef.current) return;

      const accessToken = localStorage.getItem(config.tokenKey);
      const refreshToken = localStorage.getItem(config.refreshTokenKey);

      // No tokens - user not authenticated
      if (!accessToken || !refreshToken) {
        return;
      }

      // Check if token is expiring soon (within 5 minutes)
      if (isTokenExpiringSoon(accessToken, 300)) {
        isRefreshingRef.current = true;
        
        try {
          // Import dynamically to avoid circular dependencies
          const { authApi } = await import('../api/authApi');
          const response = await authApi.refreshToken(refreshToken);
          
          // Store new tokens
          localStorage.setItem(config.tokenKey, response.accessToken);
          localStorage.setItem(config.refreshTokenKey, response.refreshToken);
          
          console.log('✓ Token refreshed proactively');
        } catch (error) {
          console.error('✗ Proactive token refresh failed:', error);
          
          // Refresh failed - clear tokens and redirect to login
          localStorage.removeItem(config.tokenKey);
          localStorage.removeItem(config.refreshTokenKey);
          window.location.href = '/login';
        } finally {
          isRefreshingRef.current = false;
        }
      }
    };

    // Check immediately on mount
    checkAndRefreshToken();

    // Check every minute
    intervalRef.current = setInterval(checkAndRefreshToken, 60000);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);
}
