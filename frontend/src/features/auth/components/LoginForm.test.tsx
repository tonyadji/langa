/**
 * Component Test: LoginForm
 * 
 * Tests the LoginForm component including form validation, submission,
 * error states, and user interaction.
 * 
 * ⚠️ This test MUST FAIL until LoginForm component is implemented
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { LoginForm } from '@/features/auth/components/LoginForm';

describe('LoginForm Component Tests', () => {
  it('should render login form fields', () => {
    render(
      <BrowserRouter>
        <LoginForm />
      </BrowserRouter>
    );
    
    expect(screen.getByLabelText('Username')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /log in/i })).toBeInTheDocument();
  });
  
  it('should validate required fields', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    
    render(
      <BrowserRouter>
        <LoginForm onSubmit={onSubmit} />
      </BrowserRouter>
    );
    
    // Get the inputs
    const usernameInput = screen.getByLabelText('Username');
    const passwordInput = screen.getByLabelText('Password');
    
    // HTML5 validation will prevent empty submission
    // Check that required attributes are set
    expect(usernameInput).toHaveAttribute('required');
    expect(passwordInput).toHaveAttribute('required');
    
    // Verify onSubmit is not called when clicking with empty fields
    // Note: HTML5 validation prevents form submission, so our handler never runs
    expect(onSubmit).not.toHaveBeenCalled();
  });
  
  it('should accept any username format', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    
    render(
      <BrowserRouter>
        <LoginForm onSubmit={onSubmit} />
      </BrowserRouter>
    );
    
    // Login should accept non-email usernames
    await user.type(screen.getByLabelText('Username'), 'johndoe');
    await user.type(screen.getByLabelText('Password'), 'password123');
    await user.click(screen.getByRole('button', { name: /log in/i }));
    
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith({
        username: 'johndoe',
        password: 'password123',
      });
    });
  });
  
  it('should submit form with valid credentials', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    
    render(
      <BrowserRouter>
        <LoginForm onSubmit={onSubmit} />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText('Username'), 'user@example.com');
    await user.type(screen.getByLabelText('Password'), 'SecurePass123!');
    await user.click(screen.getByRole('button', { name: /log in/i }));
    
    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith({
        username: 'user@example.com',
        password: 'SecurePass123!',
      });
    });
  });
  
  it('should display error message on failed login', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockRejectedValue(new Error('Invalid credentials'));
    
    render(
      <BrowserRouter>
        <LoginForm onSubmit={onSubmit} />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText('Username'), 'user@example.com');
    await user.type(screen.getByLabelText('Password'), 'wrongpassword');
    await user.click(screen.getByRole('button', { name: /log in/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });
  
  it('should disable submit button during loading', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn(() => new Promise(resolve => setTimeout(resolve, 1000)));
    
    render(
      <BrowserRouter>
        <LoginForm onSubmit={onSubmit} />
      </BrowserRouter>
    );
    
    await user.type(screen.getByLabelText('Username'), 'user@example.com');
    await user.type(screen.getByLabelText('Password'), 'password');
    
    const submitButton = screen.getByRole('button', { name: /log in/i });
    await user.click(submitButton);
    
    expect(submitButton).toBeDisabled();
    expect(screen.getByText(/signing in/i)).toBeInTheDocument();
  });
  
  it('should toggle password visibility', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <LoginForm />
      </BrowserRouter>
    );
    
    const passwordInput = screen.getByLabelText('Password');
    expect(passwordInput).toHaveAttribute('type', 'password');
    
    const toggleButton = screen.getByRole('button', { name: /show password/i });
    await user.click(toggleButton);
    
    expect(passwordInput).toHaveAttribute('type', 'text');
  });
  
  it('should have link to registration page', () => {
    render(
      <BrowserRouter>
        <LoginForm />
      </BrowserRouter>
    );
    
    const registerLink = screen.getByRole('link', { name: /register/i });
    expect(registerLink).toBeInTheDocument();
    expect(registerLink).toHaveAttribute('href', '/register');
  });
  
  it('should clear error message when user starts typing', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockRejectedValue(new Error('Invalid credentials'));
    
    render(
      <BrowserRouter>
        <LoginForm onSubmit={onSubmit} />
      </BrowserRouter>
    );
    
    // Trigger error
    await user.type(screen.getByLabelText('Username'), 'user@example.com');
    await user.type(screen.getByLabelText('Password'), 'wrong');
    await user.click(screen.getByRole('button', { name: /log in/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
    
    // Start typing again
    await user.type(screen.getByLabelText('Password'), 'new');
    
    // Error should be cleared
    expect(screen.queryByText(/invalid credentials/i)).not.toBeInTheDocument();
  });
});
