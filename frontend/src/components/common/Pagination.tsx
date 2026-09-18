import React from 'react';
import { Button } from './Button';
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-react';

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  className?: string;
  limit?: number;
  onLimitChange?: (limit: number) => void;
  totalItems?: number;
}

export const Pagination: React.FC<PaginationProps> = ({
  currentPage,
  totalPages,
  onPageChange,
  className = '',
  limit,
  onLimitChange,
  totalItems,
}) => {
  const pages = Array.from({ length: totalPages }, (_, i) => i + 1);
  
  const visiblePages = pages.filter((page) => {
    if (totalPages <= 7) return true;
    if (page === 1 || page === totalPages) return true;
    if (Math.abs(page - currentPage) <= 1) return true;
    return false;
  });

  const shouldShowLeftEllipsis = visiblePages[0] !== undefined && visiblePages[0] > 1 && visiblePages[1] !== undefined && visiblePages[1] > 2;
  const shouldShowRightEllipsis = 
    visiblePages[visiblePages.length - 1] !== undefined && 
    visiblePages[visiblePages.length - 1] < totalPages && 
    visiblePages[visiblePages.length - 2] !== undefined &&
    visiblePages[visiblePages.length - 2] < totalPages - 1;

  if (totalPages <= 1 && !limit) return null;

  return (
    <div className={`flex flex-col sm:flex-row items-center justify-between gap-4 ${className} w-full`}>
      {/* Page Size Selector */}
      <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 min-w-[200px]">
        {limit && onLimitChange && (
          <>
            <span>Rows per page:</span>
            <select
              value={limit}
              onChange={(e) => {
                const val = e.target.value === 'all' ? (totalItems || 1000000) : Number(e.target.value);
                onLimitChange(val);
                onPageChange(1); // Reset to first page on size change
              }}
              className="h-8 rounded-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            >
              {[10, 20, 50, 100].map((size) => (
                <option key={size} value={size}>
                  {size}
                </option>
              ))}
               <option value="all">All</option>
            </select>
          </>
        )}
        {totalItems !== undefined && limit && (
           <span className="ml-2">
             {Math.min((currentPage - 1) * limit + 1, totalItems)} - {Math.min(currentPage * limit, totalItems)} of {totalItems}
           </span>
        )}
      </div>

      {/* Pagination Controls */}
      <nav aria-label="Pagination" className="flex items-center justify-center gap-1">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onPageChange(1)}
          disabled={currentPage === 1}
          aria-label="First page"
          title="First page"
          className="hidden sm:flex"
        >
          <ChevronsLeft size={16} />
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 1}
          aria-label="Previous page"
        >
           <ChevronLeft size={16} />
           <span className="hidden sm:inline ml-1">Previous</span>
        </Button>

        <div className="flex items-center gap-1 mx-2">
          {shouldShowLeftEllipsis && (
            <span className="px-2 text-gray-500 dark:text-gray-400">...</span>
          )}

          {visiblePages.map((page) => (
            <Button
              key={page}
              variant={page === currentPage ? 'primary' : 'ghost'}
              size="sm"
              onClick={() => onPageChange(page)}
              className={`min-w-[32px] ${page === currentPage ? '' : 'text-gray-600 dark:text-gray-400'}`}
            >
              {page}
            </Button>
          ))}

          {shouldShowRightEllipsis && (
            <span className="px-2 text-gray-500 dark:text-gray-400">...</span>
          )}
        </div>

        <Button
          variant="ghost"
          size="sm"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage === totalPages}
          aria-label="Next page"
        >
          <span className="hidden sm:inline mr-1">Next</span>
          <ChevronRight size={16} />
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onPageChange(totalPages)}
          disabled={currentPage === totalPages}
          aria-label="Last page"
          title="Last page"
          className="hidden sm:flex"
        >
          <ChevronsRight size={16} />
        </Button>
      </nav>
    </div>
  );
};
