import React, { useEffect, useState } from 'react';
import { Search } from 'lucide-react';
import { LogLevel, type LogFilterParams } from '@/types';
import { toBackendDate, toLocalInput } from '../utils/dates';

interface LogFiltersProps {
  filters: LogFilterParams;
  onFiltersChange: (filters: LogFilterParams) => void;
  onClear: () => void;
}

const LEVELS = Object.values(LogLevel);

const inputClass =
  'w-full px-2 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 ' +
  'text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500';
const labelClass = 'block text-xs font-medium text-gray-700 dark:text-gray-300 mb-1';

export const LogFilters: React.FC<LogFiltersProps> = ({ filters, onFiltersChange, onClear }) => {
  const [localFilters, setLocalFilters] = useState<LogFilterParams>(filters);

  useEffect(() => {
    setLocalFilters(filters);
  }, [filters]);

  const apply = (event?: React.FormEvent) => {
    event?.preventDefault();
    onFiltersChange({ ...localFilters, page: 1 });
  };

  return (
    <form
      onSubmit={apply}
      className="bg-white dark:bg-gray-800 p-3 rounded-lg border border-gray-200 dark:border-gray-700"
      aria-label="Log filters"
    >
      <div className="flex flex-wrap items-end gap-2">
        <div className="w-[130px]">
          <label htmlFor="log-level" className={labelClass}>
            Level
          </label>
          <select
            id="log-level"
            value={localFilters.level || ''}
            onChange={(e) =>
              setLocalFilters({ ...localFilters, level: e.target.value || undefined })
            }
            className={inputClass}
          >
            <option value="">All levels</option>
            {LEVELS.map((level) => (
              <option key={level} value={level}>
                {level}
              </option>
            ))}
          </select>
        </div>

        <div className="flex-1 min-w-[180px]">
          <label htmlFor="log-keyword" className={labelClass}>
            Keyword
          </label>
          <div className="relative">
            <Search className="absolute left-2 top-2 h-3.5 w-3.5 text-gray-400" />
            <input
              id="log-keyword"
              type="text"
              value={localFilters.keyword || ''}
              onChange={(e) =>
                setLocalFilters({ ...localFilters, keyword: e.target.value || undefined })
              }
              placeholder="Search in messages..."
              className={`${inputClass} pl-8`}
            />
          </div>
        </div>

        <div className="w-[190px]">
          <label htmlFor="log-start" className={labelClass}>
            From
          </label>
          <input
            id="log-start"
            type="datetime-local"
            value={toLocalInput(localFilters.startDate)}
            onChange={(e) =>
              setLocalFilters({ ...localFilters, startDate: toBackendDate(e.target.value) })
            }
            className={inputClass}
          />
        </div>

        <div className="w-[190px]">
          <label htmlFor="log-end" className={labelClass}>
            To
          </label>
          <input
            id="log-end"
            type="datetime-local"
            value={toLocalInput(localFilters.endDate)}
            onChange={(e) =>
              setLocalFilters({ ...localFilters, endDate: toBackendDate(e.target.value) })
            }
            className={inputClass}
          />
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            className="px-3 py-1.5 text-sm font-medium rounded-md bg-blue-600 text-white hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600"
          >
            Apply
          </button>
          <button
            type="button"
            onClick={onClear}
            className="px-3 py-1.5 text-sm font-medium rounded-md bg-gray-200 text-gray-700 hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600"
          >
            Clear
          </button>
        </div>
      </div>
    </form>
  );
};
