/**
 * ShareApplicationModal Component
 * 
 * Modal for sharing applications with users or teams.
 * Provides form for entering email/team key and selecting share type.
 */

import { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { useShareApplication } from '../hooks/useShareApplication';
import { SharedWithProfile } from '@/types';

interface ShareApplicationModalProps {
  isOpen: boolean;
  onClose: () => void;
  applicationId: string;
  applicationName: string;
  onSuccess?: () => void;
}

export const ShareApplicationModal = ({
  isOpen,
  onClose,
  applicationId,
  applicationName,
  onSuccess,
}: ShareApplicationModalProps) => {
  const [sharedWith, setSharedWith] = useState('');
  const [profile, setProfile] = useState<SharedWithProfile>(SharedWithProfile.USER);
  const [validationError, setValidationError] = useState('');

  const { shareApplication, isLoading, isSuccess, isError, error, reset } =
    useShareApplication();

  // Move handleClose before useEffect that uses it
  const handleClose = () => {
    setSharedWith('');
    setProfile(SharedWithProfile.USER);
    setValidationError('');
    reset();
    onClose();
  };

  useEffect(() => {
    if (isSuccess) {
      onSuccess?.();
      handleClose();
    }
  }, [isSuccess, onSuccess, handleClose]);

  // Reset form when modal initially opens (not on every render)
  useEffect(() => {
    if (isOpen) {
      setSharedWith('');
      setProfile(SharedWithProfile.USER);
      setValidationError('');
    }
  }, [isOpen]);

  const validateForm = (): boolean => {
    if (!sharedWith.trim()) {
      setValidationError('Email or team key is required');
      return false;
    }

    if (profile === SharedWithProfile.USER) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(sharedWith)) {
        setValidationError('Please enter a valid email address');
        return false;
      }
    }

    setValidationError('');
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      await shareApplication(applicationId, {
        shareWith: sharedWith,
        profile,
      });
    } catch (error) {
      // Error is handled by the hook state
    }
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-md p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100">
            Share Application
          </h2>
          <button
            onClick={handleClose}
            className="text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300"
            aria-label="Close"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Application Name */}
        <p className="text-sm text-gray-600 dark:text-gray-400 mb-6">
          Share <span className="font-medium">{applicationName}</span> with users or teams
        </p>

        {/* Form */}
        <form onSubmit={handleSubmit}>
          {/* Email/Team Key Input */}
          <div className="mb-4">
            <label
              htmlFor="sharedWith"
              className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
            >
              Email or Team Key
            </label>
            <input
              id="sharedWith"
              type="text"
              value={sharedWith}
              onChange={(e) => setSharedWith(e.target.value)}
              placeholder={
                profile === SharedWithProfile.USER
                  ? 'user@example.com'
                  : 'team-xyz'
              }
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
              disabled={isLoading}
            />
            {validationError && (
              <p className="mt-1 text-sm text-red-600 dark:text-red-400">{validationError}</p>
            )}
          </div>

          {/* Share Type Select */}
          <div className="mb-6">
            <label
              htmlFor="profile"
              className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
            >
              Share Type
            </label>
            <select
              id="profile"
              value={profile}
              onChange={(e) => setProfile(e.target.value as SharedWithProfile)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
              disabled={isLoading}
              aria-label="Share Type"
            >
              <option value={SharedWithProfile.USER}>User</option>
              <option value={SharedWithProfile.TEAM}>Team</option>
            </select>
          </div>

          {/* API Error */}
          {isError && error && (
            <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-md">
              <p className="text-sm text-red-600 dark:text-red-400">{error}</p>
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-3 justify-end">
            <button
              type="button"
              onClick={handleClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-md hover:bg-gray-50 dark:hover:bg-gray-600"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 dark:bg-blue-500 rounded-md hover:bg-blue-700 dark:hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={isLoading}
            >
              {isLoading ? 'Sharing...' : 'Share'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
