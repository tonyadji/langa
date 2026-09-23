import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { MsalProvider } from '@azure/msal-react';
import '@/styles/index.css';
import App from './App';
import { initWebVitals } from '@/utils/webVitals';
import { ThemeProvider } from '@/contexts/ThemeContext';
import { initializeMsal, msalInstance } from '@/features/auth/msal';

// Initialize Web Vitals monitoring (T233)
initWebVitals();

// MSAL must process the sign-in redirect before the router renders
initializeMsal()
  .catch(error => console.error('Authentication initialization failed:', error))
  .finally(() => {
    createRoot(document.getElementById('root')!).render(
      <StrictMode>
        <MsalProvider instance={msalInstance}>
          <ThemeProvider>
            <App />
          </ThemeProvider>
        </MsalProvider>
      </StrictMode>
    );
  });
