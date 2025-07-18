import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: false, // Not using cookies for this implementation
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle unauthorized requests
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Clear token and redirect to login
      localStorage.removeItem('jwtToken');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

// Track active requests to prevent duplicates
const activeRequests = new Map();

// Auth API calls
export const authService = {
  // Exchange authorization code for JWT token (no PKCE for Web apps)
  exchangeCodeForToken: async (code) => {
    // Create a unique key for this request
    const requestKey = `exchange_${code}`;

    // Check if this request is already in progress
    if (activeRequests.has(requestKey)) {
      console.log('Code exchange already in progress, returning existing promise');
      return activeRequests.get(requestKey);
    }

    // Create the request promise
    const requestPromise = (async () => {
      try {
        console.log('Starting code exchange for:', code.substring(0, 10) + '...');

        const response = await apiClient.post('/auth/exchange', {
          code: code
        });

        if (response.data) {
          // Store JWT token
          localStorage.setItem('jwtToken', response.data);
          return {
            success: true,
            data: { token: response.data }
          };
        }

        throw new Error('No token received from backend');
      } catch (error) {
        console.error('Code exchange failed:', error);

        let errorMessage = 'Authentication failed';
        if (error.response?.data) {
          errorMessage = error.response.data;
        } else if (error.message) {
          errorMessage = error.message;
        }

        return {
          success: false,
          error: { message: errorMessage }
        };
      } finally {
        // Remove from active requests when done
        activeRequests.delete(requestKey);
      }
    })();

    // Store the promise to prevent duplicates
    activeRequests.set(requestKey, requestPromise);

    return requestPromise;
  },

  // Logout
  logout: async () => {
    try {
      localStorage.removeItem('jwtToken');
    } catch (error) {
      console.error('Logout error:', error);
    }
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    return !!localStorage.getItem('jwtToken');
  },
};

// User API calls
export const userService = {
  // Get user profile
  getProfile: async () => {
    try {
      const response = await apiClient.get('/profile');

      if (response.data) {
        return {
          success: true,
          data: response.data
        };
      }

      throw new Error('No profile data received');
    } catch (error) {
      console.error('Get profile failed:', error);

      let errorMessage = 'Failed to load profile';
      if (error.response?.data) {
        errorMessage = error.response.data;
      } else if (error.message) {
        errorMessage = error.message;
      }

      return {
        success: false,
        error: { message: errorMessage }
      };
    }
  },
};

// Health check
export const healthService = {
  check: async () => {
    try {
      const response = await apiClient.get('/health');
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Health check failed:', error);
      return {
        success: false,
        error: { message: error.message }
      };
    }
  },
};

export default apiClient;