import type { PaginationParams } from './common';

export const LogLevel = {
  TRACE: 'TRACE',
  DEBUG: 'DEBUG',
  INFO: 'INFO',
  WARN: 'WARN',
  ERROR: 'ERROR',
  FATAL: 'FATAL',
} as const;

export type LogLevel = typeof LogLevel[keyof typeof LogLevel];

export interface LogEntry {
  appKey: string;             // Application identifier
  accountKey: string;         // Account identifier
  message: string;            // Log message content
  level: LogLevel;            // Log severity level
  loggerName: string;         // Logger/class name
  timestamp: string;          // ISO 8601 timestamp
  threadName?: string;        // Thread name (optional)
  stackTrace?: string;        // Stack trace for errors (optional)
  mdc?: Record<string, string>;  // Mapped Diagnostic Context (optional)
}

export interface LogFilterParams extends PaginationParams {
  level?: string;             // Filter by log level
  keyword?: string;           // Search keyword (searches across message, logger, etc.)
  startDate?: string;         // ISO 8601 start timestamp
  endDate?: string;           // ISO 8601 end timestamp
}

export interface LogsState {
  logs: LogEntry[];
  total: number;
  loading: boolean;
  error: string | null;
  filters: LogFilterParams;
}
