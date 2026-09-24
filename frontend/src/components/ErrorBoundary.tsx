/**
 * T224: ErrorBoundary Component
 * 
 * React Error Boundary for graceful error handling across the application.
 * Catches JavaScript errors anywhere in the component tree and displays a fallback UI.
 * 
 * Features:
 * - Catches errors in child components
 * - Displays user-friendly error message
 * - Provides error details in development mode
 * - Allows error recovery with page reload
 * - Logs errors to console for debugging
 */

import { Component, type ReactNode } from 'react';
import { AlertTriangle } from 'lucide-react';

interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: React.ErrorInfo | null;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo): void {
    // Log error to console for debugging
    console.error('ErrorBoundary caught an error:', error, errorInfo);

    // Update state with error info
    this.setState({
      error,
      errorInfo,
    });

    // In production, you might want to send error to monitoring service
    // logErrorToService(error, errorInfo);
  }

  handleReset = (): void => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  handleReload = (): void => {
    window.location.reload();
  };

  render(): ReactNode {
    if (this.state.hasError) {
      // Custom fallback UI from props
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Default fallback UI
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
          <div className="max-w-md w-full">
            <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-6 border border-red-200 dark:border-red-800">
              <div className="flex items-center gap-3 mb-4">
                <div className="flex-shrink-0">
                  <AlertTriangle className="w-8 h-8 text-red-600 dark:text-red-400" />
                </div>
                <div>
                  <h1 className="text-xl font-semibold text-gray-900 dark:text-gray-100">
                    Something went wrong
                  </h1>
                  <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">
                    We're sorry for the inconvenience
                  </p>
                </div>
              </div>

              <div className="bg-red-50 dark:bg-red-900/20 rounded-md p-4 mb-4">
                <p className="text-sm text-red-800 dark:text-red-400">
                  An unexpected error occurred. Please try refreshing the page or contact support if the problem persists.
                </p>
              </div>

              {/* Show error details in development mode */}
              {import.meta.env.DEV && this.state.error && (
                <details className="mb-4">
                  <summary className="text-sm font-medium text-gray-700 cursor-pointer hover:text-gray-900">
                    Error Details (Development Only)
                  </summary>
                  <div className="mt-2 p-3 bg-gray-100 rounded text-xs font-mono text-gray-800 overflow-auto max-h-48">
                    <div className="mb-2">
                      <strong>Error:</strong> {this.state.error.message}
                    </div>
                    {this.state.error.stack && (
                      <div>
                        <strong>Stack:</strong>
                        <pre className="whitespace-pre-wrap mt-1">
                          {this.state.error.stack}
                        </pre>
                      </div>
                    )}
                    {this.state.errorInfo && (
                      <div className="mt-2">
                        <strong>Component Stack:</strong>
                        <pre className="whitespace-pre-wrap mt-1">
                          {this.state.errorInfo.componentStack}
                        </pre>
                      </div>
                    )}
                  </div>
                </details>
              )}

              <div className="flex gap-3">
                <button
                  onClick={this.handleReload}
                  className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors font-medium"
                >
                  Reload Page
                </button>
                <button
                  onClick={this.handleReset}
                  className="flex-1 px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 transition-colors font-medium"
                >
                  Try Again
                </button>
              </div>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
