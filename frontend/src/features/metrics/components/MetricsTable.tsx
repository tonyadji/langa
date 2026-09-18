/**
 * MetricsTable Component
 * 
 * Displays paginated metrics data in a table format with sorting and filtering.
 */

import { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';
import type { MetricEntry } from '@/types';

interface MetricsTableProps {
  metrics: MetricEntry[];
}

// Format timestamp to match LogsTable format
function formatTimestamp(timestamp: string | number): string {
  try {
    let date: Date;
    if (typeof timestamp === 'number') {
      date = new Date(timestamp);
    } else if (/^\d+$/.test(String(timestamp))) {
      date = new Date(Number(timestamp));
    } else {
      date = new Date(timestamp);
    }
    
    if (isNaN(date.getTime())) {
      return String(timestamp);
    }
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      fractionalSecondDigits: 3,
    });
  } catch (error) {
    return String(timestamp);
  }
}

export function MetricsTable({ metrics }: MetricsTableProps) {
  const [sortField, setSortField] = useState<keyof MetricEntry>('timestamp');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');

  const handleSort = (field: keyof MetricEntry) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('desc');
    }
  };

  const SortIcon = ({ field }: { field: keyof MetricEntry }) => {
    if (sortField !== field) return null;
    return sortDirection === 'asc' ? (
      <ChevronUp className="w-4 h-4 inline" />
    ) : (
      <ChevronDown className="w-4 h-4 inline" />
    );
  };

  const sortedMetrics = [...metrics].sort((a, b) => {
    const aValue = a[sortField];
    const bValue = b[sortField];

    if (aValue === undefined || bValue === undefined) return 0;
    
    if (aValue < bValue) return sortDirection === 'asc' ? -1 : 1;
    if (aValue > bValue) return sortDirection === 'asc' ? 1 : -1;
    return 0;
  });

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'success':
        return 'text-green-600 dark:text-green-400 bg-green-50 dark:bg-green-900/20';
      case 'error':
      case 'failed':
      case 'failure':
        return 'text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20';
      case 'timeout':
        return 'text-yellow-600 dark:text-yellow-400 bg-yellow-50 dark:bg-yellow-900/20';
      default:
        return 'text-gray-600 dark:text-gray-400 bg-gray-50 dark:bg-gray-800';
    }
  };

  const getHttpStatusColor = (status?: number) => {
    if (!status) return 'text-gray-600 dark:text-gray-400';
    if (status >= 200 && status < 300) {
      return 'text-green-600 dark:text-green-400';
    } else if (status >= 400 && status < 500) {
      return 'text-yellow-600 dark:text-yellow-400';
    } else if (status >= 500) {
      return 'text-red-600 dark:text-red-400';
    }
    return 'text-gray-600 dark:text-gray-400';
  };

  const formatDuration = (millis: number) => {
    if (millis < 1000) return `${millis}ms`;
    return `${(millis / 1000).toFixed(2)}s`;
  };

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead className="bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 sticky top-0 z-10">
          <tr>
            <th
              onClick={() => handleSort('name')}
              className="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 bg-gray-50 dark:bg-gray-900"
            >
              Name <SortIcon field="name" />
            </th>
            <th
              onClick={() => handleSort('httpMethod')}
              className="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 bg-gray-50 dark:bg-gray-900"
            >
              Method <SortIcon field="httpMethod" />
            </th>
            <th
              onClick={() => handleSort('uri')}
              className="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 bg-gray-50 dark:bg-gray-900"
            >
              URI <SortIcon field="uri" />
            </th>
            <th
              onClick={() => handleSort('httpStatus')}
              className="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 bg-gray-50 dark:bg-gray-900"
            >
              Status <SortIcon field="httpStatus" />
            </th>
            <th
              onClick={() => handleSort('durationMillis')}
              className="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 bg-gray-50 dark:bg-gray-900"
            >
              Duration <SortIcon field="durationMillis" />
            </th>
            <th
              onClick={() => handleSort('status')}
              className="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 bg-gray-50 dark:bg-gray-900"
            >
              Result <SortIcon field="status" />
            </th>
            <th
              onClick={() => handleSort('timestamp')}
              className="px-6 py-3 text-left text-xs font-medium text-gray-700 dark:text-gray-300 uppercase tracking-wider cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-800 bg-gray-50 dark:bg-gray-900"
            >
              Time <SortIcon field="timestamp" />
            </th>
          </tr>
        </thead>
        <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
          {sortedMetrics.map((metric, index) => (
            <tr
              key={`${metric.timestamp}-${index}`}
              className="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
            >
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">
                {metric.name}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm">
                {metric.httpMethod && (
                    <span className="px-2 py-1 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 rounded font-mono text-xs">
                    {metric.httpMethod}
                    </span>
                )}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600 dark:text-gray-300 font-mono max-w-xs truncate" title={metric.uri}>
                {metric.uri}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm">
                <span className={`font-semibold ${getHttpStatusColor(metric.httpStatus)}`}>
                  {metric.httpStatus}
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-100 font-mono">
                {formatDuration(metric.durationMillis)}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm">
                <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(metric.status)}`}>
                  {metric.status}
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                {formatTimestamp(metric.timestamp)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
