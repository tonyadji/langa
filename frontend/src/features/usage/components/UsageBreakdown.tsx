/**
 * T213: UsageBreakdown Component
 * 
 * Displays usage trends over time with a time period selector
 * (7d, 30d, 90d) per FR-032.
 * 
 * Features:
 * - Time period selector with 3 options
 * - Trend visualization chart using Recharts
 * - Separate lines for logs and metrics
 * - Loading/error/empty states
 * - Accessibility features
 */

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { useTheme } from '@/contexts/ThemeContext';
import { formatBytes } from '@/utils/formatters';
import type { ApplicationUsage } from '@/types/usage';

type TimePeriod = '7d' | '30d' | '90d';

interface UsageBreakdownProps {
  usage: ApplicationUsage | undefined;
  isLoading?: boolean;
  isError?: boolean;
  timePeriod?: TimePeriod;
  onTimePeriodChange?: (period: TimePeriod) => void;
}

export function UsageBreakdown({
  usage,
  isLoading,
  isError,
  timePeriod = '30d',
  onTimePeriodChange,
}: UsageBreakdownProps) {
  const { theme } = useTheme();
  const isDark = theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches);
  // Loading state (FR-034)
  if (isLoading) {
    return (
      <div
        data-testid="loading"
        className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6"
      >
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
          Usage Trends
        </h3>
        <div className="animate-pulse">
          <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-48 mb-4"></div>
          <div className="h-64 bg-gray-200 dark:bg-gray-700 rounded"></div>
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
          Usage Trends
        </h3>
        <p className="text-sm text-red-600 dark:text-red-400">
          Error loading usage breakdown. Please try again later.
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
          Usage Trends
        </h3>
        <p className="text-sm text-gray-500 dark:text-gray-400">
          No usage breakdown available. Usage trends will appear once the
          application has enough historical data.
        </p>
      </div>
    );
  }

  // Generate mock trend data based on time period
  // In a real implementation, this would come from the API
  const generateTrendData = (period: TimePeriod) => {
    const points = period === '7d' ? 7 : period === '30d' ? 30 : 90;
    const data = [];
    
    for (let i = 0; i < points; i++) {
      const date = new Date();
      date.setDate(date.getDate() - (points - i - 1));
      
      // Simple linear growth simulation
      const growthFactor = i / points;
      
      data.push({
        date: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
        logBytes: Math.floor(usage.totalLogBytes * growthFactor),
        metricBytes: Math.floor(usage.totalMetricBytes * growthFactor),
      });
    }
    
    return data;
  };

  const trendData = generateTrendData(timePeriod);

  // Custom tooltip formatter
  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white dark:bg-gray-800 p-3 rounded-lg border border-gray-200 dark:border-gray-700 shadow-lg">
          <p className="text-sm font-medium text-gray-900 dark:text-gray-100 mb-2">
            {payload[0].payload.date}
          </p>
          <p className="text-sm text-blue-600 dark:text-blue-400">
            Logs: {formatBytes(payload[0].value)}
          </p>
          <p className="text-sm text-green-600 dark:text-green-400">
            Metrics: {formatBytes(payload[1].value)}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <div
      data-testid="usage-breakdown"
      className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-6"
    >
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Usage Trends</h3>

        {/* Time Period Selector (FR-032) */}
        <div
          data-testid="time-period-selector"
          className="inline-flex rounded-lg border border-gray-200 dark:border-gray-700 p-1"
          role="group"
          aria-label="Time period selector"
        >
          <button
            data-testid="period-7d"
            onClick={() => onTimePeriodChange?.('7d')}
            aria-pressed={timePeriod === '7d'}
            className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
              timePeriod === '7d'
                ? 'bg-blue-600 text-white'
                : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
            }`}
          >
            7 days
          </button>
          <button
            data-testid="period-30d"
            onClick={() => onTimePeriodChange?.('30d')}
            aria-pressed={timePeriod === '30d'}
            className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
              timePeriod === '30d'
                ? 'bg-blue-600 text-white'
                : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
            }`}
          >
            30 days
          </button>
          <button
            data-testid="period-90d"
            onClick={() => onTimePeriodChange?.('90d')}
            aria-pressed={timePeriod === '90d'}
            className={`px-3 py-1.5 text-sm font-medium rounded-md transition-colors ${
              timePeriod === '90d'
                ? 'bg-blue-600 text-white'
                : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
            }`}
          >
            90 days
          </button>
        </div>
      </div>

      {/* Chart Visualization */}
      <div data-testid="breakdown-chart" className="w-full h-80">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart
            data={trendData}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#374151' : '#e5e7eb'} />
            <XAxis
              dataKey="date"
              tick={{ fill: isDark ? '#9ca3af' : '#6b7280', fontSize: 12 }}
              stroke={isDark ? '#6b7280' : '#9ca3af'}
            />
            <YAxis
              tickFormatter={(value) => formatBytes(value)}
              tick={{ fill: isDark ? '#9ca3af' : '#6b7280', fontSize: 12 }}
              stroke={isDark ? '#6b7280' : '#9ca3af'}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{ paddingTop: '20px' }}
              iconType="line"
            />
            <Line
              type="monotone"
              dataKey="logBytes"
              stroke={isDark ? '#60a5fa' : '#3b82f6'}
              strokeWidth={2}
              dot={{ fill: isDark ? '#60a5fa' : '#3b82f6', r: 4 }}
              activeDot={{ r: 6 }}
              name="Logs"
            />
            <Line
              type="monotone"
              dataKey="metricBytes"
              stroke={isDark ? '#34d399' : '#10b981'}
              strokeWidth={2}
              dot={{ fill: isDark ? '#34d399' : '#10b981', r: 4 }}
              activeDot={{ r: 6 }}
              name="Metrics"
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
