/**
 * useShareApplication Hook
 * 
 * Provides functionality for sharing applications with users and teams,
 * and revoking access.
 */

import { useState } from 'react';
import { applicationApi } from '@/services/applicationApi';
import type { ShareApplicationRequest, RevokeAccessRequest, ShareWith } from '@/types';
import { getApiErrorMessage } from '@/services/apiError';

interface UseShareApplicationReturn {
  shareApplication: (
    appId: string,
    data: ShareApplicationRequest
  ) => Promise<void>;
  revokeAccess: (appId: string, data: RevokeAccessRequest) => Promise<void>;
  isLoading: boolean;
  isSuccess: boolean;
  isError: boolean;
  error: string | undefined;
  shareResult: ShareWith | undefined;
  reset: () => void;
}

export const useShareApplication = (): UseShareApplicationReturn => {
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isError, setIsError] = useState(false);
  const [error, setError] = useState<string | undefined>();
  const [shareResult, setShareResult] = useState<ShareWith | undefined>();

  const reset = () => {
    setIsLoading(false);
    setIsSuccess(false);
    setIsError(false);
    setError(undefined);
    setShareResult(undefined);
  };

  const shareApplication = async (
    appId: string,
    data: ShareApplicationRequest
  ): Promise<void> => {
    try {
      setIsLoading(true);
      setIsError(false);
      setError(undefined);

      const result = await applicationApi.shareApplication(appId, data);
      
      setShareResult(result);
      setIsSuccess(true);
    } catch (err) {
      setIsError(true);
      setError(getApiErrorMessage(err, 'Failed to share application'));
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const revokeAccess = async (
    appId: string,
    data: RevokeAccessRequest
  ): Promise<void> => {
    try {
      setIsLoading(true);
      setIsError(false);
      setError(undefined);

      await applicationApi.revokeAccess(appId, data);
      
      setIsSuccess(true);
    } catch (err) {
      setIsError(true);
      setError(getApiErrorMessage(err, 'Failed to revoke access'));
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  return {
    shareApplication,
    revokeAccess,
    isLoading,
    isSuccess,
    isError,
    error,
    shareResult,
    reset,
  };
};
