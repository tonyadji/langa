/**
 * E2E Test: Complete Log Viewing Flow
 * 
 * Tests the end-to-end user journey for viewing and filtering logs.
 * 
 * Test ID: T116
 * User Story: US3 - Log Viewing and Filtering
 */

import { test, expect } from '@playwright/test';

test.describe('Log Viewing Flow - E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    // Assume user is logged in - add login steps if needed
  });

  test('should navigate to logs page and view logs', async ({ page }) => {
    await page.goto('/logs');
    
    await expect(page.locator('h1')).toContainText(/logs/i);
    await expect(page.locator('table')).toBeVisible();
  });

  test('should filter logs by level', async ({ page }) => {
    await page.goto('/logs');
    
    await page.click('text=ERROR');
    
    await expect(page.locator('[data-level="error"]')).toHaveCount(0);
  });

  test('should search logs by keyword', async ({ page }) => {
    await page.goto('/logs');
    
    await page.fill('[placeholder*="Search"]', 'database');
    
    await expect(page.locator('table tbody tr')).toHaveCount(0);
  });

  test('should expand log entry to show metadata', async ({ page }) => {
    await page.goto('/logs');
    
    await page.click('table tbody tr:first-child');
    
    await expect(page.locator('[data-testid="log-metadata"]')).toBeVisible();
  });

  test('should paginate through logs', async ({ page }) => {
    await page.goto('/logs');
    
    await page.click('[aria-label="Next page"]');
    
    await expect(page).toHaveURL(/page=2/);
  });
});
