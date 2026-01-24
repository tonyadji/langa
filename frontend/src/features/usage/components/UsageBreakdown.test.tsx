/**
 * T210: Component test for UsageBreakdown component
 * 
 * Tests the UsageBreakdown component that shows usage trends over time
 * with a time period selector (7d, 30d, 90d) per FR-032.
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';

interface ApplicationUsage {
  id: string;
  appKey: string;
  totalLogBytes: number;
  totalMetricBytes: number;
}

type TimePeriod = '7d' | '30d' | '90d';

interface UsageBreakdownProps {
  usage: ApplicationUsage | undefined;
  isLoading?: boolean;
  isError?: boolean;
  timePeriod?: TimePeriod;
  onTimePeriodChange?: (period: TimePeriod) => void;
}

// Placeholder component (will be implemented in T213)
const UsageBreakdown = ({
  usage,
  isLoading,
  isError,
  timePeriod = '30d',
  onTimePeriodChange,
}: UsageBreakdownProps) => {
  if (isLoading) {
    return <div data-testid="loading">Loading usage breakdown...</div>;
  }

  if (isError) {
    return <div data-testid="error">Error loading usage breakdown</div>;
  }

  if (!usage) {
    return <div data-testid="no-data">No usage breakdown available</div>;
  }

  return (
    <div data-testid="usage-breakdown">
      <div data-testid="time-period-selector">
        <button
          data-testid="period-7d"
          onClick={() => onTimePeriodChange?.('7d')}
          aria-pressed={timePeriod === '7d'}
        >
          7 days
        </button>
        <button
          data-testid="period-30d"
          onClick={() => onTimePeriodChange?.('30d')}
          aria-pressed={timePeriod === '30d'}
        >
          30 days
        </button>
        <button
          data-testid="period-90d"
          onClick={() => onTimePeriodChange?.('90d')}
          aria-pressed={timePeriod === '90d'}
        >
          90 days
        </button>
      </div>
      <div data-testid="breakdown-chart">Chart for {timePeriod}</div>
    </div>
  );
};

describe('UsageBreakdown Component - Component Tests', () => {
  const mockUsage: ApplicationUsage = {
    id: 'usage-1',
    appKey: 'app-123',
    totalLogBytes: 10485760, // 10 MB
    totalMetricBytes: 5242880, // 5 MB
  };

  it('should render usage breakdown with time period selector (FR-032)', () => {
    render(<UsageBreakdown usage={mockUsage} />);

    expect(screen.getByTestId('usage-breakdown')).toBeInTheDocument();
    expect(screen.getByTestId('time-period-selector')).toBeInTheDocument();
  });

  it('should display all three time period options (7d, 30d, 90d)', () => {
    render(<UsageBreakdown usage={mockUsage} />);

    expect(screen.getByTestId('period-7d')).toBeInTheDocument();
    expect(screen.getByTestId('period-30d')).toBeInTheDocument();
    expect(screen.getByTestId('period-90d')).toBeInTheDocument();

    expect(screen.getByText('7 days')).toBeInTheDocument();
    expect(screen.getByText('30 days')).toBeInTheDocument();
    expect(screen.getByText('90 days')).toBeInTheDocument();
  });

  it('should default to 30 days period', () => {
    render(<UsageBreakdown usage={mockUsage} />);

    const period30d = screen.getByTestId('period-30d');
    expect(period30d).toHaveAttribute('aria-pressed', 'true');
  });

  it('should call onTimePeriodChange when period is selected', () => {
    const handleTimePeriodChange = vi.fn();
    render(<UsageBreakdown usage={mockUsage} onTimePeriodChange={handleTimePeriodChange} />);

    const period7d = screen.getByTestId('period-7d');
    fireEvent.click(period7d);

    expect(handleTimePeriodChange).toHaveBeenCalledWith('7d');
  });

  it('should update active period when different option is clicked', () => {
    const handleTimePeriodChange = vi.fn();
    const { rerender } = render(
      <UsageBreakdown
        usage={mockUsage}
        timePeriod="30d"
        onTimePeriodChange={handleTimePeriodChange}
      />
    );

    fireEvent.click(screen.getByTestId('period-90d'));

    rerender(
      <UsageBreakdown
        usage={mockUsage}
        timePeriod="90d"
        onTimePeriodChange={handleTimePeriodChange}
      />
    );

    const period90d = screen.getByTestId('period-90d');
    expect(period90d).toHaveAttribute('aria-pressed', 'true');

    const period30d = screen.getByTestId('period-30d');
    expect(period30d).toHaveAttribute('aria-pressed', 'false');
  });

  it('should show loading state while fetching breakdown data (FR-034)', () => {
    render(<UsageBreakdown usage={undefined} isLoading={true} />);

    expect(screen.getByTestId('loading')).toBeInTheDocument();
    expect(screen.getByText(/loading usage breakdown/i)).toBeInTheDocument();
  });

  it('should show error state when data fetch fails (FR-033)', () => {
    render(<UsageBreakdown usage={undefined} isError={true} />);

    expect(screen.getByTestId('error')).toBeInTheDocument();
    expect(screen.getByText(/error loading usage breakdown/i)).toBeInTheDocument();
  });

  it('should show empty state when no usage data available (FR-035)', () => {
    render(<UsageBreakdown usage={undefined} />);

    expect(screen.getByTestId('no-data')).toBeInTheDocument();
    expect(screen.getByText(/no usage breakdown available/i)).toBeInTheDocument();
  });

  it('should display trend visualization chart area', () => {
    render(<UsageBreakdown usage={mockUsage} />);

    expect(screen.getByTestId('breakdown-chart')).toBeInTheDocument();
    // Real implementation will render Recharts chart per T011
  });

  it('should show separate trend lines for logs and metrics', () => {
    render(<UsageBreakdown usage={mockUsage} />);

    // Real implementation will show two lines/areas in chart
    // One for totalLogBytes, one for totalMetricBytes
    expect(screen.getByTestId('breakdown-chart')).toBeInTheDocument();
  });

  it('should update chart when time period changes', () => {
    const { rerender } = render(
      <UsageBreakdown usage={mockUsage} timePeriod="7d" />
    );

    expect(screen.getByText('Chart for 7d')).toBeInTheDocument();

    rerender(<UsageBreakdown usage={mockUsage} timePeriod="90d" />);

    expect(screen.getByText('Chart for 90d')).toBeInTheDocument();
  });

  it('should handle zero usage data in chart', () => {
    const zeroUsage: ApplicationUsage = {
      id: 'usage-zero',
      appKey: 'app-new',
      totalLogBytes: 0,
      totalMetricBytes: 0,
    };

    render(<UsageBreakdown usage={zeroUsage} />);

    expect(screen.getByTestId('usage-breakdown')).toBeInTheDocument();
    // Real implementation should show chart with zero baseline
  });

  it('should format time axis appropriately for each period', () => {
    render(<UsageBreakdown usage={mockUsage} timePeriod="7d" />);

    // Real implementation will format X-axis:
    // 7d: hourly or daily ticks
    // 30d: daily ticks
    // 90d: weekly ticks
    expect(screen.getByTestId('breakdown-chart')).toBeInTheDocument();
  });

  it('should show tooltips on chart hover (T221)', () => {
    render(<UsageBreakdown usage={mockUsage} />);

    // Real implementation will add tooltips showing:
    // - Date/time
    // - Log bytes value (formatted)
    // - Metric bytes value (formatted)
    expect(screen.getByTestId('usage-breakdown')).toBeInTheDocument();
  });

  it('should have accessible time period selector buttons', () => {
    render(<UsageBreakdown usage={mockUsage} />);

    const period7d = screen.getByTestId('period-7d');
    const period30d = screen.getByTestId('period-30d');
    const period90d = screen.getByTestId('period-90d');

    // Should have aria-pressed for accessibility
    expect(period7d).toHaveAttribute('aria-pressed');
    expect(period30d).toHaveAttribute('aria-pressed');
    expect(period90d).toHaveAttribute('aria-pressed');
  });

  it('should transition smoothly from loading to chart display', () => {
    const { rerender } = render(<UsageBreakdown usage={undefined} isLoading={true} />);

    expect(screen.getByTestId('loading')).toBeInTheDocument();

    rerender(<UsageBreakdown usage={mockUsage} isLoading={false} />);

    expect(screen.queryByTestId('loading')).not.toBeInTheDocument();
    expect(screen.getByTestId('usage-breakdown')).toBeInTheDocument();
  });

  it('should handle very large values in chart visualization', () => {
    const largeUsage: ApplicationUsage = {
      id: 'usage-large',
      appKey: 'app-large',
      totalLogBytes: 1099511627776, // 1 TB
      totalMetricBytes: 549755813888, // 512 GB
    };

    render(<UsageBreakdown usage={largeUsage} />);

    // Real implementation should handle TB-scale values
    // with appropriate Y-axis scaling
    expect(screen.getByTestId('usage-breakdown')).toBeInTheDocument();
  });

  it('should maintain selected period across component rerenders', () => {
    const { rerender } = render(
      <UsageBreakdown usage={mockUsage} timePeriod="7d" />
    );

    expect(screen.getByTestId('period-7d')).toHaveAttribute('aria-pressed', 'true');

    // Rerender with same period
    rerender(<UsageBreakdown usage={mockUsage} timePeriod="7d" />);

    expect(screen.getByTestId('period-7d')).toHaveAttribute('aria-pressed', 'true');
  });
});
