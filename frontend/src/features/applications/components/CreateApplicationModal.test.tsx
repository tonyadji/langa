/**
 * Component Test: CreateApplicationModal
 * 
 * Tests the CreateApplicationModal component for creating new applications.
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete
 */

import { describe, it, expect, vi, beforeAll, afterEach, afterAll } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { CreateApplicationModal } from '@/features/applications/components/CreateApplicationModal';

const API_BASE_URL = 'http://localhost:8080/api';

const handlers = [
  http.post(`${API_BASE_URL}/applications`, async ({ request }) => {
    const body = await request.json() as { name: string };
    
    if (body.name === 'error-trigger') {
      return HttpResponse.json(
        { error: 'Application creation failed' },
        { status: 400 }
      );
    }
    
    return HttpResponse.json({
      id: 'new-app-123',
      name: body.name,
      key: 'generated-app-key',
      accountKey: 'account-key',
      ingestionUri: 'http://localhost:8080/ingest/generated-app-key',
      owner: 'testuser',
      sharedWith: [],
      createdAt: new Date().toISOString(),
    });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('CreateApplicationModal', () => {
  it('should render modal when open', () => {
    render(
      <BrowserRouter>
        <CreateApplicationModal isOpen={true} onClose={vi.fn()} />
      </BrowserRouter>
    );
    
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(/create application/i)).toBeInTheDocument();
  });
  
  it('should not render when closed', () => {
    render(
      <BrowserRouter>
        <CreateApplicationModal isOpen={false} onClose={vi.fn()} />
      </BrowserRouter>
    );
    
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });
  
  it('should display application name input', () => {
    render(
      <BrowserRouter>
        <CreateApplicationModal isOpen={true} onClose={vi.fn()} />
      </BrowserRouter>
    );
    
    expect(screen.getByLabelText(/application name/i)).toBeInTheDocument();
  });
  
  it('should have submit and cancel buttons', () => {
    render(
      <BrowserRouter>
        <CreateApplicationModal isOpen={true} onClose={vi.fn()} />
      </BrowserRouter>
    );
    
    expect(screen.getByRole('button', { name: /create/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
  });
  
  it('should call onClose when cancel button is clicked', async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal isOpen={true} onClose={onClose} />
      </BrowserRouter>
    );
    
    await user.click(screen.getByRole('button', { name: /cancel/i }));
    
    expect(onClose).toHaveBeenCalled();
  });
  
  it('should validate required fields', async () => {
    const user = userEvent.setup();
    const onSuccess = vi.fn();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal
          isOpen={true}
          onClose={vi.fn()}
          onSuccess={onSuccess}
        />
      </BrowserRouter>
    );
    
    // Try to submit without filling name
    await user.click(screen.getByRole('button', { name: /create/i }));
    
    // Should not call onSuccess
    expect(onSuccess).not.toHaveBeenCalled();
  });
  
  it('should show validation error for empty name', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal isOpen={true} onClose={vi.fn()} />
      </BrowserRouter>
    );
    
    const nameInput = screen.getByLabelText(/application name/i);
    await user.type(nameInput, 'test');
    await user.clear(nameInput);
    await user.click(screen.getByRole('button', { name: /create/i }));
    
    // Should show validation message
    const input = screen.getByLabelText(/application name/i);
    expect(input).toHaveAttribute('required');
  });
  
  it('should disable submit button while submitting', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal isOpen={true} onClose={vi.fn()} />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/application name/i), 'Test App');
    const submitButton = screen.getByRole('button', { name: /create/i });
    
    await user.click(submitButton);
    
    // Button should be disabled during submission
    await waitFor(() => {
      expect(submitButton).toBeDisabled();
    });
  });
  
  it('should call onSuccess with created application', async () => {
    const user = userEvent.setup();
    const onSuccess = vi.fn();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal
          isOpen={true}
          onClose={vi.fn()}
          onSuccess={onSuccess}
        />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/application name/i), 'New Application');
    await user.click(screen.getByRole('button', { name: /create/i }));
    
    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'New Application',
        })
      );
    });
  });
  
  it('should display error message on creation failure', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal isOpen={true} onClose={vi.fn()} />
      </BrowserRouter>
    );
    
    // Simulate API error by using a name that triggers error
    await user.type(screen.getByLabelText(/application name/i), 'error-trigger');
    await user.click(screen.getByRole('button', { name: /create/i }));
    
    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });
  });
  
  it('should reset form after successful creation', async () => {
    const user = userEvent.setup();
    const onSuccess = vi.fn();
    
    render(
      <BrowserRouter>
        <CreateApplicationModal
          isOpen={true}
          onClose={vi.fn()}
          onSuccess={onSuccess}
        />
      </BrowserRouter>
    );
    
    const nameInput = screen.getByLabelText(/application name/i) as HTMLInputElement;
    await user.type(nameInput, 'Test App');
    await user.click(screen.getByRole('button', { name: /create/i }));
    
    await waitFor(() => {
      expect(nameInput.value).toBe('');
    });
  });
});
