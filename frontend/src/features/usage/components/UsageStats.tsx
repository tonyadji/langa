/**
 * T212: UsageStats Component
 * 
 * Displays application storage usage statistics with separate displays
 * for log bytes and metric bytes (FR-031).
 * 
 * Features:
 * - Human-readable byte formatting
 * - Loading state with skeleton
 * - Error state with user-friendly message
 * - Empty state for new applications
 * - Visual icons for clarity (T221)
 */

import { Database, FileText, BarChart } from 'lucide-react';
import { formatBytes } from '@/utils/formatters';
import type { ApplicationUsage } from '@/types/usage';

interface UsageStatsProps {
  usage: ApplicationUsage | undefined;
  isLoading?: boolean;
  isError?: boolean;
}

export function UsageStats({ usage, isLoading, isError }: UsageStatsProps) {
  // Loading state (FR-034)
  if (isLoading) {
    return (
      <div
        data-testid="loading"
        className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6"
      >
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
          Storage Usage
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-16 mb-2"></div>
            <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-24"></div>
          </div>
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-16 mb-2"></div>
            <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-24"></div>
          </div>
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-16 mb-2"></div>
            <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-24"></div>
          </div>
        </div>
      </div>
    );
  }

  // Error state (FR-033)
  if (isError) {
    return (
      <div
        data-testid="error"
        className="bg-white dark:bg-gray-800 rounded-lg border border-red-200 dark:border-red-800 p-6"
      >
        <h3 className="text-lg font-semibold text-red-900 dark:text-red-400 mb-2">
          Storage Usage
        </h3>
        <p className="text-sm text-red-600 dark:text-red-400">
          Error loading usage statistics. Please try again later.
        </p>
      </div>
    );
  }

  // Empty state (FR-035)
  if (!usage) {
    return (
      <div
        data-testid="no-data"
        className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6"
      >
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
          Storage Usage
        </h3>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          No usage data available yet. Usage statistics will appear once the
          application starts sending logs or metrics.
        </p>
      </div>
    );
  }

  // Calculate total usage
  const totalBytes = usage.totalLogBytes + usage.totalMetricBytes;

  // Data display (FR-031)
  return (
    <div
      data-testid="usage-stats"
      className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6"
    >
      <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
        Storage Usage
      </h3>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Log Bytes */}
        <div className="flex items-start gap-3">
          <div className="p-2 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <FileText className="w-5 h-5 text-blue-600 dark:text-blue-400" />
          </div>
          <div className="flex-1">
            <p className="text-sm font-medium text-gray-600 dark:text-gray-300 mb-1">Logs</p>
            <p
              data-testid="total-log-bytes"
              className="text-2xl font-semibold text-gray-900 dark:text-gray-100"
              title={`${usage.totalLogBytes} bytes`}
            >
              {formatBytes(usage.totalLogBytes)}
            </p>
          </div>
        </div>

        {/* Metric Bytes */}
        <div className="flex items-start gap-3">
          <div className="p-2 bg-green-50 dark:bg-green-900/20 rounded-lg">
            <BarChart className="w-5 h-5 text-green-600 dark:text-green-400" />
          </div>
          <div className="flex-1">
            <p className="text-sm font-medium text-gray-600 dark:text-gray-300 mb-1">Metrics</p>
            <p
              data-testid="total-metric-bytes"
              className="text-2xl font-semibold text-gray-900 dark:text-gray-100"
              title={`${usage.totalMetricBytes} bytes`}
            >
              {formatBytes(usage.totalMetricBytes)}
            </p>
          </div>
        </div>

        {/* Total Bytes */}
        <div className="flex items-start gap-3">
          <div className="p-2 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
            <Database className="w-5 h-5 text-purple-600 dark:text-purple-400" />
          </div>
          <div className="flex-1">
            <p className="text-sm font-medium text-gray-600 dark:text-gray-300 mb-1">Total</p>
            <p
              data-testid="total-bytes"
              className="text-2xl font-semibold text-gray-900 dark:text-gray-100"
              title={`${totalBytes} bytes`}
            >
              {formatBytes(totalBytes)}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
