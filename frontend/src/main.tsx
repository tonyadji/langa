import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import '@/styles/index.css';
import App from './App';
import { initWebVitals } from '@/utils/webVitals';
import { ThemeProvider } from '@/contexts/ThemeContext';

// Initialize Web Vitals monitoring (T233)
initWebVitals();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider>
      <App />
    </ThemeProvider>
  </StrictMode>
);
