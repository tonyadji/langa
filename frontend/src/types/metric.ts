import type { PaginationParams } from './common';

export type MetricType = 'cpu' | 'memory' | 'requests' | 'errors' | 'latency';
export type TimeRange = '1h' | '6h' | '24h' | '7d' | '30d';

export const MetricStatus = {
  SUCCESS: 'SUCCESS',
  FAILURE: 'FAILURE',
  TIMEOUT: 'TIMEOUT',
  ERROR: 'ERROR',
} as const;

export type MetricStatus = typeof MetricStatus[keyof typeof MetricStatus];

export interface MetricEntry {
  appKey: string;
  accountKey: string;
  name: string;
  durationMillis: number;
  status: MetricStatus;
  timestamp: string; // ISO 8601 timestamp
  uri?: string;
  httpMethod?: string;
  httpStatus?: number;
}

export interface MetricFilterParams extends PaginationParams {
  name?: string;
  status?: string;
  uri?: string;
  httpMethod?: string;
  httpStatus?: number;
  durationLessThan?: number;
  durationGreaterThan?: number;
  keyword?: string;
  startDate?: string;
  endDate?: string;
  // UI helper fields
  timeRange?: TimeRange;
  metricTypes?: MetricType[];
}

export interface MetricStats {
  count: number;
  avgDuration: number;
  p95Duration: number;
  p99Duration: number;
  errorRate: number;
}

export interface MetricsState {
  metrics: MetricEntry[];
  total: number;
  loading: boolean;
  error: string | null;
  filters: MetricFilterParams;
  stats?: MetricStats;
}

export interface ChartConfig {
  title: string;
  type: 'line' | 'area' | 'bar';
  dataKey: string;
  color: string;
  unit: string;
}
