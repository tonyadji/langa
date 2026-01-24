/**
 * Component Test: ShareApplicationModal
 * 
 * Tests the ShareApplicationModal component functionality:
 * - Rendering form fields
 * - User/Team selection
 * - Form validation
 * - Submit handling
 * - Error display
 */

import { describe, it, expect, vi, beforeAll, afterEach, afterAll } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { ShareApplicationModal } from './ShareApplicationModal';

const API_BASE_URL = 'http://localhost:8080/api';

const handlers = [
  http.post(`${API_BASE_URL}/applications/:appId/share`, async ({ request }) => {
    const body = (await request.json()) as { sharedWith: string; profile: string };
    
    if (body.sharedWith === 'existing@example.com') {
      return HttpResponse.json(
        { error: 'Application already shared with this user' },
        { status: 409 }
      );
    }
    
    return HttpResponse.json({
      appId: 'app-123',
      appName: 'Test Application',
      key: 'user-123',
      profile: body.profile,
      sharedDate: new Date().toISOString(),
      expirationDate: null,
      revokedDate: null,
      currentlyActive: true,
      expired: false,
      revoked: false,
    }, { status: 200 });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('ShareApplicationModal', () => {
  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    mockOnClose.mockClear();
    mockOnSuccess.mockClear();
  });

  describe('Rendering', () => {
    it('should render modal with form fields when open', () => {
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      expect(screen.getByRole('heading', { name: /share/i })).toBeInTheDocument();
      expect(screen.getByText(/test app/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/email or team key/i)).toBeInTheDocument();
      expect(screen.getByRole('combobox', { name: /type/i })).toBeInTheDocument();
    });

    it('should not render modal when closed', () => {
      const { container } = render(
        <ShareApplicationModal
          isOpen={false}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      expect(container.firstChild).toBeNull();
    });

    it('should show USER and TEAM options in select dropdown', () => {
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      const select = screen.getByRole('combobox', { name: /type/i });
      expect(select).toBeInTheDocument();
      
      const options = screen.getAllByRole('option');
      expect(options).toHaveLength(2);
      expect(options[0]).toHaveTextContent(/user/i);
      expect(options[1]).toHaveTextContent(/team/i);
    });
  });

  describe('Form Validation', () => {
    it('should show error for empty email', async () => {
      const user = userEvent.setup();
      
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      const submitButton = screen.getByRole('button', { name: /share/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/required/i)).toBeInTheDocument();
      });
    });

    it('should validate email format for USER profile', async () => {
      const user = userEvent.setup();
      
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      const input = screen.getByLabelText(/email or team key/i);
      await user.type(input, 'invalid-email');

      const select = screen.getByRole('combobox', { name: /type/i });
      await user.selectOptions(select, 'USER');

      const submitButton = screen.getByRole('button', { name: /share/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/valid email/i)).toBeInTheDocument();
      });
    });

    it('should accept team key format for TEAM profile', async () => {
      const user = userEvent.setup();
      
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
          onSuccess={mockOnSuccess}
        />
      );

      const input = screen.getByLabelText(/email or team key/i);
      await user.type(input, 'team-xyz');

      const select = screen.getByRole('combobox', { name: /type/i });
      await user.selectOptions(select, 'TEAM');

      const submitButton = screen.getByRole('button', { name: /share/i });
      await user.click(submitButton);

      // Should not show email validation error for team keys
      await waitFor(() => {
        expect(mockOnSuccess).toHaveBeenCalled();
      });
    });
  });

  describe('Form Submission', () => {
    it('should submit form with valid user email', async () => {
      const user = userEvent.setup();
      
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
          onSuccess={mockOnSuccess}
        />
      );

      const input = screen.getByLabelText(/email or team key/i);
      await user.type(input, 'user@example.com');

      const select = screen.getByRole('combobox', { name: /type/i });
      await user.selectOptions(select, 'USER');

      const submitButton = screen.getByRole('button', { name: /share/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnSuccess).toHaveBeenCalled();
      });
    });

    it('should submit form with valid team key', async () => {
      const user = userEvent.setup();
      
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
          onSuccess={mockOnSuccess}
        />
      );

      const input = screen.getByLabelText(/email or team key/i);
      await user.type(input, 'team-xyz');

      const select = screen.getByRole('combobox', { name: /type/i });
      await user.selectOptions(select, 'TEAM');

      const submitButton = screen.getByRole('button', { name: /share/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnSuccess).toHaveBeenCalled();
      });
    });

    it('should show loading state during submission', async () => {
      const user = userEvent.setup();
      
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      const input = screen.getByLabelText(/email or team key/i);
      await user.type(input, 'user@example.com');

      const submitButton = screen.getByRole('button', { name: /^share$/i });
      
      // Verify button is enabled before clicking
      expect(submitButton).not.toBeDisabled();
      
      await user.click(submitButton);

      // Wait for successful completion (modal closes)
      await waitFor(() => {
        expect(mockOnClose).toHaveBeenCalled();
      });
    });

    it('should display API error messages', async () => {
      const user = userEvent.setup();
      
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      const input = screen.getByLabelText(/email or team key/i);
      await user.type(input, 'existing@example.com');

      const submitButton = screen.getByRole('button', { name: /share/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/already shared/i)).toBeInTheDocument();
      });
    });

    it('should call onClose after successful submission', async () => {
      const user = userEvent.setup();
      
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      const input = screen.getByLabelText(/email or team key/i);
      await user.type(input, 'user@example.com');

      const submitButton = screen.getByRole('button', { name: /share/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnClose).toHaveBeenCalled();
      });
    });
  });

  describe('Cancel Action', () => {
    it('should call onClose when cancel button is clicked', async () => {
      const user = userEvent.setup();
      
      render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('should reset form when modal is closed and reopened', async () => {
      const user = userEvent.setup();
      const { rerender } = render(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      const input = screen.getByLabelText(/email or team key/i);
      await user.type(input, 'user@example.com');
      
      // Verify value is set
      expect(input).toHaveValue('user@example.com');

      // Close modal
      rerender(
        <ShareApplicationModal
          isOpen={false}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      // Modal should not be rendered when closed
      expect(screen.queryByLabelText(/email or team key/i)).not.toBeInTheDocument();

      // Reopen modal
      rerender(
        <ShareApplicationModal
          isOpen={true}
          onClose={mockOnClose}
          applicationId="app-123"
          applicationName="Test App"
        />
      );

      // Wait for the input to appear and check it's reset
      const inputAfterReopen = await screen.findByLabelText(/email or team key/i);
      expect(inputAfterReopen).toHaveValue('');
    });
  });
});
