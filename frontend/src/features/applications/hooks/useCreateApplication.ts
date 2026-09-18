/**
 * useCreateApplication Hook
 * 
 * Custom hook for creating new applications.
 * Provides loading states, error handling, and callbacks.
 */

import { useState, useCallback } from 'react';
import { AxiosError } from 'axios';
import { applicationApi } from '@/services/applicationApi';
import type { Application } from '@/types';

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
        let errorMessage = 'Failed to create application';
        
        if (err instanceof AxiosError && err.response?.data?.error) {
          errorMessage = err.response.data.error;
        } else if (err instanceof Error) {
          errorMessage = err.message;
        }
        
        const error = new Error(errorMessage);
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
