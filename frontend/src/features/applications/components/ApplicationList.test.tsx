/**
 * Component Test: ApplicationList
 * 
 * Tests the ApplicationList component with search and filtering functionality.
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete
 */

import { describe, it, expect } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '@/features/auth/context/AuthContext';
import { ApplicationList } from '@/features/applications/components/ApplicationList';
import type { Application } from '@/types';

// Test wrapper component
const TestWrapper = ({ children }: { children: React.ReactNode }) => (
  <BrowserRouter>
    <AuthProvider>
      {children}
    </AuthProvider>
  </BrowserRouter>
);

const mockApplications: Application[] = [
  {
    id: 'app-1',
    name: 'Production API',
    key: 'prod-api-key',
    accountKey: 'account-key',
    ingestionUri: 'http://localhost:8080/ingest/prod-api-key',
    owner: 'user@example.com',
    sharedWith: [],
    createdAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'app-2',
    name: 'Staging API',
    key: 'staging-api-key',
    accountKey: 'account-key',
    ingestionUri: 'http://localhost:8080/ingest/staging-api-key',
    owner: 'user@example.com',
    sharedWith: [],
    createdAt: '2024-01-02T00:00:00Z',
  },
  {
    id: 'app-3',
    name: 'Mobile App',
    key: 'mobile-app-key',
    accountKey: 'account-key',
    ingestionUri: 'http://localhost:8080/ingest/mobile-app-key',
    owner: 'other@example.com',
    sharedWith: [
      {
        appId: 'app-3',
        appName: 'Mobile App',
        key: 'user-123',
        profile: 'USER',
        sharedDate: '2024-01-03T00:00:00Z',
        expirationDate: null,
        revokedDate: null,
        currentlyActive: true,
        expired: false,
        revoked: false,
      },
    ],
    createdAt: '2024-01-03T00:00:00Z',
  },
];

describe('ApplicationList', () => {
  it('should render list of applications', () => {
    render(
      <TestWrapper>
        <ApplicationList applications={mockApplications} />
      </TestWrapper>
    );
    
    expect(screen.getByText('Production API')).toBeInTheDocument();
    expect(screen.getByText('Staging API')).toBeInTheDocument();
    expect(screen.getByText('Mobile App')).toBeInTheDocument();
  });
  
  it('should render applications in grid layout', () => {
    render(
      <TestWrapper>
        <ApplicationList applications={mockApplications} />
      </TestWrapper>
    );
    
    const grid = screen.getByTestId('applications-grid');
    expect(grid).toHaveClass('grid');
  });
  
  it('should display search input', () => {
    render(
      <TestWrapper>
        <ApplicationList applications={mockApplications} />
      </TestWrapper>
    );
    
    expect(screen.getByPlaceholderText(/search applications/i)).toBeInTheDocument();
  });
  
  it('should filter applications by name', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <ApplicationList applications={mockApplications} />
      </TestWrapper>
    );
    
    const searchInput = screen.getByPlaceholderText(/search applications/i);
    await user.type(searchInput, 'API');
    
    expect(screen.getByText('Production API')).toBeInTheDocument();
    expect(screen.getByText('Staging API')).toBeInTheDocument();
    expect(screen.queryByText('Mobile App')).not.toBeInTheDocument();
  });
  
  it('should perform case-insensitive search', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <ApplicationList applications={mockApplications} />
      </TestWrapper>
    );
    
    const searchInput = screen.getByPlaceholderText(/search applications/i);
    await user.type(searchInput, 'mobile');
    
    expect(screen.getByText('Mobile App')).toBeInTheDocument();
  });
  
  it('should show empty state when no applications', () => {
    render(
      <TestWrapper>
        <ApplicationList applications={[]} />
      </TestWrapper>
    );
    
    expect(screen.getByText(/no applications/i)).toBeInTheDocument();
  });
  
  it('should show empty state when search returns no results', async () => {
    const user = userEvent.setup();
    
    render(
      <TestWrapper>
        <ApplicationList applications={mockApplications} />
      </TestWrapper>
    );
    
    await user.type(screen.getByPlaceholderText(/search applications/i), 'nonexistent');
    
    expect(screen.getByText(/no applications found/i)).toBeInTheDocument();
  });
  
  it('should display application count', () => {
    render(
      <TestWrapper>
        <ApplicationList applications={mockApplications} />
      </TestWrapper>
    );
    
    const grid = screen.getByTestId('applications-grid');
    const cards = within(grid).getAllByRole('link');
    expect(cards).toHaveLength(3);
  });
  
  it('should navigate when card is clicked', async () => {
    render(
      <TestWrapper>
        <ApplicationList applications={mockApplications} />
      </TestWrapper>
    );
    
    const firstCard = screen.getByText('Production API').closest('a');
    expect(firstCard).toHaveAttribute('href', '/applications/app-1');
  });
  
  it('should render in responsive grid', () => {
    render(
      <TestWrapper>
        <ApplicationList applications={mockApplications} />
      </TestWrapper>
    );
    
    const grid = screen.getByTestId('applications-grid');
    // Should have responsive grid classes
    expect(grid).toHaveClass('grid');
    expect(grid.className).toMatch(/grid-cols/);
  });
});
