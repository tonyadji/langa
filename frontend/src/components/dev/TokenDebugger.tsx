import { useState, useEffect } from 'react';
import { config } from '@/config';

interface TokenInfo {
  exp: number;
  iat: number;
  sub: string;
}

/**
 * Development-only component to display token expiration info
 * Shows countdown to token expiration and allows manual refresh testing
 */
export function TokenDebugger() {
  const [tokenInfo, setTokenInfo] = useState<TokenInfo | null>(null);
  const [timeLeft, setTimeLeft] = useState<string>('');
  const [isVisible, setIsVisible] = useState(false);

  // Only show in development
  if (import.meta.env.PROD) {
    return null;
  }

  useEffect(() => {
    const updateTokenInfo = () => {
      const token = localStorage.getItem(config.tokenKey);
      if (!token) {
        setTokenInfo(null);
        return;
      }

      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
          atob(base64)
            .split('')
            .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
            .join('')
        );
        setTokenInfo(JSON.parse(jsonPayload));
      } catch (error) {
        console.error('Failed to decode token:', error);
        setTokenInfo(null);
      }
    };

    updateTokenInfo();
    const interval = setInterval(updateTokenInfo, 1000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (!tokenInfo?.exp) {
      setTimeLeft('No token');
      return;
    }

    const updateTimeLeft = () => {
      const now = Math.floor(Date.now() / 1000);
      const secondsLeft = tokenInfo.exp - now;

      if (secondsLeft <= 0) {
        setTimeLeft('EXPIRED');
        return;
      }

      const minutes = Math.floor(secondsLeft / 60);
      const seconds = secondsLeft % 60;
      setTimeLeft(`${minutes}m ${seconds}s`);
    };

    updateTimeLeft();
    const interval = setInterval(updateTimeLeft, 1000);

    return () => clearInterval(interval);
  }, [tokenInfo]);

  const handleManualRefresh = async () => {
    const refreshToken = localStorage.getItem(config.refreshTokenKey);
    if (!refreshToken) {
      alert('No refresh token available');
      return;
    }

    try {
      const { authApi } = await import('@/features/auth/api/authApi');
      const response = await authApi.refreshToken(refreshToken);
      localStorage.setItem(config.tokenKey, response.accessToken);
      localStorage.setItem(config.refreshTokenKey, response.refreshToken);
      alert('Token refreshed successfully!');
    } catch (error) {
      alert('Failed to refresh token: ' + (error as Error).message);
    }
  };

  const handleInvalidateToken = () => {
    localStorage.setItem(config.tokenKey, 'invalid.token.here');
    alert('Token invalidated - next API call will trigger refresh');
  };

  if (!isVisible) {
    return (
      <button
        onClick={() => setIsVisible(true)}
        className="fixed bottom-4 right-4 bg-purple-600 text-white px-3 py-2 rounded-full text-xs shadow-lg hover:bg-purple-700 z-50"
        title="Show Token Debugger"
      >
        🔐
      </button>
    );
  }

  const getExpirationColor = () => {
    if (!tokenInfo?.exp) return 'text-gray-500';
    const now = Math.floor(Date.now() / 1000);
    const secondsLeft = tokenInfo.exp - now;

    if (secondsLeft <= 0) return 'text-red-600';
    if (secondsLeft <= 300) return 'text-orange-600'; // < 5 min
    if (secondsLeft <= 600) return 'text-yellow-600'; // < 10 min
    return 'text-green-600';
  };

  return (
    <div className="fixed bottom-4 right-4 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-xl p-4 w-80 z-50">
      <div className="flex justify-between items-center mb-3">
        <h3 className="text-sm font-semibold text-gray-900 dark:text-gray-100">
          🔐 Token Debugger
        </h3>
        <button
          onClick={() => setIsVisible(false)}
          className="text-gray-500 hover:text-gray-700 dark:hover:text-gray-300"
        >
          ✕
        </button>
      </div>

      {tokenInfo ? (
        <div className="space-y-2 text-xs">
          <div className="flex justify-between">
            <span className="text-gray-600 dark:text-gray-400">Expires in:</span>
            <span className={`font-mono font-semibold ${getExpirationColor()}`}>
              {timeLeft}
            </span>
          </div>

          <div className="flex justify-between">
            <span className="text-gray-600 dark:text-gray-400">Issued at:</span>
            <span className="font-mono text-gray-900 dark:text-gray-100">
              {new Date(tokenInfo.iat * 1000).toLocaleTimeString()}
            </span>
          </div>

          <div className="flex justify-between">
            <span className="text-gray-600 dark:text-gray-400">User ID:</span>
            <span className="font-mono text-gray-900 dark:text-gray-100 truncate max-w-[150px]">
              {tokenInfo.sub}
            </span>
          </div>

          <div className="border-t border-gray-200 dark:border-gray-700 pt-2 mt-2 space-y-2">
            <button
              onClick={handleManualRefresh}
              className="w-full bg-blue-600 text-white px-3 py-1.5 rounded text-xs hover:bg-blue-700"
            >
              Manual Refresh
            </button>
            <button
              onClick={handleInvalidateToken}
              className="w-full bg-orange-600 text-white px-3 py-1.5 rounded text-xs hover:bg-orange-700"
            >
              Invalidate Token (Test)
            </button>
          </div>
        </div>
      ) : (
        <div className="text-center text-gray-500 dark:text-gray-400 py-4">
          No active token
        </div>
      )}

      <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
        <p className="text-[10px] text-gray-500 dark:text-gray-400">
          ✓ Proactive refresh: Every 60s
          <br />
          ✓ Reactive refresh: On 401/403
          <br />
          ✓ Auto-logout: On refresh fail
        </p>
      </div>
    </div>
  );
}
