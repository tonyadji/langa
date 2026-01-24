import React, { useState } from 'react';
import { Search } from 'lucide-react';
import type { MetricFilterParams } from '@/types';

interface MetricsFiltersProps {
  filters: MetricFilterParams;
  onFiltersChange: (filters: MetricFilterParams) => void;
}

const HTTP_METHODS = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'];
const HTTP_STATUSES = [200, 201, 400, 401, 403, 404, 500, 502, 503];

export const MetricsFilters: React.FC<MetricsFiltersProps> = ({ filters, onFiltersChange }) => {
  const [localFilters, setLocalFilters] = useState<MetricFilterParams>(filters);

  const handleApplyFilters = () => {
    onFiltersChange(localFilters);
  };

  const handleClearFilters = () => {
    const cleared: MetricFilterParams = { page: 1, limit: 50 };
    setLocalFilters(cleared);
    onFiltersChange(cleared);
  };

  return (
    <div className="bg-white dark:bg-gray-800 p-3 rounded-lg border border-gray-200 dark:border-gray-700">
      <div className="flex flex-wrap items-end gap-2">
        {/* Name Filter */}
        <div className="w-[150px]">
          <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
            Name
          </label>
          <input
            type="text"
            value={localFilters.name || ''}
            onChange={(e) => setLocalFilters({ ...localFilters, name: e.target.value || undefined })}
            placeholder="Name"
            className="w-full px-2 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* URI Filter */}
        <div className="w-[150px]">
          <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
            URI
          </label>
          <input
            type="text"
            value={localFilters.uri || ''}
            onChange={(e) => setLocalFilters({ ...localFilters, uri: e.target.value || undefined })}
            placeholder="URI"
            className="w-full px-2 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Keyword Filter */}
        <div className="flex-1 min-w-[150px]">
          <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
            Keyword
          </label>
          <div className="relative">
            <Search className="absolute left-2 top-2 h-3.5 w-3.5 text-gray-400" />
            <input
              type="text"
              value={localFilters.keyword || ''}
              onChange={(e) => setLocalFilters({ ...localFilters, keyword: e.target.value || undefined })}
              placeholder="Search..."
              className="w-full pl-8 pr-2 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {/* HTTP Method Filter */}
        <div className="w-[100px]">
          <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
            Method
          </label>
          <select
            value={localFilters.httpMethod || ''}
            onChange={(e) => setLocalFilters({ ...localFilters, httpMethod: e.target.value || undefined })}
            className="w-full px-2 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All</option>
            {HTTP_METHODS.map((method) => (
              <option key={method} value={method}>
                {method}
              </option>
            ))}
          </select>
        </div>

        {/* HTTP Status Filter */}
        <div className="w-[100px]">
          <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
            Status
          </label>
          <select
            value={localFilters.httpStatus || ''}
            onChange={(e) => setLocalFilters({ ...localFilters, httpStatus: e.target.value ? Number(e.target.value) : undefined })}
            className="w-full px-2 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All</option>
            {HTTP_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>

        {/* Start Date */}
        <div className="w-[160px]">
          <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
            Start Date
          </label>
          <input
            type="datetime-local"
            value={localFilters.startDate || ''}
            onChange={(e) => setLocalFilters({ ...localFilters, startDate: e.target.value || undefined })}
            className="w-full px-2 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* End Date */}
        <div className="w-[160px]">
          <label className="block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1">
            End Date
          </label>
          <input
            type="datetime-local"
            value={localFilters.endDate || ''}
            onChange={(e) => setLocalFilters({ ...localFilters, endDate: e.target.value || undefined })}
            className="w-full px-2 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Action Buttons */}
        <button
          onClick={handleApplyFilters}
          className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-md transition-colors text-sm font-medium"
        >
          Apply
        </button>
        <button
          onClick={handleClearFilters}
          className="px-3 py-1.5 bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-md transition-colors text-sm font-medium"
        >
          Clear
        </button>
      </div>
    </div>
  );
};
