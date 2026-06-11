import { useEffect, useState } from 'react';
import { isAuthenticated } from '../services/auth';

export function useAuth() {
  const [authenticated, setAuthenticated] = useState(() => isAuthenticated());

  useEffect(() => {
    const handleStorage = () => setAuthenticated(isAuthenticated());
    window.addEventListener('storage', handleStorage);
    return () => window.removeEventListener('storage', handleStorage);
  }, []);

  return { authenticated };
}
