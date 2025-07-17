import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { parseTokenFromUrl } from '../utils/msalConfig';
import { authService } from '../services/apiService';

const Callback = () => {
  const navigate = useNavigate();
  const [status, setStatus] = useState('Processing authentication...');
  const [error, setError] = useState(null);

  useEffect(() => {
    const handleCallback = async () => {
      try {
        setStatus('Extracting authentication token...');

        // Parse token from URL fragment
        const tokenData = parseTokenFromUrl();

        if (tokenData.error) {
          throw new Error(`Microsoft authentication error: ${tokenData.errorDescription || tokenData.error}`);
        }

        if (!tokenData.idToken) {
          throw new Error('No ID token received from Microsoft. Please try logging in again.');
        }

        setStatus('Verifying token with backend...');

        // Send ID token to backend for verification
        const backendResponse = await authService.verifyMicrosoftToken(tokenData.idToken);

        if (backendResponse.success) {
          setStatus('Authentication successful! Redirecting to dashboard...');

          // Clear the URL fragment to remove tokens from browser history
          window.history.replaceState({}, document.title, window.location.pathname);

          // Small delay to show success message
          setTimeout(() => {
            navigate('/dashboard');
          }, 1500);
        } else {
          throw new Error(backendResponse.error?.message || 'Backend token verification failed');
        }

      } catch (error) {
        console.error('Authentication callback error:', error);
        setError(error.message || 'Authentication failed');
        setStatus('Authentication failed');

        // Clear URL fragment even on error
        window.history.replaceState({}, document.title, window.location.pathname);

        // Redirect to login after showing error
        setTimeout(() => {
          navigate('/');
        }, 3000);
      }
    };

    handleCallback();
  }, [navigate]);

  return (
    <div className="callback-container">
      <div className="callback-card">
        <div className="callback-content">
          {error ? (
            <>
              <div className="error-icon">⚠️</div>
              <h2>Authentication Failed</h2>
              <p className="error-message">{error}</p>
              <p>Redirecting to login page...</p>
            </>
          ) : (
            <>
              <div className="loading-spinner"></div>
              <h2>Authenticating</h2>
              <p>{status}</p>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Callback;