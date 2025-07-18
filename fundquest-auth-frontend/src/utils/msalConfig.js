// Microsoft OAuth configuration for authorization code flow (Web app - no PKCE)
export const msalConfig = {
  clientId: process.env.REACT_APP_AZURE_CLIENT_ID,
  tenantId: process.env.REACT_APP_AZURE_TENANT_ID,
  redirectUri: process.env.REACT_APP_REDIRECT_URI,
  authority: `https://login.microsoftonline.com/${process.env.REACT_APP_AZURE_TENANT_ID}`,
};

// Microsoft OAuth2 endpoints
export const loginRequest = {
  scopes: ["openid", "profile", "email"],
  responseType: "code",
  responseMode: "query",
};

// Build authorization URL for authorization code flow (no PKCE for Web apps)
export async function buildAuthUrl() {
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
}

// Parse authorization code from URL query parameters
export const parseCodeFromUrl = () => {
  const urlParams = new URLSearchParams(window.location.search);

  return {
    code: urlParams.get('code'),
    error: urlParams.get('error'),
    errorDescription: urlParams.get('error_description'),
    state: urlParams.get('state'),
  };
};

// Helper functions for cleanup (no longer needed but keeping for compatibility)
export const getCodeVerifier = () => {
  return null; // Not needed for Web apps
};

export const clearCodeVerifier = () => {
  // Not needed for Web apps
};