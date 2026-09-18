/**
 * Component Test: MetricsChart
 * Test ID: T140 | US4 - Metrics Visualization
 */

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MetricChart } from '@/features/metrics/components/MetricChart';

describe('MetricsChart Component Tests', () => {
  it('should render chart with data', () => {
    const mockData = [
      { timestamp: '2024-01-01T12:00:00Z', value: 45.2 },
    ];
    
    const mockConfig = {
      title: 'CPU Usage',
      type: 'line' as const,
      dataKey: 'value',
      color: '#3b82f6',
      unit: '%',
    };
    
    render(<MetricChart data={mockData} config={mockConfig} />);
    expect(screen.getByText('CPU Usage')).toBeInTheDocument();
  });
});
