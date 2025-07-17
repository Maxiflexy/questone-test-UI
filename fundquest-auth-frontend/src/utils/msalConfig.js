// Simple Microsoft OAuth configuration (no MSAL library needed)
export const msalConfig = {
  clientId: process.env.REACT_APP_MICROSOFT_CLIENT_ID,
  tenantId: process.env.REACT_APP_MICROSOFT_TENANT_ID,
  redirectUri: process.env.REACT_APP_REDIRECT_URI,
  authority: `https://login.microsoftonline.com/${process.env.REACT_APP_MICROSOFT_TENANT_ID}`,
};

// Microsoft OAuth2 endpoints
export const loginRequest = {
  scopes: ["openid", "profile", "email"],
  responseType: "id_token",
  responseMode: "fragment",
};

// Build authorization URL
export const buildAuthUrl = () => {
  const params = new URLSearchParams({
    client_id: msalConfig.clientId,
    response_type: loginRequest.responseType,
    redirect_uri: msalConfig.redirectUri,
    scope: loginRequest.scopes.join(" "),
    response_mode: loginRequest.responseMode,
    nonce: Date.now().toString(),
    state: Date.now().toString(),
  });

  return `${msalConfig.authority}/oauth2/v2.0/authorize?${params.toString()}`;
};

// Parse token from URL fragment
export const parseTokenFromUrl = () => {
  const fragment = window.location.hash.substring(1);
  const params = new URLSearchParams(fragment);

  return {
    idToken: params.get('id_token'),
    error: params.get('error'),
    errorDescription: params.get('error_description'),
  };
};