/**
 * T230: Theme Context for Dark Mode
 * 
 * Provides theme state management with:
 * - Dark/light mode toggle
 * - System preference detection
 * - localStorage persistence
 * - Type-safe context
 */

import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';

type Theme = 'light' | 'dark' | 'system';
type ResolvedTheme = 'light' | 'dark';

interface ThemeContextType {
  theme: Theme;
  resolvedTheme: ResolvedTheme;
  setTheme: (theme: Theme) => void;
  toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

const STORAGE_KEY = 'langa-theme';

/**
 * Gets the user's system theme preference
 */
function getSystemTheme(): ResolvedTheme {
  if (typeof window === 'undefined') return 'light';
  
  return window.matchMedia('(prefers-color-scheme: dark)').matches
    ? 'dark'
    : 'light';
}

/**
 * Applies the theme to the document element
 */
function applyTheme(resolvedTheme: ResolvedTheme) {
  const root = window.document.documentElement;
  
  if (resolvedTheme === 'dark') {
    root.classList.add('dark');
  } else {
    root.classList.remove('dark');
  }
}

interface ThemeProviderProps {
  children: ReactNode;
  defaultTheme?: Theme;
}

export function ThemeProvider({ children, defaultTheme = 'system' }: ThemeProviderProps) {
  // Get initial theme from localStorage or use default
  const [theme, setThemeState] = useState<Theme>(() => {
    if (typeof window === 'undefined') return defaultTheme;

    const stored = localStorage.getItem(STORAGE_KEY) as Theme | null;
    return stored || defaultTheme;
  });

  // System preference, kept in sync with the OS setting
  const [systemTheme, setSystemTheme] = useState<ResolvedTheme>(getSystemTheme);

  // Derived: no state to keep in sync
  const resolvedTheme: ResolvedTheme = theme === 'system' ? systemTheme : theme;

  // Update theme when preference changes
  const setTheme = (newTheme: Theme) => {
    setThemeState(newTheme);
    localStorage.setItem(STORAGE_KEY, newTheme);
  };

  // Toggle between light and dark (skips system)
  const toggleTheme = () => {
    setTheme(resolvedTheme === 'dark' ? 'light' : 'dark');
  };

  // Apply the resolved theme to the document
  useEffect(() => {
    applyTheme(resolvedTheme);
  }, [resolvedTheme]);

  // Follow system theme changes
  useEffect(() => {
    if (typeof window === 'undefined') return;

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = () => setSystemTheme(getSystemTheme());

    // Modern browsers
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange);
      return () => mediaQuery.removeEventListener('change', handleChange);
    }
    // Legacy browsers
    else if (mediaQuery.addListener) {
      mediaQuery.addListener(handleChange);
      return () => mediaQuery.removeListener(handleChange);
    }
  }, []);

  return (
    <ThemeContext.Provider value={{ theme, resolvedTheme, setTheme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

/**
 * Hook to access theme context
 * @throws Error if used outside ThemeProvider
 */
// eslint-disable-next-line react-refresh/only-export-components -- the hook belongs with its provider
export function useTheme(): ThemeContextType {
  const context = useContext(ThemeContext);
  
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  
  return context;
}
