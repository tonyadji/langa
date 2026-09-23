import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import '@/styles/index.css';
import App from './App';
import { initWebVitals } from '@/utils/webVitals';
import { ThemeProvider } from '@/contexts/ThemeContext';
import { authClient } from '@/features/auth/providers';

// Initialize Web Vitals monitoring (T233)
initWebVitals();

// The identity provider must process the sign-in redirect before the router renders
authClient
  .initialize()
  .catch(error => console.error('Authentication initialization failed:', error))
  .finally(() => {
    createRoot(document.getElementById('root')!).render(
      <StrictMode>
        <ThemeProvider>
          <App />
        </ThemeProvider>
      </StrictMode>
    );
  });
