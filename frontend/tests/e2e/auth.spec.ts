/**
 * E2E Test: Complete Authentication Flow
 * 
 * Tests the full user journey: register → login → setup wizard → dashboard access
 * using Playwright for end-to-end browser testing.
 * 
 * ⚠️ This test MUST FAIL until all authentication components are implemented
 */

import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:5173';

test.describe('Authentication Flow - E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Clear any existing session
    await page.context().clearCookies();
    await page.goto(BASE_URL);
  });
  
  test('should complete full registration and login flow', async ({ page }) => {
    const timestamp = Date.now();
    const testEmail = `test-user-${timestamp}@example.com`;
    const testPassword = 'SecurePassword123!';
    
    // Step 1: Navigate to registration page
    await page.goto(`${BASE_URL}/register`);
    await expect(page).toHaveTitle(/register/i);
    
    // Step 2: Fill registration form
    await page.getByLabel(/email/i).fill(testEmail);
    await page.getByLabel(/^password$/i).fill(testPassword);
    await page.getByLabel(/confirm password/i).fill(testPassword);
    
    // Step 3: Submit registration
    await page.getByRole('button', { name: /register/i }).click();
    
    // Step 4: Verify redirect to login page
    await expect(page).toHaveURL(`${BASE_URL}/login`);
    await expect(page.getByText(/registered successfully/i)).toBeVisible();
    
    // Step 5: Login with new credentials
    await page.getByLabel(/username/i).fill(testEmail);
    await page.getByLabel(/password/i).fill(testPassword);
    await page.getByRole('button', { name: /log in/i }).click();
    
    // Step 6: Verify redirect to setup wizard (first-time user)
    await expect(page).toHaveURL(/\/setup/);
    await expect(page.getByText(/welcome/i)).toBeVisible();
    
    // Step 7: Complete setup wizard
    await page.getByRole('button', { name: /next/i }).click();
    await page.getByRole('button', { name: /next/i }).click();
    await page.getByRole('button', { name: /finish/i }).click();
    
    // Step 8: Verify redirect to dashboard
    await expect(page).toHaveURL(`${BASE_URL}/dashboard`);
    await expect(page.getByText(/dashboard/i)).toBeVisible();
  });
  
  test('should prevent access to protected routes when not authenticated', async ({ page }) => {
    // Try to access dashboard directly
    await page.goto(`${BASE_URL}/dashboard`);
    
    // Should redirect to login
    await expect(page).toHaveURL(`${BASE_URL}/login`);
    await expect(page.getByText(/log in/i)).toBeVisible();
  });
  
  test('should handle login with invalid credentials', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    
    // Enter invalid credentials
    await page.getByLabel(/username/i).fill('invaliduser');
    await page.getByLabel(/password/i).fill('wrongpassword');
    await page.getByRole('button', { name: /log in/i }).click();
    
    // Should show error message
    await expect(page.getByText(/invalid credentials/i)).toBeVisible();
    
    // Should remain on login page
    await expect(page).toHaveURL(`${BASE_URL}/login`);
  });
  
  test('should handle registration with duplicate email', async ({ page }) => {
    const existingEmail = 'existing@example.com';
    
    await page.goto(`${BASE_URL}/register`);
    
    // First registration
    await page.getByLabel(/email/i).fill(existingEmail);
    await page.getByLabel(/^password$/i).fill('SecurePass123!');
    await page.getByLabel(/confirm password/i).fill('SecurePass123!');
    await page.getByRole('button', { name: /register/i }).click();
    
    // Wait for success
    await expect(page).toHaveURL(`${BASE_URL}/login`);
    
    // Try to register again with same email
    await page.goto(`${BASE_URL}/register`);
    await page.getByLabel(/email/i).fill(existingEmail);
    await page.getByLabel(/^password$/i).fill('SecurePass123!');
    await page.getByLabel(/confirm password/i).fill('SecurePass123!');
    await page.getByRole('button', { name: /register/i }).click();
    
    // Should show error
    await expect(page.getByText(/already registered/i)).toBeVisible();
  });
  
  test('should logout and clear session', async ({ page }) => {
    // Login first
    await page.goto(`${BASE_URL}/login`);
    await page.getByLabel(/username/i).fill('testuser');
    await page.getByLabel(/password/i).fill('password');
    await page.getByRole('button', { name: /log in/i }).click();
    
    // Verify logged in
    await expect(page).toHaveURL(/\/dashboard|\/setup/);
    
    // Logout
    await page.getByRole('button', { name: /logout/i }).click();
    
    // Should redirect to login
    await expect(page).toHaveURL(`${BASE_URL}/login`);
    
    // Try to access protected route again
    await page.goto(`${BASE_URL}/dashboard`);
    await expect(page).toHaveURL(`${BASE_URL}/login`);
  });
  
  test('should maintain authentication across page refreshes', async ({ page }) => {
    // Login
    await page.goto(`${BASE_URL}/login`);
    await page.getByLabel(/username/i).fill('testuser');
    await page.getByLabel(/password/i).fill('password');
    await page.getByRole('button', { name: /log in/i }).click();
    
    // Navigate to dashboard
    await expect(page).toHaveURL(/\/dashboard|\/setup/);
    
    // Refresh page
    await page.reload();
    
    // Should still be authenticated
    await expect(page).toHaveURL(/\/dashboard|\/setup/);
    expect(await page.getByText(/logout/i).isVisible()).toBeTruthy();
  });
  
  test('should handle token expiration and refresh', async ({ page }) => {
    // This test would require backend support for short-lived tokens
    // For now, we'll test the concept
    
    await page.goto(`${BASE_URL}/login`);
    await page.getByLabel(/email/i).fill('user@example.com');
    await page.getByLabel(/password/i).fill('password');
    await page.getByRole('button', { name: /log in/i }).click();
    
    await expect(page).toHaveURL(/\/dashboard|\/setup/);
    
    // Simulate token expiration by clearing access token but keeping refresh token
    await page.evaluate(() => {
      localStorage.setItem('accessToken', 'expired-token');
      // Keep refresh token intact
    });
    
    // Make an API request that requires authentication
    await page.goto(`${BASE_URL}/applications`);
    
    // Should automatically refresh token and load the page
    await expect(page.getByText(/applications/i)).toBeVisible();
  });
  
  test('should validate password confirmation', async ({ page }) => {
    await page.goto(`${BASE_URL}/register`);
    
    // Enter mismatched passwords
    await page.getByLabel(/email/i).fill('user@example.com');
    await page.getByLabel(/^password$/i).fill('SecurePass123!');
    await page.getByLabel(/confirm password/i).fill('DifferentPass456!');
    await page.getByRole('button', { name: /register/i }).click();
    
    // Should show validation error
    await expect(page.getByText(/passwords do not match/i)).toBeVisible();
  });
  
  test('should skip setup wizard if desired', async ({ page }) => {
    // Login
    await page.goto(`${BASE_URL}/login`);
    await page.getByLabel(/email/i).fill('newuser@example.com');
    await page.getByLabel(/password/i).fill('password');
    await page.getByRole('button', { name: /log in/i }).click();
    
    // On setup wizard
    await expect(page).toHaveURL(/\/setup/);
    
    // Skip wizard
    const skipButton = page.getByRole('button', { name: /skip/i });
    if (await skipButton.isVisible()) {
      await skipButton.click();
      
      // Should go to dashboard
      await expect(page).toHaveURL(`${BASE_URL}/dashboard`);
    }
  });
});
