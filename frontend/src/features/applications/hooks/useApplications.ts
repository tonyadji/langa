/**
 * useApplications Hook
 * 
 * Custom hook for fetching and managing application list state.
 * Provides loading states, error handling, and pagination.
 */

import { useState, useEffect, useCallback } from 'react';
import { applicationApi } from '@/services/applicationApi';
import type { Application } from '@/types';

interface UseApplicationsOptions {
  page?: number;
  limit?: number;
}

interface PaginationInfo {
  page: number;
  limit: number;
  total: number;
}

interface UseApplicationsReturn {
  applications: Application[];
  isLoading: boolean;
  error: Error | null;
  refetch: () => Promise<void>;
  pagination: PaginationInfo;
}

export function useApplications(options: UseApplicationsOptions = {}): UseApplicationsReturn {
  const { page = 1, limit = 10 } = options;
  
  const [applications, setApplications] = useState<Application[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [pagination, setPagination] = useState<PaginationInfo>({
    page,
    limit,
    total: 0,
  });
  
  const fetchApplications = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      const response = await applicationApi.getApplications({ page, limit });
      
      setApplications(response.applications);
      setPagination({
        page: response.page,
        limit: response.limit,
        total: response.total,
      });
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to fetch applications'));
    } finally {
      setIsLoading(false);
    }
  }, [page, limit]);
  
  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);
  
  return {
    applications,
    isLoading,
    error,
    refetch: fetchApplications,
    pagination,
  };
}
