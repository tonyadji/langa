/**
 * useCreateApplication Hook
 * 
 * Custom hook for creating new applications.
 * Provides loading states, error handling, and callbacks.
 */

import { useState, useCallback } from 'react';
import { applicationApi } from '@/services/applicationApi';
import type { Application } from '@/types';
import { getApiErrorMessage } from '@/services/apiError';

interface CreateApplicationInput {
  name: string;
}

interface UseCreateApplicationOptions {
  onSuccess?: (application: Application) => void;
  onError?: (error: Error) => void;
}

interface UseCreateApplicationReturn {
  createApplication: (input: CreateApplicationInput) => Promise<Application>;
  isLoading: boolean;
  error: Error | null;
  reset: () => void;
}

export function useCreateApplication(
  options: UseCreateApplicationOptions = {}
): UseCreateApplicationReturn {
  const { onSuccess, onError } = options;
  
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  
  const createApplication = useCallback(
    async (input: CreateApplicationInput): Promise<Application> => {
      try {
        setIsLoading(true);
        setError(null);
        
        const application = await applicationApi.createApplication(input.name);
        
        onSuccess?.(application);
        return application;
      } catch (err) {
        const error = new Error(getApiErrorMessage(err, 'Failed to create application'));
        setError(error);
        onError?.(error);
        throw error;
      } finally {
        setIsLoading(false);
      }
    },
    [onSuccess, onError]
  );
  
  const reset = useCallback(() => {
    setError(null);
    setIsLoading(false);
  }, []);
  
  return {
    createApplication,
    isLoading,
    error,
    reset,
  };
}
