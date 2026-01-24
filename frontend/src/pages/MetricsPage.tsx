import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { BarChart3 } from 'lucide-react';
import { MetricsFilters } from '@/features/metrics/components/MetricsFilters';
import { MetricsTable } from '@/features/metrics/components/MetricsTable';
import { useMetrics } from '@/features/metrics/hooks/useMetrics';
import { useApplications } from '@/features/applications/hooks';
import { Spinner } from '@/components/common/Spinner';
import { EmptyState } from '@/components/common/EmptyState';
import { Pagination } from '@/components/common/Pagination';
import type { MetricFilterParams } from '@/types';

export const MetricsPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const applicationId = searchParams.get('applicationId');
  
  const { applications, isLoading: loadingApps } = useApplications();

  const [filters, setFilters] = useState<MetricFilterParams>({ page: 1, limit: 50 });
  const [currentPage, setCurrentPage] = useState(1);
  const [limit, setLimit] = useState(20);

  const { metrics, total, totalPages: backendTotalPages, appName, isLoading, error } = useMetrics({
    applicationId: applicationId || undefined,
    enabled: !!applicationId,
    ...filters,
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
      console.log(`[MetricsPage] Current page ${currentPage} exceeds totalPages ${totalPages}, resetting to ${totalPages}`);
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const handleFiltersChange = (newFilters: MetricFilterParams) => {
    setFilters(newFilters);
    setCurrentPage(1);
  };

  const handlePageChange = (page: number) => {
    const validPage = Math.min(page, totalPages);
    console.log(`[MetricsPage] Page change requested: ${page}, valid page: ${validPage}, totalPages: ${totalPages}`);
    setCurrentPage(validPage);
  };

  const handleApplicationChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const appId = event.target.value;
    if (appId) {
      navigate(`/metrics?applicationId=${appId}`);
    } else {
      navigate('/metrics');
    }
  };

  return (
    <div className="flex flex-col h-full">
      {/* Fixed Header */}
      <div className="flex-none bg-white dark:bg-gray-900 space-y-4 pb-4 border-b border-gray-200 dark:border-gray-700">
        {/* Header with Application Selector */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Metrics</h1>
            <p className="mt-1 text-sm text-gray-600 dark:text-gray-300">
              {appName ? `Monitoring ${appName}` : 'Monitor application performance'}
            </p>
          </div>

          <div className="sm:min-w-[300px]">
            <label htmlFor="application-select" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Select Application
            </label>
            {loadingApps ? (
              <div className="text-sm text-gray-500 dark:text-gray-400">Loading...</div>
            ) : (
              <select
                id="application-select"
                value={applicationId || ''}
                onChange={handleApplicationChange}
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
          <MetricsFilters filters={filters} onFiltersChange={handleFiltersChange} />
        )}
      </div>

      {/* Scrollable Content */}
      <div className="flex-1 overflow-y-auto scrollbar-light">
        <div className="pt-4">
          {!applicationId ? (
            <EmptyState
              icon={<BarChart3 size={48} className="text-gray-400" />}
              title="No application selected"
              description="Please select an application from the dropdown above to view its metrics."
            />
          ) : (
            <>
              {error && (
                <div className="mb-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4">
                  <p className="text-red-800 dark:text-red-400">{error.message}</p>
                </div>
              )}

              {isLoading ? (
                <div className="flex justify-center items-center py-12">
                  <Spinner size="lg" />
                </div>
              ) : metrics && metrics.length === 0 ? (
                <EmptyState
                  icon={<BarChart3 size={48} className="text-gray-400" />}
                  title="No metrics found"
                  description="Try adjusting your filters or check back later."
                />
              ) : (
                <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-sm overflow-hidden">
                  <MetricsTable metrics={metrics || []} />
                  {totalPages > 0 && (
                    <div className="px-6 py-4 border-t border-gray-200 dark:border-gray-700">
                      <Pagination 
                        currentPage={currentPage} 
                        totalPages={totalPages} 
                        onPageChange={handlePageChange}
                        limit={limit}
                        onLimitChange={setLimit}
                        totalItems={total}
                      />
                    </div>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};
