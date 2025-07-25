import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';

// Create axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Enable cookies for refresh token handling
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

// Response interceptor to handle unauthorized requests
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Clear token and redirect to login
      localStorage.removeItem('accessToken');
      localStorage.removeItem('tokenType');
      localStorage.removeItem('expiresIn');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

// Track active requests to prevent duplicates
const activeRequests = new Map();

// Auth API calls
export const authService = {
  // Exchange authorization code for JWT token using new API
  exchangeCodeForToken: async (authCode) => {
    // Create a unique key for this request
    const requestKey = `exchange_${authCode}`;

    // Check if this request is already in progress
    if (activeRequests.has(requestKey)) {
      console.log('Code exchange already in progress, returning existing promise');
      return activeRequests.get(requestKey);
    }

    // Create the request promise
    const requestPromise = (async () => {
      try {
        console.log('Starting Microsoft token verification for:', authCode.substring(0, 10) + '...');

        const response = await apiClient.post('/api/v1/auth/microsoft/verify', {
          authCode: authCode
        });

        // ADDED: Enhanced console logging for verify endpoint response
        console.log('=== VERIFY ENDPOINT RESPONSE ===');
        console.log('Full Response:', response);
        console.log('Response Data:', response.data);
        console.log('Response Status:', response.status);
        console.log('Response Headers:', response.headers);

        if (response.data?.data) {
          console.log('Token Data:', {
            accessToken: response.data.data.accessToken ? `${response.data.data.accessToken.substring(0, 20)}...` : 'Not provided',
            tokenType: response.data.data.tokenType,
            expiresIn: response.data.data.expiresIn
          });

          // ADDED: Log user data from verify endpoint
          if (response.data.data.user) {
            console.log('User Data from Verify Endpoint:', response.data.data.user);
          }
        }
        console.log('=== END VERIFY ENDPOINT RESPONSE ===');

        if (response.data?.success && response.data?.data) {
          const { accessToken, expiresIn, tokenType } = response.data.data;

          // Store access token in localStorage
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('tokenType', tokenType);
          localStorage.setItem('expiresIn', expiresIn.toString());

          console.log('Tokens stored successfully in localStorage');

          return {
            success: true,
            data: response.data.data
          };
        }

        throw new Error('Invalid response format from backend');
      } catch (error) {
        console.error('Microsoft token verification failed:', error);

        let errorMessage = 'Authentication failed';
        if (error.response?.data?.error?.message) {
          errorMessage = error.response.data.error.message;
        } else if (error.response?.data?.message) {
          errorMessage = error.response.data.message;
        } else if (error.message) {
          errorMessage = error.message;
        }

        return {
          success: false,
          error: {
            code: error.response?.data?.error?.code || 'AUTH_ERROR',
            message: errorMessage
          }
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

  refreshToken: async () => {
    try {
      console.log('Refreshing access token...');

      // NEW: Added refresh token endpoint
      const response = await apiClient.post('/api/v1/auth/refresh');

      console.log('Refresh token response:', response.data);

      if (response.data?.success && response.data?.data) {
        const { accessToken, expiresIn, tokenType } = response.data.data;

        // Store new access token in localStorage
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('tokenType', tokenType);
        localStorage.setItem('expiresIn', expiresIn.toString());

        console.log('New tokens stored successfully');

        return {
          success: true,
          data: response.data.data
        };
      }

      throw new Error('Invalid refresh response format');
    } catch (error) {
      console.error('Token refresh failed:', error);

      let errorMessage = 'Token refresh failed';
      if (error.response?.data?.error?.message) {
        errorMessage = error.response.data.error.message;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      return {
        success: false,
        error: {
          code: error.response?.data?.error?.code || 'REFRESH_ERROR',
          message: errorMessage
        }
      };
    }
  },

  // Logout with backend call to clear cookies
  logout: async () => {
    try {
      console.log('Logging out...');

      await apiClient.post('/api/v1/auth/logout');

      console.log('Backend logout successful');
    } catch (error) {
      console.error('Backend logout error:', error);
      // Continue with local cleanup even if backend call fails
    } finally {
      // Always clear local storage
      localStorage.removeItem('accessToken');
      localStorage.removeItem('tokenType');
      localStorage.removeItem('expiresIn');
      console.log('Local tokens cleared');
    }
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    const token = localStorage.getItem('accessToken');

    if (!token) {
      return false;
    }

    // Basic check - in production you might want to validate JWT expiry
    return true;
  },
};

// User API calls
export const userService = {
  // Get user profile using new endpoint
  getProfile: async () => {
    try {
      console.log('Fetching user profile...');

      const response = await apiClient.get('/api/v1/auth/user/profile');

      console.log('Profile response:', response.data);

      if (response.data?.success && response.data?.data) {
        return {
          success: true,
          data: response.data.data
        };
      }

      throw new Error('Invalid profile response format');
    } catch (error) {
      console.error('Get profile failed:', error);

      let errorMessage = 'Failed to load profile';
      if (error.response?.data?.error?.message) {
        errorMessage = error.response.data.error.message;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      // Throw error with response details for proper handling
      const profileError = new Error(errorMessage);
      profileError.response = error.response;
      throw profileError;
    }
  },
};

// Health check
export const healthService = {
  check: async () => {
    try {
      const response = await apiClient.get('/api/v1/auth/health');
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