import { apiClient } from '@/services/api';
import type { ApplicationMetricsResponse, MetricFilterParams } from '@/types';

export const metricsApi = {
  getApplicationMetrics: async (
    applicationId: string,
    params: MetricFilterParams = { page: 1, limit: 20 }
  ): Promise<ApplicationMetricsResponse> => {
    // Backend expects 0-based page index, frontend uses 1-based
    const frontendPage = params.page || 1;
    const page = frontendPage > 0 ? frontendPage - 1 : 0;
    const size = params.limit || 20;

    // Create clean params without page and limit
    const { page: _, limit: __, ...otherParams } = params;

    const queryParams = {
        ...otherParams,
        page,
        size,
    };

    const response = await apiClient.get<ApplicationMetricsResponse>(
      `/applications/${applicationId}/metrics`,
      { params: queryParams }
    );
    
    return response.data;
  },
};
