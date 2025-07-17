import React, { useState } from 'react';
import { buildAuthUrl } from '../utils/msalConfig';

const Login = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleLogin = () => {
    try {
      setIsLoading(true);
      setError(null);

      // Build Microsoft OAuth URL
      const authUrl = buildAuthUrl();

      // Redirect to Microsoft
      window.location.href = authUrl;
    } catch (error) {
      console.error('Login failed:', error);
      setError('Login failed. Please check your configuration.');
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>FundQuest Auth Service</h1>
          <p>Sign in with your Microsoft account</p>
        </div>

        <div className="login-content">
          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <button
            onClick={handleLogin}
            disabled={isLoading}
            className="login-button"
          >
            {isLoading ? (
              <span>Redirecting to Microsoft...</span>
            ) : (
              <>
                <svg className="microsoft-icon" viewBox="0 0 48 48">
                  <path fill="#ff5722" d="M6 6h16v16H6z"/>
                  <path fill="#4caf50" d="M26 6h16v16H26z"/>
                  <path fill="#03a9f4" d="M6 26h16v16H6z"/>
                  <path fill="#ffeb3b" d="M26 26h16v16H26z"/>
                </svg>
                Sign in with Microsoft
              </>
            )}
          </button>
        </div>

        <div className="login-footer">
          <p>Secure authentication powered by Microsoft Azure</p>
        </div>
      </div>
    </div>
  );
};

export default Login;