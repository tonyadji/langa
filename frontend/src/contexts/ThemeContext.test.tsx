/**
 * T230: Tests for ThemeContext
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { ThemeProvider, useTheme } from './ThemeContext';

describe('ThemeContext', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark');
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should initialize with system theme by default', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    expect(result.current.theme).toBe('system');
    expect(['light', 'dark']).toContain(result.current.resolvedTheme);
  });

  it('should initialize with custom default theme', () => {
    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <ThemeProvider defaultTheme="dark">{children}</ThemeProvider>
    );

    const { result } = renderHook(() => useTheme(), { wrapper });

    expect(result.current.theme).toBe('dark');
    expect(result.current.resolvedTheme).toBe('dark');
  });

  it('should set theme to light', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    act(() => {
      result.current.setTheme('light');
    });

    expect(result.current.theme).toBe('light');
    expect(result.current.resolvedTheme).toBe('light');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('should set theme to dark', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    act(() => {
      result.current.setTheme('dark');
    });

    expect(result.current.theme).toBe('dark');
    expect(result.current.resolvedTheme).toBe('dark');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('should toggle theme from light to dark', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    act(() => {
      result.current.setTheme('light');
    });

    act(() => {
      result.current.toggleTheme();
    });

    expect(result.current.resolvedTheme).toBe('dark');
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('should toggle theme from dark to light', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    act(() => {
      result.current.setTheme('dark');
    });

    act(() => {
      result.current.toggleTheme();
    });

    expect(result.current.resolvedTheme).toBe('light');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('should persist theme to localStorage', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    act(() => {
      result.current.setTheme('dark');
    });

    expect(localStorage.getItem('langa-theme')).toBe('dark');
  });

  it('should restore theme from localStorage', () => {
    localStorage.setItem('langa-theme', 'dark');

    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    expect(result.current.theme).toBe('dark');
    expect(result.current.resolvedTheme).toBe('dark');
  });

  it('should throw error when useTheme is used outside provider', () => {
    // Suppress console.error for this test
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});

    expect(() => {
      renderHook(() => useTheme());
    }).toThrow('useTheme must be used within a ThemeProvider');

    consoleError.mockRestore();
  });

  it('should apply dark class to document element when theme is dark', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    act(() => {
      result.current.setTheme('dark');
    });

    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('should remove dark class from document element when theme is light', () => {
    const { result } = renderHook(() => useTheme(), {
      wrapper: ThemeProvider,
    });

    // Set to dark first
    act(() => {
      result.current.setTheme('dark');
    });

    // Then set to light
    act(() => {
      result.current.setTheme('light');
    });

    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });
});
