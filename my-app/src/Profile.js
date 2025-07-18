import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Profile = () => {
  const [profile, setProfile] = useState(null);
  const backendUrl = process.env.REACT_APP_BACKEND_URL;

  useEffect(() => {
    const jwtToken = localStorage.getItem('jwtToken');
    if (jwtToken) {
      axios.get(`${backendUrl}/profile`, {
        headers: { Authorization: `Bearer ${jwtToken}` }
      })
        .then(response => setProfile(response.data))
        .catch(error => {
          console.error('Error fetching profile:', error);
          window.location.href = '/';
        });
    } else {
      window.location.href = '/';
    }
  }, []);

  if (!profile) return <div>Loading...</div>;

  return (
    <div>
      <h1>Profile</h1>
      <p><strong>Name:</strong> {profile.name}</p>
      <p><strong>Email:</strong> {profile.email}</p>
    </div>
  );
};

export default Profile;