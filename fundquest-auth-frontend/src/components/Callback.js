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

        setStatus('Exchanging code for token...');

        // Exchange authorization code for JWT token (no PKCE for Web apps)
        const backendResponse = await authService.exchangeCodeForToken(codeData.code);

        if (backendResponse.success) {
          setStatus('Authentication successful! Redirecting to dashboard...');

          // Clear URL query parameters
          clearCodeVerifier();
          window.history.replaceState({}, document.title, window.location.pathname);

          // Small delay to show success message
          setTimeout(() => {
            navigate('/dashboard');
          }, 1500);
        } else {
          throw new Error(backendResponse.error?.message || 'Backend token exchange failed');
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
        }, 3000);
      }
    };

    handleCallback();
  }, [navigate]); // Add navigate to dependencies

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          {error ? (
            <>
              <div className="text-red-500 text-6xl mb-4">⚠️</div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Authentication Failed</h2>
              <p className="text-gray-600 mb-4">{error}</p>
              <p className="text-sm text-gray-500">Redirecting to login page...</p>
            </>
          ) : (
            <>
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Authenticating</h2>
              <p className="text-gray-600">{status}</p>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Callback;