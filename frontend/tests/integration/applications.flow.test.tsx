/**
 * Integration Test: Application Creation Flow
 * 
 * Validates the end-to-end application creation workflow from UI interaction
 * to API response and UI update.
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete
 */

import { describe, it, expect, beforeAll, afterEach, afterAll, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { BrowserRouter } from 'react-router-dom';
import { CreateApplicationModal } from '@/features/applications/components/CreateApplicationModal';
import type { Application } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';

const handlers = [
  http.post(`${API_BASE_URL}/applications`, async ({ request }) => {
    const body = await request.json() as { name: string };
    
    if (body.name === 'existing-app') {
      return HttpResponse.json(
        { error: 'Application name already exists' },
        { status: 409 }
      );
    }
    
    if (!body.name || body.name.trim() === '') {
      return HttpResponse.json(
        { error: 'Application name is required' },
        { status: 400 }
      );
    }
    
    const application: Application = {
      id: 'new-app-123',
      name: body.name,
      key: `${body.name.toLowerCase().replace(/\s+/g, '-')}-key-abc`,
      accountKey: 'account-key-xyz',
      ingestionUri: `http://localhost:8080/ingest/${body.name.toLowerCase().replace(/\s+/g, '-')}-key-abc`,
      owner: 'user@example.com',
      sharedWith: [],
      createdAt: new Date().toISOString(),
    };
    
    return HttpResponse.json(application, { status: 201 });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('Application Creation Flow - Integration Test', () => {
  it('should complete application creation flow successfully', async () => {
    const user = userEvent.setup();
    const onSuccess = vi.fn();
    const onClose = vi.fn();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal
          isOpen={true}
          onClose={onClose}
          onSuccess={onSuccess}
        />
      </BrowserRouter>
    );
    
    // Fill in application name
    const nameInput = screen.getByLabelText(/application name/i);
    await user.type(nameInput, 'My New Application');
    
    // Submit form
    await user.click(screen.getByRole('button', { name: /create/i }));
    
    // Wait for successful creation
    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'My New Application',
          owner: 'user@example.com',
        })
      );
    });
  });
  
  it('should show error for duplicate application name', async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal
          isOpen={true}
          onClose={onClose}
        />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/application name/i), 'existing-app');
    await user.click(screen.getByRole('button', { name: /create/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/already exists/i)).toBeInTheDocument();
    });
  });
  
  it('should validate required fields', async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal
          isOpen={true}
          onClose={onClose}
        />
      </BrowserRouter>
    );
    
    // Try to submit without filling name
    const submitButton = screen.getByRole('button', { name: /create/i });
    await user.click(submitButton);
    
    // Should show validation error or prevent submission
    const nameInput = screen.getByLabelText(/application name/i);
    expect(nameInput).toHaveAttribute('required');
  });
  
  it('should disable submit button while loading', async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    
    // Delay the response to test loading state
    server.use(
      http.post(`${API_BASE_URL}/applications`, async () => {
        await new Promise(resolve => setTimeout(resolve, 100));
        return HttpResponse.json({
          id: 'app-123',
          name: 'Test App',
          key: 'test-app-key',
          accountKey: 'account-key',
          ingestionUri: 'http://localhost:8080/ingest/test-app-key',
          owner: 'user@example.com',
          sharedWith: [],
          createdAt: new Date().toISOString(),
        }, { status: 201 });
      })
    );
    
    render(
      <BrowserRouter>
        <CreateApplicationModal
          isOpen={true}
          onClose={onClose}
        />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/application name/i), 'Loading Test');
    const submitButton = screen.getByRole('button', { name: /create/i });
    await user.click(submitButton);
    
    // Button should be disabled during submission
    expect(submitButton).toBeDisabled();
  });
});
