import React, { useState } from 'react';
import { ChevronDown, ChevronRight, FileText } from 'lucide-react';
import type { LogEntry } from '@/types';
import { Badge, type BadgeProps } from '@/components/common/Badge';
import { EmptyState } from '@/components/common/EmptyState';
import { Pagination } from '@/components/common/Pagination';
import { Spinner } from '@/components/common/Spinner';
import { formatTimestamp } from '../utils/dates';

interface LogsTableProps {
  logs: LogEntry[];
  isLoading: boolean;
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  limit?: number;
  onLimitChange?: (limit: number) => void;
  totalItems?: number;
}

const LEVEL_VARIANTS: Record<string, BadgeProps['variant']> = {
  FATAL: 'error',
  ERROR: 'error',
  WARN: 'warning',
  INFO: 'info',
  DEBUG: 'default',
  TRACE: 'default',
};

const hasDetails = (log: LogEntry) =>
  Boolean(log.stackTrace || log.threadName || (log.mdc && Object.keys(log.mdc).length > 0));

const LogRow: React.FC<{ log: LogEntry }> = ({ log }) => {
  const [expanded, setExpanded] = useState(false);
  const expandable = hasDetails(log);
  const level = String(log.level ?? '').toUpperCase();

  return (
    <>
      <tr
        className={`border-b border-gray-100 dark:border-gray-700 align-top ${expandable ? 'cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700/50' : ''}`}
        onClick={() => expandable && setExpanded(!expanded)}
      >
        <td className="px-2 py-2 w-6 text-gray-400">
          {expandable && (expanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />)}
        </td>
        <td className="px-2 py-2 whitespace-nowrap text-xs text-gray-600 dark:text-gray-400 font-mono">
          {formatTimestamp(log.timestamp)}
        </td>
        <td className="px-2 py-2">
          <Badge variant={LEVEL_VARIANTS[level] ?? 'default'} size="sm">
            {level || '—'}
          </Badge>
        </td>
        <td
          className="px-2 py-2 text-xs text-gray-600 dark:text-gray-400 font-mono max-w-[220px] truncate"
          title={log.loggerName}
        >
          {log.loggerName}
        </td>
        <td className="px-2 py-2 text-sm text-gray-900 dark:text-gray-100 break-words">
          {log.message}
        </td>
      </tr>
      {expanded && (
        <tr className="bg-gray-50 dark:bg-gray-800/60 border-b border-gray-100 dark:border-gray-700">
          <td />
          <td colSpan={4} className="px-2 py-3 space-y-2 text-xs text-gray-700 dark:text-gray-300">
            {log.threadName && (
              <div>
                <span className="font-semibold">Thread:</span>{' '}
                <span className="font-mono">{log.threadName}</span>
              </div>
            )}
            {log.mdc && Object.keys(log.mdc).length > 0 && (
              <div>
                <span className="font-semibold">MDC:</span>
                <ul className="mt-1 font-mono">
                  {Object.entries(log.mdc).map(([key, value]) => (
                    <li key={key}>
                      {key} = {value}
                    </li>
                  ))}
                </ul>
              </div>
            )}
            {log.stackTrace && (
              <pre className="whitespace-pre-wrap font-mono text-red-700 dark:text-red-300 max-h-80 overflow-auto">
                {log.stackTrace}
              </pre>
            )}
          </td>
        </tr>
      )}
    </>
  );
};

export const LogsTable: React.FC<LogsTableProps> = ({
  logs,
  isLoading,
  currentPage,
  totalPages,
  onPageChange,
  limit,
  onLimitChange,
  totalItems,
}) => {
  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner size="lg" />
      </div>
    );
  }

  if (logs.length === 0) {
    return (
      <EmptyState
        icon={<FileText size={48} className="text-gray-400" />}
        title="No logs found"
        description="No log matches the current filters."
      />
    );
  }

  return (
    <div className="space-y-4">
      <div className="overflow-x-auto bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
        <table className="min-w-full">
          <thead className="bg-gray-50 dark:bg-gray-900/40 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400">
            <tr>
              <th className="px-2 py-2" />
              <th className="px-2 py-2">Timestamp</th>
              <th className="px-2 py-2">Level</th>
              <th className="px-2 py-2">Logger</th>
              <th className="px-2 py-2">Message</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log, index) => (
              <LogRow key={`${log.timestamp}-${index}`} log={log} />
            ))}
          </tbody>
        </table>
      </div>
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={onPageChange}
        limit={limit}
        onLimitChange={onLimitChange}
        totalItems={totalItems}
      />
    </div>
  );
};
