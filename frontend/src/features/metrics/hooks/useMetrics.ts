import { useState, useEffect, useCallback } from 'react';
import { metricsApi } from '../api/metricsApi';
import type { MetricEntry, MetricFilterParams } from '@/types';

export interface UseMetricsOptions extends MetricFilterParams {
  applicationId?: string;
  enabled?: boolean;
}

export const useMetrics = ({ enabled = true, applicationId, ...filters }: UseMetricsOptions) => {
  const [metrics, setMetrics] = useState<MetricEntry[]>([]);
  const [total, setTotal] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [appName, setAppName] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const fetchMetrics = useCallback(async () => {
    if (!enabled || !applicationId) return;

    setIsLoading(true);
    setError(null);

    try {
      const response = await metricsApi.getApplicationMetrics(applicationId, filters);
      setMetrics(response.paginatedMetrics.content);
      setTotal(response.paginatedMetrics.totalElements);
      setTotalPages(response.paginatedMetrics.totalPages);
      setAppName(response.appName);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to fetch metrics'));
      setMetrics([]);
      setTotal(0);
      setTotalPages(0);
    } finally {
      setIsLoading(false);
    }
  }, [enabled, applicationId, JSON.stringify(filters)]);

  useEffect(() => {
    fetchMetrics();
  }, [fetchMetrics]);

  return {
    metrics,
    total,
    totalPages,
    appName,
    isLoading,
    error,
    refetch: fetchMetrics,
  };
};
