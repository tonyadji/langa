/**
 * T211: useApplicationUsage hook
 * 
 * Custom hook for fetching application storage usage statistics.
 * Uses React hooks for state management and axios for HTTP requests.
 */

import { useState, useEffect } from 'react';
import { apiClient } from '@/services/api';
import type { ApplicationUsage } from '@/types/usage';

// Backend response format (different from frontend type)
interface BackendUsageResponse {
  id: string;
  key: string;
  name: string;
  logUsage: number;
  metricUsage: number;
}

interface UseApplicationUsageResult {
  data: ApplicationUsage | undefined;
  isLoading: boolean;
  isError: boolean;
  error: Error | null;
}

/**
 * Hook for fetching application usage statistics
 * 
 * @param appId - The application ID to fetch usage for
 * @returns Usage data, loading state, and error state
 */
export const useApplicationUsage = (appId: string): UseApplicationUsageResult => {
  const [data, setData] = useState<ApplicationUsage | undefined>(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const [isError, setIsError] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    // Don't fetch if appId is empty
    if (!appId) {
      return;
    }

    const fetchUsage = async () => {
      setIsLoading(true);
      setIsError(false);
      setError(null);

      try {
        const response = await apiClient.get<BackendUsageResponse>(`/applications/${appId}/usage`);
        
        // Map backend response to frontend type
        const backendData = response.data;
        const mappedData: ApplicationUsage = {
          id: backendData.id,
          appKey: backendData.key,
          totalLogBytes: backendData.logUsage,
          totalMetricBytes: backendData.metricUsage,
        };
        
        setData(mappedData);
      } catch (err) {
        setIsError(true);
        
        if (err && typeof err === 'object' && 'response' in err) {
          const axiosError = err as { response?: { status: number } };
          if (axiosError.response?.status === 403) {
            setError(new Error('Forbidden - only application owners can view usage statistics'));
          } else if (axiosError.response?.status === 404) {
            setError(new Error('Application not found'));
          } else {
            setError(new Error('Failed to fetch usage data'));
          }
        } else {
          setError(new Error('Network error'));
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchUsage();
  }, [appId]); // Refetch when appId changes

  return {
    data,
    isLoading,
    isError,
    error,
  };
};
