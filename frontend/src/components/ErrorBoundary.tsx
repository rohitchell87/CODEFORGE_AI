import { ReactNode } from 'react';
import { Component } from 'react';

interface ErrorBoundaryState {
  hasError: boolean;
  error?: Error;
}

interface ErrorBoundaryProps {
  children: ReactNode;
}

class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: unknown) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center px-4 py-10 text-center text-white">
          <div className="rounded-2xl border border-white/10 bg-black p-10">
            <h1 className="text-3xl font-semibold text-white">Something went wrong</h1>
            <p className="mt-4 text-sm text-textSecondary">The page encountered an unexpected error. Please refresh the page or try again.</p>
            {this.state.error && <pre className="mt-4 max-h-60 overflow-auto rounded-2xl bg-black/90 p-4 text-left text-xs text-textSecondary">{this.state.error.message}</pre>}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
