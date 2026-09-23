import { useCallback, useEffect, useState } from 'react';
import { logsApi } from '../api/logsApi';
import type { LogEntry, LogFilterParams } from '@/types';

export interface UseLogsOptions extends LogFilterParams {
  applicationId?: string;
  enabled?: boolean;
}

export const useLogs = ({ enabled = true, applicationId, ...filters }: UseLogsOptions) => {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [appName, setAppName] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const filtersKey = JSON.stringify(filters);

  const fetchLogs = useCallback(async () => {
    if (!enabled || !applicationId) {
      setLogs([]);
      setTotal(0);
      setTotalPages(0);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const response = await logsApi.getLogs(
        applicationId,
        JSON.parse(filtersKey) as LogFilterParams
      );
      setLogs(response.data);
      setTotal(response.total);
      setTotalPages(response.totalPages);
      setAppName(response.appName ?? '');
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to fetch logs'));
      setLogs([]);
      setTotal(0);
      setTotalPages(0);
    } finally {
      setIsLoading(false);
    }
  }, [enabled, applicationId, filtersKey]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  return { logs, total, totalPages, appName, isLoading, error, refetch: fetchLogs };
};
