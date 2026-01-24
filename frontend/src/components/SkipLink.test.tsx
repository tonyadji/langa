/**
 * T225: SkipLink Tests
 */

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SkipLink } from './SkipLink';

describe('SkipLink', () => {
  it('renders with default text', () => {
    render(<SkipLink />);
    expect(screen.getByText('Skip to main content')).toBeInTheDocument();
  });

  it('renders with custom text', () => {
    render(<SkipLink>Skip to content</SkipLink>);
    expect(screen.getByText('Skip to content')).toBeInTheDocument();
  });

  it('links to default target id', () => {
    render(<SkipLink />);
    const link = screen.getByText('Skip to main content');
    expect(link).toHaveAttribute('href', '#main-content');
  });

  it('links to custom target id', () => {
    render(<SkipLink targetId="custom-target">Skip here</SkipLink>);
    const link = screen.getByText('Skip here');
    expect(link).toHaveAttribute('href', '#custom-target');
  });

  it('is an anchor element for keyboard navigation', () => {
    render(<SkipLink />);
    const link = screen.getByText('Skip to main content');
    expect(link.tagName).toBe('A');
  });

  it('has screen reader only class by default', () => {
    render(<SkipLink />);
    const link = screen.getByText('Skip to main content');
    expect(link).toHaveClass('sr-only');
  });

  it('becomes visible on focus with proper styling', () => {
    render(<SkipLink />);
    const link = screen.getByText('Skip to main content');
    
    // Should have focus classes for visibility
    expect(link.className).toContain('focus:not-sr-only');
    expect(link.className).toContain('focus:absolute');
    expect(link.className).toContain('focus:z-50');
  });
});
