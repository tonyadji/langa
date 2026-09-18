/**
 * Component Test: ApplicationCard
 * 
 * Tests the ApplicationCard component rendering and interaction.
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ApplicationCard } from '@/features/applications/components/ApplicationCard';
import { AuthContext } from '@/features/auth/context/AuthContext';
import type { Application } from '@/types';

const mockApplication: Application = {
  id: 'app-123',
  name: 'My Test Application',
  key: 'my-test-app-key',
  accountKey: 'account-key-xyz',
  ingestionUri: 'http://localhost:8080/ingest/my-test-app-key',
  owner: 'testuser',
  sharedWith: [],
  createdAt: '2024-01-01T00:00:00Z',
};

const mockSharedApplication: Application = {
  ...mockApplication,
  id: 'app-456',
  name: 'Shared Application',
  owner: 'otheruser',
  sharedWith: [
    {
      appId: 'app-456',
      appName: 'Shared Application',
      key: 'user-123',
      profile: 'USER',
      sharedDate: '2024-01-01T00:00:00Z',
      expirationDate: null,
      revokedDate: null,
      currentlyActive: true,
      expired: false,
      revoked: false,
    },
    {
      appId: 'app-456',
      appName: 'Shared Application',
      key: 'team-engineering',
      profile: 'TEAM',
      sharedDate: '2024-01-01T00:00:00Z',
      expirationDate: null,
      revokedDate: null,
      currentlyActive: true,
      expired: false,
      revoked: false,
    },
  ],
};

const mockAuthContextOwner = {
  user: { username: 'testuser', email: 'user@example.com' },
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn(),
  isAuthenticated: true,
  isLoading: false,
};

const mockAuthContextShared = {
  user: { username: 'differentuser', email: 'different@example.com' },
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn(),
  isAuthenticated: true,
  isLoading: false,
};

const renderWithAuth = (component: React.ReactElement, authValue: any) => {
  return render(
    <BrowserRouter>
      <AuthContext.Provider value={authValue}>
        {component}
      </AuthContext.Provider>
    </BrowserRouter>
  );
};

describe('ApplicationCard', () => {
  it('should render application name', () => {
    renderWithAuth(
      <ApplicationCard application={mockApplication} />,
      mockAuthContextOwner
    );
    
    expect(screen.getByText('My Test Application')).toBeInTheDocument();
  });
  
  it('should show "Owner" badge for owned applications', () => {
    renderWithAuth(
      <ApplicationCard application={mockApplication} />,
      mockAuthContextOwner
    );
    
    expect(screen.getByText(/Owner/i)).toBeInTheDocument();
  });
  
  it('should show "Shared" badge for shared applications', () => {
    renderWithAuth(
      <ApplicationCard application={mockSharedApplication} />,
      mockAuthContextShared
    );
    
    expect(screen.getByTestId('badge-shared')).toBeInTheDocument();
  });
  
  it('should display creation date', () => {
    renderWithAuth(
      <ApplicationCard application={mockApplication} />,
      mockAuthContextOwner
    );
    
    // Should show formatted date
    expect(screen.getByText(/1\/1\/2024/)).toBeInTheDocument();
  });
  
  it('should have link to application details', () => {
    renderWithAuth(
      <ApplicationCard application={mockApplication} />,
      mockAuthContextOwner
    );
    
    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/applications/app-123');
  });
  
  it('should display shared count when shared with multiple users/teams', () => {
    renderWithAuth(
      <ApplicationCard application={mockSharedApplication} />,
      mockAuthContextShared
    );
    
    // Should show number of users/teams it's shared with
    expect(screen.getByText(/Shared with 2 user\(s\)/i)).toBeInTheDocument();
  });
  
  it('should have quick action buttons for logs and metrics', () => {
    renderWithAuth(
      <ApplicationCard application={mockApplication} />,
      mockAuthContextOwner
    );
    
    expect(screen.getByRole('button', { name: /Logs/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Metrics/i })).toBeInTheDocument();
  });
  
  it('should apply hover styles to the card', () => {
    renderWithAuth(
      <ApplicationCard application={mockApplication} />,
      mockAuthContextOwner
    );
    
    const card = screen.getByTestId('application-card-app-123');
    expect(card).toHaveClass('hover:shadow-lg');
  });
});
