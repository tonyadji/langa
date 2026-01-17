import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { FileText } from 'lucide-react';
import { LogFilters } from '@/features/logs/components/LogFilters';
import { LogsTable } from '@/features/logs/components/LogsTable';
import { useLogs } from '@/features/logs/hooks/useLogs';
import { useApplications } from '@/features/applications/hooks/useApplications';
import { EmptyState } from '@/components/common/EmptyState';
import { Spinner } from '@/components/common/Spinner';
import type { LogFilterParams } from '@/types';

export const LogsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const applicationId = searchParams.get('applicationId');

  const { applications, isLoading: isLoadingApps } = useApplications();

  // Filters state (excluding pagination which is handled separately)
  const [filters, setFilters] = useState<LogFilterParams>({ page: 1, limit: 50 });

  const [currentPage, setCurrentPage] = useState(1);
  const [limit, setLimit] = useState(50);

  const { logs, total, totalPages: backendTotalPages, isLoading } = useLogs({
    ...filters,
    applicationId: applicationId || undefined,
    page: currentPage,
    limit,
  });

  // Backend uses 0-based indexing (pages 0, 1, 2...) and returns total page count
  // Frontend converts page numbers: UI page 1 → backend page 0
  // Subtract 1 to match available pages
  const totalPages = backendTotalPages > 0 ? backendTotalPages - 1 : 1;
  
  // Ensure current page doesn't exceed total pages
  React.useEffect(() => {
    if (currentPage > totalPages && totalPages > 0) {
      console.log(`[LogsPage] Current page ${currentPage} exceeds totalPages ${totalPages}, resetting to ${totalPages}`);
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const handleFiltersChange = (newFilters: LogFilterParams) => {
    setFilters(newFilters);
    setCurrentPage(1); // Reset to first page when filters change
  };

  const handleClearFilters = () => {
    setFilters({ page: 1, limit: 50 });
    setCurrentPage(1);
  };

  const handleApplicationChange = (appId: string) => {
    setSearchParams({ applicationId: appId });
    setFilters({ page: 1, limit: 50 });
    setCurrentPage(1);
  };

  const handlePageChange = (page: number) => {
    const validPage = Math.min(page, totalPages);
    console.log(`[LogsPage] Page change requested: ${page}, valid page: ${validPage}, totalPages: ${totalPages}`);
    setCurrentPage(validPage);
  };

  return (
    <div className="flex flex-col h-full">
      {/* Fixed Header */}
      <div className="flex-none bg-white dark:bg-gray-900 space-y-4 pb-4 border-b border-gray-200 dark:border-gray-700">
        {/* Header with Application Selector */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Logs</h1>
            <p className="mt-1 text-sm text-gray-600 dark:text-gray-300">View and search application logs</p>
          </div>
          
          <div className="sm:min-w-[300px]">
            <label htmlFor="application-select" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Select Application
            </label>
            {isLoadingApps ? (
              <div className="flex items-center gap-2">
                <Spinner size="sm" />
                <span className="text-sm text-gray-500 dark:text-gray-400">Loading...</span>
              </div>
            ) : (
              <select
                id="application-select"
                value={applicationId || ''}
                onChange={(e) => handleApplicationChange(e.target.value)}
                className="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">-- Select an application --</option>
                {applications.map((app) => (
                  <option key={app.id} value={app.id}>
                    {app.name}
                  </option>
                ))}
              </select>
            )}
          </div>
        </div>

        {applicationId && (
          <LogFilters filters={filters} onFiltersChange={handleFiltersChange} onClear={handleClearFilters} />
        )}
      </div>

      {/* Scrollable Content */}
      <div className="flex-1 overflow-y-auto scrollbar-light">
        <div className="pt-4">
          {!applicationId ? (
            <EmptyState
              icon={<FileText size={48} className="text-gray-400" />}
              title="No application selected"
              description="Please select an application from the dropdown above to view its logs."
            />
          ) : (
            <LogsTable
              logs={logs}
              isLoading={isLoading}
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
              limit={limit}
              onLimitChange={setLimit}
              totalItems={total}
            />
          )}
        </div>
      </div>
    </div>
  );
};
