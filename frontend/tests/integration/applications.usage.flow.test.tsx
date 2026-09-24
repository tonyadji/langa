/**
 * T207: Integration test for usage data flow
 * 
 * Tests the complete flow of fetching and displaying usage statistics
 * in the application details page. Validates data fetching, error handling,
 * and integration between components per FR-031 and FR-032.
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { useApplicationUsage } from '@/features/usage/hooks/useApplicationUsage';
import { UsageStats } from '@/features/usage/components/UsageStats';
import { apiClient } from '@/services/api';

// Mock apiClient
vi.mock('@/services/api', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

interface ApplicationUsage {
  id: string;
  appKey: string;
  totalLogBytes: number;
  totalMetricBytes: number;
}

// Mock application details page that displays usage
const MockApplicationDetailsPage = ({ appId }: { appId: string }) => {
  const { data: usage, isLoading, isError } = useApplicationUsage(appId);

  return (
    <div>
      <h1>Application Details</h1>
      <div data-testid="app-id">{appId}</div>
      <UsageStats usage={usage} isLoading={isLoading} isError={isError} />
    </div>
  );
};

describe('Application Usage - Integration Tests', () => {
  const mockUsage: ApplicationUsage = {
    id: 'usage-123',
    appKey: 'app-123',
    totalLogBytes: 10485760, // 10 MB
    totalMetricBytes: 5242880, // 5 MB
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should fetch and display usage data in application details page', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: mockUsage,
    });

    render(
      <BrowserRouter>
        <MockApplicationDetailsPage appId="app-123" />
      </BrowserRouter>
    );

    // Should show loading initially
    expect(screen.getByTestId('loading')).toBeInTheDocument();

    // Wait for data to load
    await waitFor(() => {
      expect(screen.getByTestId('usage-stats')).toBeInTheDocument();
    });

    // Should display usage statistics
    expect(screen.getByTestId('total-log-bytes')).toBeInTheDocument();
    expect(screen.getByTestId('total-metric-bytes')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/applications/app-123/usage');
  });

  it('should show loading state while fetching usage data (FR-034)', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    render(
      <BrowserRouter>
        <MockApplicationDetailsPage appId="app-123" />
      </BrowserRouter>
    );

    // Initially should show loading
    expect(screen.getByTestId('loading')).toBeInTheDocument();
  });

  it('should handle 403 error when user is not the owner (FR-023)', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockRejectedValue({
      response: { status: 403 },
    });

    render(
      <BrowserRouter>
        <MockApplicationDetailsPage appId="app-123" />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('error')).toBeInTheDocument();
    });
  });

  it('should handle 404 error when application not found', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockRejectedValue({
      response: { status: 404 },
    });

    render(
      <BrowserRouter>
        <MockApplicationDetailsPage appId="app-999" />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('error')).toBeInTheDocument();
    });
  });

  it('should handle network errors (FR-033)', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockRejectedValue(
      new Error('Network error')
    );

    render(
      <BrowserRouter>
        <MockApplicationDetailsPage appId="app-123" />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('error')).toBeInTheDocument();
    });
  });

  it('should refetch usage data when application ID changes', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: mockUsage,
    });

    const { rerender } = render(
      <BrowserRouter>
        <MockApplicationDetailsPage appId="app-123" />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('usage-stats')).toBeInTheDocument();
    });

    expect(apiClient.get).toHaveBeenCalledWith('/applications/app-123/usage');

    // Change to different app
    const newMockUsage: ApplicationUsage = {
      id: 'usage-456',
      appKey: 'app-456',
      totalLogBytes: 20971520, // 20 MB
      totalMetricBytes: 10485760, // 10 MB
    };

    (apiClient.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: newMockUsage,
    });

    rerender(
      <BrowserRouter>
        <MockApplicationDetailsPage appId="app-456" />
      </BrowserRouter>
    );

    // Should call API with new app ID
    await waitFor(() => {
      expect(apiClient.get).toHaveBeenCalledWith('/applications/app-456/usage');
    });
  });
});

