import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '@/features/auth/context/AuthContext';
import { useTokenRefresh } from '@/features/auth/hooks/useTokenRefresh';
import { TokenDebugger } from '@/components/dev/TokenDebugger';
import { AppRouter } from '@/router';
import { ErrorBoundary } from '@/components/ErrorBoundary';

function AppContent() {
  // Enable automatic token refresh (proactive + reactive)
  useTokenRefresh();
  
  return (
    <>
      <AppRouter />
      <TokenDebugger />
    </>
  );
}

function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
          <AppContent />
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  );
}

export default App;

