/**
 * Integration Test: User Registration Flow
 * 
 * Tests the complete user registration flow including form submission,
 * validation, API communication, and successful redirect to login.
 * 
 * ⚠️ This test MUST FAIL until RegisterForm and authApi are implemented
 */

import { describe, it, expect, beforeAll, afterEach, afterAll, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { BrowserRouter } from 'react-router-dom';
import { RegisterForm } from '@/features/auth/components/RegisterForm';
import { authApi } from '@/services/authApi';
import type { RegisterRequest } from '@/types/api';

const API_BASE_URL = 'http://localhost:8080/api';

const handlers = [
  http.post(`${API_BASE_URL}/auth/register`, async ({ request }) => {
    const body = await request.json() as RegisterRequest;
    
    if (body.username === 'existing@example.com') {
      return HttpResponse.json(
        { error: 'Email already registered' },
        { status: 409 }
      );
    }
    
    return HttpResponse.json(
      'User registered successfully',
      { status: 201 }
    );
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('User Registration Flow - Integration Test', () => {
  it('should complete registration flow successfully', async () => {
    const user = userEvent.setup();
    const onSuccess = vi.fn();
    
    render(
      <BrowserRouter>
        <RegisterForm onSuccess={onSuccess} />
      </BrowserRouter>
    );
    
    // Fill in registration form
    await user.type(screen.getByLabelText(/email/i), 'newuser@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'SecurePass123!');
    await user.type(screen.getByLabelText(/confirm password/i), 'SecurePass123!');
    
    // Submit form
    await user.click(screen.getByRole('button', { name: /register/i }));
    
    // Wait for successful registration
    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalled();
    });
  });
  
  it('should show error for duplicate email', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <RegisterForm onSubmit={(data) => authApi.register(data)} />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/email/i), 'existing@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'SecurePass123!');
    await user.type(screen.getByLabelText(/confirm password/i), 'SecurePass123!');
    await user.click(screen.getByRole('button', { name: /register/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/already registered/i)).toBeInTheDocument();
    });
  });
  
  it('should validate password confirmation', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <RegisterForm />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/email/i), 'user@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'SecurePass123!');
    await user.type(screen.getByLabelText(/confirm password/i), 'DifferentPass456!');
    await user.click(screen.getByRole('button', { name: /register/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
    });
  });
  
  it('should disable submit button while loading', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <RegisterForm onSubmit={(data) => authApi.register(data)} />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/email/i), 'user@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'SecurePass123!');
    await user.type(screen.getByLabelText(/confirm password/i), 'SecurePass123!');
    
    const submitButton = screen.getByRole('button', { name: /register/i });
    
    // Click and check immediately in a waitFor to catch the loading state
    user.click(submitButton);
    
    await waitFor(() => {
      expect(submitButton).toBeDisabled();
    });
  });
});
