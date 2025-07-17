import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Include cookies in requests
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Try to refresh token
      try {
        const refreshResponse = await refreshAccessToken();
        if (refreshResponse.success) {
          // Retry the original request
          const originalRequest = error.config;
          originalRequest.headers.Authorization = `Bearer ${refreshResponse.data.accessToken}`;
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.removeItem('accessToken');
        window.location.href = '/';
      }
    }
    return Promise.reject(error);
  }
);

// Auth API calls
export const authService = {
  // Verify Microsoft ID token
  verifyMicrosoftToken: async (idToken) => {
    try {
      const response = await apiClient.post('/auth/microsoft/verify', {
        idToken: idToken,
      });

      if (response.data.success) {
        // Store access token
        localStorage.setItem('accessToken', response.data.data.accessToken);
        return response.data;
      }

      throw new Error(response.data.error?.message || 'Authentication failed');
    } catch (error) {
      console.error('Microsoft token verification failed:', error);
      throw error;
    }
  },

  // Refresh access token
  refreshAccessToken: async () => {
    try {
      const response = await apiClient.post('/auth/refresh');

      if (response.data.success) {
        localStorage.setItem('accessToken', response.data.data.accessToken);
        return response.data;
      }

      throw new Error(response.data.error?.message || 'Token refresh failed');
    } catch (error) {
      console.error('Token refresh failed:', error);
      throw error;
    }
  },

  // Logout
  logout: async () => {
    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem('accessToken');
    }
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    return !!localStorage.getItem('accessToken');
  },
};

// User API calls
export const userService = {
  // Get user profile
  getProfile: async () => {
    try {
      const response = await apiClient.get('/api/user/profile');
      return response.data;
    } catch (error) {
      console.error('Get profile failed:', error);
      throw error;
    }
  },

  // Update user profile
  updateProfile: async (updates) => {
    try {
      const response = await apiClient.put('/api/user/profile', updates);
      return response.data;
    } catch (error) {
      console.error('Update profile failed:', error);
      throw error;
    }
  },
};

// Health check
export const healthService = {
  check: async () => {
    try {
      const response = await apiClient.get('/auth/health');
      return response.data;
    } catch (error) {
      console.error('Health check failed:', error);
      throw error;
    }
  },
};

// Export refresh function for interceptor
const refreshAccessToken = authService.refreshAccessToken;

export default apiClient;