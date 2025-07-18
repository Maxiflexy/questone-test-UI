import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

const AuthCallback = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const backendUrl = process.env.REACT_APP_BACKEND_URL;

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const code = params.get('code');

    if (code) {
      axios.post(`${backendUrl}/auth/exchange`, { code })
        .then(response => {
          localStorage.setItem('jwtToken', response.data);
          navigate('/profile');
        })
        .catch(error => {
          console.error('Error exchanging code:', error);
          navigate('/');
        });
    } else {
      console.error('Authorization code not found');
      navigate('/');
    }
  }, [location, navigate]);

  return <div>Loading...</div>;
};

export default AuthCallback;