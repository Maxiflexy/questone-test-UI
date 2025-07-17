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
      await authService.logout();
      navigate('/');
    } catch (error) {
      console.error('Logout error:', error);
      // Even if logout fails, redirect to login
      navigate('/');
    }
  };

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading-spinner"></div>
        <p>Loading profile...</p>
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
        <button onClick={handleLogout} className="logout-button">
          Logout
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
              <p className="profile-email">{user?.email}</p>
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
                <label>Email</label>
                <span>{user?.email || 'Not provided'}</span>
              </div>

              <div className="detail-item">
                <label>Given Name</label>
                <span>{user?.givenName || 'Not provided'}</span>
              </div>

              <div className="detail-item">
                <label>Family Name</label>
                <span>{user?.familyName || 'Not provided'}</span>
              </div>

              <div className="detail-item">
                <label>Job Title</label>
                <span>{user?.jobTitle || 'Not provided'}</span>
              </div>

              <div className="detail-item">
                <label>Department</label>
                <span>{user?.department || 'Not provided'}</span>
              </div>

              <div className="detail-item">
                <label>Microsoft ID</label>
                <span className="microsoft-id">{user?.microsoftId || 'Not provided'}</span>
              </div>

              <div className="detail-item">
                <label>Member Since</label>
                <span>
                  {user?.createdAt
                    ? new Date(user.createdAt).toLocaleDateString()
                    : 'Not provided'
                  }
                </span>
              </div>

              <div className="detail-item">
                <label>Last Login</label>
                <span>
                  {user?.lastLogin
                    ? new Date(user.lastLogin).toLocaleString()
                    : 'Not provided'
                  }
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="actions-card">
          <h3>Quick Actions</h3>
          <div className="action-buttons">
            <button className="action-button">
              View Activity
            </button>
            <button className="action-button">
              Account Settings
            </button>
            <button className="action-button">
              Security Settings
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;