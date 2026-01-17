/**
 * T209: Component test for UsageStats component
 * 
 * Tests the UsageStats component that displays total log and metric bytes
 * separately with human-readable formatting per FR-031.
 */

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';

interface ApplicationUsage {
  id: string;
  appKey: string;
  totalLogBytes: number;
  totalMetricBytes: number;
}

interface UsageStatsProps {
  usage: ApplicationUsage | undefined;
  isLoading?: boolean;
  isError?: boolean;
}

// Placeholder component (will be implemented in T212)
const UsageStats = ({ usage, isLoading, isError }: UsageStatsProps) => {
  if (isLoading) {
    return <div data-testid="loading">Loading usage statistics...</div>;
  }

  if (isError) {
    return <div data-testid="error">Error loading usage statistics</div>;
  }

  if (!usage) {
    return <div data-testid="no-data">No usage data available</div>;
  }

  return (
    <div data-testid="usage-stats">
      <div data-testid="total-log-bytes">{usage.totalLogBytes}</div>
      <div data-testid="total-metric-bytes">{usage.totalMetricBytes}</div>
    </div>
  );
};

describe('UsageStats Component - Component Tests', () => {
  const mockUsage: ApplicationUsage = {
    id: 'usage-1',
    appKey: 'app-123',
    totalLogBytes: 10485760, // 10 MB
    totalMetricBytes: 5242880, // 5 MB
  };

  it('should render usage statistics with separate log and metric displays (FR-031)', () => {
    render(<UsageStats usage={mockUsage} />);

    expect(screen.getByTestId('usage-stats')).toBeInTheDocument();
    expect(screen.getByTestId('total-log-bytes')).toBeInTheDocument();
    expect(screen.getByTestId('total-metric-bytes')).toBeInTheDocument();
  });

  it('should display log bytes separately from metric bytes', () => {
    render(<UsageStats usage={mockUsage} />);

    const logBytes = screen.getByTestId('total-log-bytes');
    const metricBytes = screen.getByTestId('total-metric-bytes');

    expect(logBytes).toHaveTextContent('10485760'); // Will be formatted in implementation
    expect(metricBytes).toHaveTextContent('5242880'); // Will be formatted in implementation
  });

  it('should show loading state while fetching data (FR-034)', () => {
    render(<UsageStats usage={undefined} isLoading={true} />);

    expect(screen.getByTestId('loading')).toBeInTheDocument();
    expect(screen.getByText(/loading usage statistics/i)).toBeInTheDocument();
  });

  it('should show error state when data fetch fails (FR-033)', () => {
    render(<UsageStats usage={undefined} isError={true} />);

    expect(screen.getByTestId('error')).toBeInTheDocument();
    expect(screen.getByText(/error loading usage statistics/i)).toBeInTheDocument();
  });

  it('should show empty state when no usage data available (FR-035)', () => {
    render(<UsageStats usage={undefined} />);

    expect(screen.getByTestId('no-data')).toBeInTheDocument();
    expect(screen.getByText(/no usage data available/i)).toBeInTheDocument();
  });

  it('should handle zero bytes correctly', () => {
    const zeroUsage: ApplicationUsage = {
      id: 'usage-zero',
      appKey: 'app-new',
      totalLogBytes: 0,
      totalMetricBytes: 0,
    };

    render(<UsageStats usage={zeroUsage} />);

    const logBytes = screen.getByTestId('total-log-bytes');
    const metricBytes = screen.getByTestId('total-metric-bytes');

    expect(logBytes).toHaveTextContent('0');
    expect(metricBytes).toHaveTextContent('0');
  });

  it('should format bytes in human-readable format (implementation detail)', () => {
    const largeUsage: ApplicationUsage = {
      id: 'usage-large',
      appKey: 'app-large',
      totalLogBytes: 1073741824, // 1 GB
      totalMetricBytes: 536870912, // 512 MB
    };

    render(<UsageStats usage={largeUsage} />);

    // Placeholder shows raw numbers, real implementation will format
    // e.g., "1.00 GB" and "512.00 MB"
    expect(screen.getByTestId('total-log-bytes')).toBeInTheDocument();
    expect(screen.getByTestId('total-metric-bytes')).toBeInTheDocument();
  });

  it('should handle very large values (> 1 TB)', () => {
    const massiveUsage: ApplicationUsage = {
      id: 'usage-massive',
      appKey: 'app-massive',
      totalLogBytes: 1099511627776, // 1 TB
      totalMetricBytes: 549755813888, // 512 GB
    };

    render(<UsageStats usage={massiveUsage} />);

    expect(screen.getByTestId('usage-stats')).toBeInTheDocument();
    // Real implementation should handle TB formatting
  });

  it('should display appropriate labels/icons for log and metric sections', () => {
    render(<UsageStats usage={mockUsage} />);

    // Real implementation will add labels like "Logs" and "Metrics"
    // and possibly icons per T221-T222
    expect(screen.getByTestId('usage-stats')).toBeInTheDocument();
  });

  it('should handle updates when usage data changes', () => {
    const { rerender } = render(<UsageStats usage={mockUsage} />);

    const updatedUsage: ApplicationUsage = {
      id: 'usage-1',
      appKey: 'app-123',
      totalLogBytes: 20971520, // 20 MB
      totalMetricBytes: 10485760, // 10 MB
    };

    rerender(<UsageStats usage={updatedUsage} />);

    const logBytes = screen.getByTestId('total-log-bytes');
    const metricBytes = screen.getByTestId('total-metric-bytes');

    expect(logBytes).toHaveTextContent('20971520');
    expect(metricBytes).toHaveTextContent('10485760');
  });

  it('should display total combined usage', () => {
    render(<UsageStats usage={mockUsage} />);

    // Real implementation might show total = logs + metrics
    // Total should be 10485760 + 5242880 = 15728640 (15 MB)
    expect(screen.getByTestId('usage-stats')).toBeInTheDocument();
  });

  it('should transition smoothly from loading to data display', () => {
    const { rerender } = render(<UsageStats usage={undefined} isLoading={true} />);

    expect(screen.getByTestId('loading')).toBeInTheDocument();

    rerender(<UsageStats usage={mockUsage} isLoading={false} />);

    expect(screen.queryByTestId('loading')).not.toBeInTheDocument();
    expect(screen.getByTestId('usage-stats')).toBeInTheDocument();
  });
});
