/**
 * Component Test: RegisterForm
 * 
 * Tests the RegisterForm component including password confirmation,
 * email validation, successful submission, and error handling.
 * 
 * ⚠️ This test MUST FAIL until RegisterForm component is implemented
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { RegisterForm } from '@/features/auth/components/RegisterForm';

describe('RegisterForm Component Tests', () => {
  it('should render registration form fields', () => {
    render(
      <BrowserRouter>
        <RegisterForm />
      </BrowserRouter>
    );
    
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /register/i })).toBeInTheDocument();
  });
  
  it('should validate required fields', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    
    const { container } = render(
      <BrowserRouter>
        <RegisterForm onSubmit={onSubmit} />
      </BrowserRouter>
    );
    
    // Disable HTML5 validation to test custom validation
    const form = container.querySelector('form');
    if (form) {
      form.setAttribute('novalidate', 'true');
    }
    
    await user.click(screen.getByRole('button', { name: /register/i }));
    
    expect(screen.getByText(/email is required/i)).toBeInTheDocument();
    expect(screen.getByText(/password is required/i)).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });
  
  it('should validate email format', async () => {
    const user = userEvent.setup();
    
    const { container } = render(
      <BrowserRouter>
        <RegisterForm />
      </BrowserRouter>
    );
    
    // Disable HTML5 validation to test custom validation
    const form = container.querySelector('form');
    if (form) {
      form.setAttribute('novalidate', 'true');
    }
    
    await user.type(screen.getByLabelText(/email/i), 'invalidemail');
    await user.type(screen.getByLabelText(/^password$/i), 'SecurePass123!');
    await user.type(screen.getByLabelText(/confirm password/i), 'SecurePass123!');
    await user.click(screen.getByRole('button', { name: /register/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/invalid email format/i)).toBeInTheDocument();
    });
  });
  
  it('should validate password strength', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <RegisterForm />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/email/i), 'user@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'weak');
    await user.type(screen.getByLabelText(/confirm password/i), 'weak');
    await user.click(screen.getByRole('button', { name: /register/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument();
    });
  });
  
  it('should validate password confirmation matches', async () => {
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
  
  it('should submit form with valid data', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    
    render(
      <BrowserRouter>
        <RegisterForm onSubmit={onSubmit} />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/email/i), 'newuser@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'SecurePass123!');
    await user.type(screen.getByLabelText(/confirm password/i), 'SecurePass123!');
    await user.click(screen.getByRole('button', { name: /register/i }));
    
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith({
        username: 'newuser@example.com',
        password: 'SecurePass123!',
        confirmationPassword: 'SecurePass123!',
      });
    });
  });
  
  it('should display error for duplicate email', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockRejectedValue(new Error('Email already registered'));
    
    render(
      <BrowserRouter>
        <RegisterForm onSubmit={onSubmit} />
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
  
  it('should disable submit button during loading', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn(() => new Promise(resolve => setTimeout(resolve, 1000)));
    
    render(
      <BrowserRouter>
        <RegisterForm onSubmit={onSubmit} />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText(/email/i), 'user@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'SecurePass123!');
    await user.type(screen.getByLabelText(/confirm password/i), 'SecurePass123!');
    
    const submitButton = screen.getByRole('button', { name: /register/i });
    await user.click(submitButton);
    
    expect(submitButton).toBeDisabled();
  });
  
  it('should have link to login page', () => {
    render(
      <BrowserRouter>
        <RegisterForm />
      </BrowserRouter>
    );
    
    const loginLink = screen.getByRole('link', { name: /log in/i });
    expect(loginLink).toBeInTheDocument();
    expect(loginLink).toHaveAttribute('href', '/login');
  });
  
  it('should show password strength indicator', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <RegisterForm />
      </BrowserRouter>
    );
    
    const passwordInput = screen.getByLabelText(/^password$/i);
    
    // Weak password
    await user.type(passwordInput, 'weak');
    expect(screen.getByText(/weak/i)).toBeInTheDocument();
    
    // Strong password
    await user.clear(passwordInput);
    await user.type(passwordInput, 'SecurePass123!@#');
    expect(screen.getByText(/strong/i)).toBeInTheDocument();
  });
  
  it('should toggle password visibility', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <RegisterForm />
      </BrowserRouter>
    );
    
    const passwordInput = screen.getByLabelText(/^password$/i);
    expect(passwordInput).toHaveAttribute('type', 'password');
    
    const toggleButton = screen.getByRole('button', { name: /show password/i });
    await user.click(toggleButton);
    
    expect(passwordInput).toHaveAttribute('type', 'text');
  });
});
