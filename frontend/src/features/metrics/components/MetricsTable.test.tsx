/**
 * Component Test: MetricsTable
 * Test ID: T140 | US4 - Metrics Visualization
 *
 * The table renders the metrics it receives (pagination, loading and empty states are handled by MetricsPage):
 * - columns (Name, Method, URI, Status, Duration, Result, Time)
 * - duration and timestamp formatting
 * - HTTP status and result styling
 * - client-side sorting by column
 */

import { describe, it, expect } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MetricsTable } from './MetricsTable';
import type { MetricEntry } from '@/types';

const metric = (overrides: Partial<MetricEntry>): MetricEntry => ({
  appKey: 'APP-1',
  accountKey: 'ACC-1',
  name: 'api.request',
  durationMillis: 150,
  status: 'SUCCESS' as MetricEntry['status'],
  timestamp: '2024-01-08T16:00:00Z',
  uri: '/api/users',
  httpMethod: 'GET',
  httpStatus: 200,
  ...overrides,
});

const mockMetrics: MetricEntry[] = [
  metric({}),
  metric({ durationMillis: 1500, timestamp: '2024-01-08T16:01:00Z', uri: '/api/products', httpMethod: 'POST', httpStatus: 201 }),
  metric({
    name: 'api.error',
    durationMillis: 500,
    status: 'FAILURE' as MetricEntry['status'],
    timestamp: '2024-01-08T16:02:00Z',
    uri: '/api/orders',
    httpMethod: 'DELETE',
    httpStatus: 500,
  }),
];

const bodyRows = () => screen.getAllByRole('row').slice(1);

describe('MetricsTable Component', () => {
  it('should render the columns', () => {
    render(<MetricsTable metrics={mockMetrics} />);

    for (const header of ['Name', 'Method', 'URI', 'Status', 'Duration', 'Result', 'Time']) {
      expect(screen.getByRole('columnheader', { name: new RegExp(header) })).toBeInTheDocument();
    }
  });

  it('should render one row per metric', () => {
    render(<MetricsTable metrics={mockMetrics} />);

    expect(bodyRows()).toHaveLength(3);
    expect(screen.getByText('/api/orders')).toBeInTheDocument();
    expect(screen.getByText('DELETE')).toBeInTheDocument();
  });

  it('should render no row without metrics', () => {
    render(<MetricsTable metrics={[]} />);

    expect(bodyRows()).toHaveLength(0);
  });

  it('should format durations in ms or s', () => {
    render(<MetricsTable metrics={mockMetrics} />);

    expect(screen.getByText('150ms')).toBeInTheDocument();
    expect(screen.getByText('1.50s')).toBeInTheDocument();
  });

  it('should format timestamps', () => {
    render(<MetricsTable metrics={[metric({})]} />);

    expect(screen.getByText(/Jan 8, 2024/)).toBeInTheDocument();
  });

  it('should color HTTP status codes by class', () => {
    render(<MetricsTable metrics={mockMetrics} />);

    expect(screen.getByText('200')).toHaveClass('text-green-600');
    expect(screen.getByText('500')).toHaveClass('text-red-600');
  });

  it('should color results', () => {
    render(<MetricsTable metrics={mockMetrics} />);

    expect(screen.getAllByText('SUCCESS')[0]).toHaveClass('text-green-600');
    expect(screen.getByText('FAILURE')).toHaveClass('text-red-600');
  });

  it('should show the full URI as a tooltip', () => {
    render(<MetricsTable metrics={[metric({ uri: '/api/a/very/long/uri/that/may/be/truncated' })]} />);

    expect(screen.getByTitle('/api/a/very/long/uri/that/may/be/truncated')).toBeInTheDocument();
  });

  it('should sort by most recent first, then by the clicked column', async () => {
    render(<MetricsTable metrics={mockMetrics} />);

    expect(within(bodyRows()[0]).getByText('/api/orders')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('columnheader', { name: /Duration/ }));
    expect(within(bodyRows()[0]).getByText('1.50s')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('columnheader', { name: /Duration/ }));
    expect(within(bodyRows()[0]).getByText('150ms')).toBeInTheDocument();
  });

  it('should support dark mode classes', () => {
    render(<MetricsTable metrics={mockMetrics} />);

    expect(screen.getByRole('table').querySelector('tbody')).toHaveClass('dark:bg-gray-800');
  });
});
