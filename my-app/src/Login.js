import React from 'react';
import { useNavigate } from 'react-router-dom';

const Login = () => {
  const navigate = useNavigate();
  const tenantId = process.env.REACT_APP_AZURE_TENANT_ID;
  const clientId = process.env.REACT_APP_AZURE_CLIENT_ID;
  const redirectUri = process.env.REACT_APP_REDIRECT_URI;

  const login = () => {
    const authUrl = `https://login.microsoftonline.com/${tenantId}/oauth2/v2.0/authorize?client_id=${clientId}&response_type=code&redirect_uri=${redirectUri}&scope=openid%20profile%20email&response_mode=query`;
    window.location.href = authUrl;
  };

  return (
    <div>
      <h1>Login</h1>
      <button onClick={login}>Login with Azure AD</button>
    </div>
  );
};

export default Login;