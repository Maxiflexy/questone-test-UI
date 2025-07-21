import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { userService, authService } from '../services/apiService';

const Dashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        setLoading(true);
        setError(null);

        // Check if user is authenticated
        if (!authService.isAuthenticated()) {
          navigate('/');
          return;
        }

        // Load user profile
        const response = await userService.getProfile();

        if (response.success) {
          setUser(response.data);
          console.log('User profile loaded:', response.data);
        } else {
          throw new Error(response.error?.message || 'Failed to load profile');
        }
      } catch (error) {
        console.error('Failed to load user profile:', error);
        setError(error.message || 'Failed to load profile');

        // If authentication error, redirect to login
        if (error.response?.status === 401) {
          navigate('/');
        }
      } finally {
        setLoading(false);
      }
    };

    loadUserProfile();
  }, [navigate]);

  const handleLogout = async () => {
    try {
      setLoading(true);
      await authService.logout();
      navigate('/');
    } catch (error) {
      console.error('Logout error:', error);
      // Even if logout fails, redirect to login
      navigate('/');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Not available';

    try {
      const date = new Date(dateString);
      return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        timeZoneName: 'short'
      });
    } catch (error) {
      return 'Invalid date';
    }
  };

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="callback-card">
          <div className="callback-content">
            <div className="loading-spinner"></div>
            <h2>Loading Profile</h2>
            <p>Please wait while we load your profile...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-container">
        <div className="error-card">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={() => navigate('/')} className="button-secondary">
            Back to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Welcome to FundQuest</h1>
        <button onClick={handleLogout} className="logout-button" disabled={loading}>
          {loading ? 'Logging out...' : 'Logout'}
        </button>
      </div>

      <div className="dashboard-content">
        <div className="profile-card">
          <div className="profile-header">
            <div className="profile-avatar">
              {user?.name?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div className="profile-info">
              <h2>{user?.name || 'Unknown User'}</h2>
              <p className="profile-email">{user?.email || 'No email provided'}</p>
            </div>
          </div>

          <div className="profile-details">
            <h3>Profile Information</h3>
            <div className="detail-grid">
              <div className="detail-item">
                <label>Full Name</label>
                <span>{user?.name || 'Not provided'}</span>
              </div>

              <div className="detail-item">
                <label>Email Address</label>
                <span>{user?.email || 'Not provided'}</span>
              </div>

              <div className="detail-item">
                <label>Microsoft ID</label>
                <span className="microsoft-id">{user?.microsoftId || 'Not provided'}</span>
              </div>

              <div className="detail-item">
                <label>Authentication Status</label>
                <span style={{ color: '#28a745', fontWeight: 'bold' }}>
                  ✓ Authenticated
                </span>
              </div>

              <div className="detail-item">
                <label>Account Created</label>
                <span>{formatDate(user?.createdAt)}</span>
              </div>

              <div className="detail-item">
                <label>Last Login</label>
                <span>{formatDate(user?.lastLogin)}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="actions-card">
          <h3>Account Information</h3>
          <div className="action-buttons">
            <div className="action-button" style={{ cursor: 'default' }}>
              <strong>Microsoft ID:</strong><br />
              <span style={{ fontSize: '12px', fontFamily: 'monospace', wordBreak: 'break-all' }}>
                {user?.microsoftId || 'Not available'}
              </span>
            </div>

            <div className="action-button" style={{ cursor: 'default' }}>
              <strong>Login Status:</strong><br />
              <span style={{ color: '#28a745' }}>Active Session</span>
            </div>

            <div className="action-button" style={{ cursor: 'default' }}>
              <strong>Last Activity:</strong><br />
              <span style={{ fontSize: '13px' }}>
                {formatDate(user?.lastLogin)}
              </span>
            </div>

            <button
              className="action-button"
              onClick={() => window.location.reload()}
              style={{ cursor: 'pointer' }}
            >
              Refresh Profile
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;