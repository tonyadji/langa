import { expect, afterEach, vi } from 'vitest';
import { cleanup } from '@testing-library/react';
import * as matchers from '@testing-library/jest-dom/matchers';

expect.extend(matchers);

// Mock window.matchMedia for dark mode tests
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(), // deprecated
    removeListener: vi.fn(), // deprecated
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

// Mock the identity provider: tests run signed out (no Authorization header is sent)
vi.mock('@/features/auth/providers', () => ({
  authClient: {
    name: 'test',
    initialize: async () => {},
    isAuthenticated: () => false,
    login: async () => {},
    register: async () => {},
    logout: async () => {},
    getAccessToken: async () => null,
    subscribe: () => () => {},
  },
}));

afterEach(() => {
  cleanup();
  localStorage.clear();
  document.documentElement.classList.remove('dark');
});

// Polyfill ProgressEvent for MSW
if (typeof global.ProgressEvent === 'undefined') {
  class ProgressEvent extends Event {
    lengthComputable: boolean;
    loaded: number;
    total: number;

    constructor(type: string, eventInitDict?: ProgressEventInit) {
      super(type, eventInitDict);
      this.lengthComputable = eventInitDict?.lengthComputable || false;
      this.loaded = eventInitDict?.loaded || 0;
      this.total = eventInitDict?.total || 0;
    }
  }
  global.ProgressEvent = ProgressEvent as unknown as typeof global.ProgressEvent;
}
