import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { parseCodeFromUrl, clearCodeVerifier } from '../utils/msalConfig';
import { authService } from '../services/apiService';

const Callback = () => {
  const navigate = useNavigate();
  const [status, setStatus] = useState('Processing authentication...');
  const [error, setError] = useState(null);
  const hasProcessed = useRef(false); // Prevent double execution

  useEffect(() => {
    const handleCallback = async () => {
      // Prevent double execution
      if (hasProcessed.current) {
        console.log('Callback already processed, skipping...');
        return;
      }
      hasProcessed.current = true;

      try {
        setStatus('Extracting authorization code...');

        // Parse authorization code from URL query parameters
        const codeData = parseCodeFromUrl();

        if (codeData.error) {
          throw new Error(`Microsoft authentication error: ${codeData.errorDescription || codeData.error}`);
        }

        if (!codeData.code) {
          throw new Error('No authorization code received from Microsoft. Please try logging in again.');
        }

        console.log('Authorization code received:', codeData.code.substring(0, 10) + '...');
        setStatus('Verifying Microsoft token with backend...');

        // Exchange authorization code for JWT token using new API
        const backendResponse = await authService.exchangeCodeForToken(codeData.code);

        console.log('Backend verification response:', backendResponse);

        if (backendResponse.success) {
          setStatus('Authentication successful! Redirecting to dashboard...');
          console.log('Authentication successful, tokens stored');

          // Clear URL query parameters
          clearCodeVerifier();
          window.history.replaceState({}, document.title, window.location.pathname);

          // Small delay to show success message
          setTimeout(() => {
            navigate('/dashboard');
          }, 1500);
        } else {
          const errorMessage = backendResponse.error?.message || 'Backend token verification failed';
          const errorCode = backendResponse.error?.code || 'UNKNOWN_ERROR';

          console.error('Backend verification failed:', errorCode, errorMessage);
          throw new Error(`${errorCode}: ${errorMessage}`);
        }

      } catch (error) {
        console.error('Authentication callback error:', error);
        setError(error.message || 'Authentication failed');
        setStatus('Authentication failed');

        // Clear URL query parameters even on error
        clearCodeVerifier();
        window.history.replaceState({}, document.title, window.location.pathname);

        // Redirect to login after showing error
        setTimeout(() => {
          navigate('/');
        }, 5000);
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
              <p style={{ marginBottom: '16px', color: '#dc3545' }}>{error}</p>
              <p style={{ fontSize: '14px', color: '#666' }}>
                Redirecting to login page in a few seconds...
              </p>
              <button
                onClick={() => navigate('/')}
                className="button-secondary"
                style={{ marginTop: '16px' }}
              >
                Return to Login
              </button>
            </>
          ) : (
            <>
              <div className="loading-spinner"></div>
              <h2>Authenticating</h2>
              <p>{status}</p>
              <p style={{ fontSize: '12px', color: '#888', marginTop: '16px' }}>
                Please wait while we securely authenticate you with Microsoft...
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Callback;