/**
 * CreateApplicationModal Component
 * 
 * Modal dialog for creating a new application with form validation.
 */

import { useState } from 'react';
import { useCreateApplication } from '../hooks/useCreateApplication';
import type { Application } from '@/types';

interface CreateApplicationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: (application: Application) => void;
}

export function CreateApplicationModal({
  isOpen,
  onClose,
  onSuccess,
}: CreateApplicationModalProps) {
  const [name, setName] = useState('');
  const { createApplication, isLoading, error } = useCreateApplication({
    onSuccess: (app) => {
      setName('');
      onSuccess?.(app);
      onClose();
    },
  });
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!name.trim()) {
      return;
    }
    
    try {
      await createApplication({ name: name.trim() });
    } catch {
      // Error is handled by the hook
    }
  };
  
  const handleClose = () => {
    if (!isLoading) {
      setName('');
      onClose();
    }
  };
  
  if (!isOpen) return null;
  
  return (
    <div
      className="fixed inset-0 bg-black dark:bg-black bg-opacity-50 dark:bg-opacity-70 flex items-center justify-center z-50"
      onClick={handleClose}
      aria-labelledby="modal-title"
      role="dialog"
      aria-modal="true"
    >
      <div
        className="bg-white dark:bg-gray-800 rounded-lg p-8 max-w-md w-full mx-4"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="modal-title" className="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-6">
          Create Application
        </h2>
        
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label
              htmlFor="app-name"
              className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2"
            >
              Application Name
            </label>
            <input
              id="app-name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50"
              placeholder="My Application"
              disabled={isLoading}
              required
              minLength={3}
              maxLength={50}
              aria-describedby={error ? 'error-message' : undefined}
            />
          </div>
          
          {error && (
            <div
              id="error-message"
              className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg"
              role="alert"
            >
              <p className="text-sm text-red-800 dark:text-red-400">{error.message}</p>
            </div>
          )}
          
          <div className="flex gap-3 justify-end">
            <button
              type="button"
              onClick={handleClose}
              disabled={isLoading}
              className="px-4 py-2 text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading || !name.trim()}
              className="px-4 py-2 text-white bg-blue-600 dark:bg-blue-500 rounded-lg hover:bg-blue-700 dark:hover:bg-blue-600 disabled:opacity-50"
            >
              {isLoading ? 'Creating...' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
