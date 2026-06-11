import axios from 'axios';
import { getAuthToken, logout } from './auth';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

console.debug('Frontend API baseURL:', api.defaults.baseURL, 'VITE_API_URL:', import.meta.env.VITE_API_URL);

api.interceptors.request.use((config) => {
  const token = getAuthToken();
  console.log('API Request Debug:', {
    url: config.url,
    hasToken: !!token,
    tokenLength: token ? token.length : 0,
    tokenPreview: token ? token.substring(0, 20) + '...' : 'NO TOKEN',
  });
  
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
    console.log('Authorization header SET:', config.headers.Authorization.substring(0, 30) + '...');
  } else {
    console.warn('NO TOKEN - Authorization header NOT set', { hasToken: !!token, hasHeaders: !!config.headers });
  }
  
  const requestUrl = `${config.baseURL}${config.url}`;
  console.debug('Frontend API request:', requestUrl, config.method, config.data);
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('Axios error:', {
      message: error?.message,
      response: error?.response,
      request: error?.request,
      configUrl: error?.config?.url,
      configBaseURL: error?.config?.baseURL,
    });
    if (error?.response?.status === 401) {
      logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
