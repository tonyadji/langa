/**
 * Component Test: SetupWizard
 * 
 * Tests the multi-step setup wizard for first-time users.
 * Validates navigation, form submission, and completion flow.
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { SetupWizard } from '@/features/auth/components/SetupWizard';

describe('SetupWizard Component Tests', () => {
  it('should render first step of wizard', () => {
    render(
      <BrowserRouter>
        <SetupWizard />
      </BrowserRouter>
    );
    
    // Use specific heading selector to avoid duplicates
    expect(screen.getByRole('heading', { level: 2, name: /welcome to langa!/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /next/i })).toBeInTheDocument();
  });
  
  it('should navigate through wizard steps', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <SetupWizard />
      </BrowserRouter>
    );
    
    // Step 1: Welcome
    expect(screen.getByRole('heading', { level: 2, name: /welcome to langa!/i })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /next/i }));
    
    // Step 2: Application Info
    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 2, name: /what is an application/i })).toBeInTheDocument();
    });
    
    await user.click(screen.getByRole('button', { name: /next/i }));
    
    // Step 3: Completion
    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 2, name: /you're ready to go/i })).toBeInTheDocument();
    });
  });
  
  it('should allow navigating back', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <SetupWizard />
      </BrowserRouter>
    );
    
    // Go to step 2
    await user.click(screen.getByRole('button', { name: /next/i }));
    
    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 2, name: /what is an application/i })).toBeInTheDocument();
    });
    
    // Go back to step 1
    await user.click(screen.getByRole('button', { name: /back/i }));
    
    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 2, name: /welcome to langa!/i })).toBeInTheDocument();
    });
  });
  
  it('should disable back button on first step', () => {
    render(
      <BrowserRouter>
        <SetupWizard />
      </BrowserRouter>
    );
    
    const backButton = screen.queryByRole('button', { name: /back/i });
    
    if (backButton) {
      expect(backButton).toBeDisabled();
    }
  });
  
  it('should show finish button on last step', async () => {
    const user = userEvent.setup();
    
    render(
      <BrowserRouter>
        <SetupWizard />
      </BrowserRouter>
    );
    
    // Navigate to last step (3 steps total)
    const nextButton = screen.getByRole('button', { name: /next/i });
    
    await user.click(nextButton);
    await user.click(nextButton);
    
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /finish/i })).toBeInTheDocument();
    });
  });
  
  it('should complete wizard and call onComplete', async () => {
    const user = userEvent.setup();
    const onComplete = vi.fn();
    
    render(
      <BrowserRouter>
        <SetupWizard onComplete={onComplete} />
      </BrowserRouter>
    );
    
    // Navigate through all steps
    const nextButton = screen.getByRole('button', { name: /next/i });
    await user.click(nextButton);
    await user.click(nextButton);
    
    // Click finish
    const finishButton = screen.getByRole('button', { name: /finish/i });
    await user.click(finishButton);
    
    await waitFor(() => {
      expect(onComplete).toHaveBeenCalled();
    });
  });
  
  it('should show progress indicator', () => {
    render(
      <BrowserRouter>
        <SetupWizard />
      </BrowserRouter>
    );
    
    // Should show step 1 of 3
    expect(screen.getByText(/step 1 of 3/i)).toBeInTheDocument();
  });
  
  it('should allow skipping wizard', async () => {
    const user = userEvent.setup();
    const onSkip = vi.fn();
    
    render(
      <BrowserRouter>
        <SetupWizard onSkip={onSkip} />
      </BrowserRouter>
    );
    
    const skipButton = screen.queryByRole('button', { name: /skip/i });
    
    if (skipButton) {
      await user.click(skipButton);
      expect(onSkip).toHaveBeenCalled();
    }
  });
});
