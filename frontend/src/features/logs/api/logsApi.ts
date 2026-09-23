import { apiClient } from '@/services/api';
import type { ApplicationLogsResponse, LogEntry, LogFilterParams } from '@/types';

export interface LogsPage {
  data: LogEntry[];
  total: number;
  totalPages: number;
  /** 1-based page number */
  page: number;
  limit: number;
  appName?: string;
}

/** Levels may be passed as a list: the backend expects them comma separated. */
type LogsQuery = Omit<LogFilterParams, 'level'> & { level?: string | string[] };

export const logsApi = {
  getLogs: async (applicationId: string, params: LogsQuery): Promise<LogsPage> => {
    const page = params.page && params.page > 0 ? params.page : 1;
    const limit = params.limit || 20;
    const level = Array.isArray(params.level) ? params.level.join(',') : params.level;

    const response = await apiClient.get<ApplicationLogsResponse>(
      `/applications/${applicationId}/logs`,
      {
        params: {
          // Backend pages are 0-based
          page: page - 1,
          size: limit,
          level: level || undefined,
          keyword: params.keyword || undefined,
          startDate: params.startDate || undefined,
          endDate: params.endDate || undefined,
        },
      }
    );

    const paginated = response.data.paginatedLogs;
    return {
      data: paginated.content,
      total: paginated.totalElements,
      totalPages: paginated.totalPages,
      page,
      limit,
      appName: response.data.appName,
    };
  },
};
