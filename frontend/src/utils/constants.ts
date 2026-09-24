// Application constants

export const DEFAULT_PAGE_SIZE = 10;
export const MAX_PAGE_SIZE = 100;

export const LOG_LEVELS = ['error', 'warn', 'info', 'debug'] as const;

export const METRIC_STATUSES = ['healthy', 'unhealthy', 'degraded'] as const;

export const APPLICATION_STATUSES = ['running', 'stopped', 'error', 'deploying'] as const;

export const TIME_RANGES = ['1h', '6h', '24h', '7d', '30d'] as const;

export const METRIC_TYPES = ['cpu', 'memory', 'requests', 'errors', 'latency'] as const;

export const TEAM_ROLES = ['owner', 'admin', 'member'] as const;

export const INVITATION_STATUSES = ['pending', 'accepted', 'declined', 'expired'] as const;

export const USER_ROLES = ['admin', 'developer', 'viewer'] as const;

export const LOG_LEVEL_COLORS = {
  error: 'text-red-600 bg-red-50',
  warn: 'text-yellow-600 bg-yellow-50',
  info: 'text-blue-600 bg-blue-50',
  debug: 'text-gray-600 bg-gray-50',
} as const;

export const STATUS_COLORS = {
  running: 'text-green-600 bg-green-50',
  stopped: 'text-gray-600 bg-gray-50',
  error: 'text-red-600 bg-red-50',
  deploying: 'text-yellow-600 bg-yellow-50',
} as const;

export const HEALTH_STATUS_COLORS = {
  healthy: 'text-green-600 bg-green-50',
  unhealthy: 'text-red-600 bg-red-50',
  degraded: 'text-yellow-600 bg-yellow-50',
} as const;

export const API_TIMEOUT = 30000; // 30 seconds
export const TOKEN_REFRESH_THRESHOLD = 300000; // 5 minutes before expiry
export const MAX_RETRY_ATTEMPTS = 3;
export const RETRY_DELAY_MS = 1000;
