const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const FETCH_TIMEOUT_MS = 15000;

export const storeAuthData = (data) => {
  const expiryTime = Date.now() + (data.tokenExpiresIn || 72 * 60 * 60 * 1000);
  localStorage.setItem('token', data.accessToken);
  localStorage.setItem('tokenExpiry', expiryTime.toString());
  localStorage.setItem('userId', data.userId);
  localStorage.setItem('username', data.username);
  if (data.preferencesJson) {
    localStorage.setItem('userPreferences', data.preferencesJson);
  }
};

export const clearAuthData = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('tokenExpiry');
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  localStorage.removeItem('userPreferences');
};

export const fetchWithAuth = async (url, options = {}) => {
  const token = localStorage.getItem('token');
  const tokenExpiry = localStorage.getItem('tokenExpiry');
  
  const now = Date.now();
  const expiryTime = tokenExpiry ? parseInt(tokenExpiry) : 0;
  const isTokenValid = token && expiryTime && now < expiryTime;

  if (!token) {
    throw new Error('Not authenticated - token not found');
  }

  if (!isTokenValid) {
    clearAuthData();
    throw new Error('Token expired - please login again');
  }

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
    ...options.headers
  };

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), FETCH_TIMEOUT_MS);

  let response;
  try {
    response = await fetch(url, {
      ...options,
      headers,
      signal: controller.signal
    });
  } catch (fetchError) {
    clearTimeout(timeoutId);
    if (fetchError.name === 'AbortError') {
      throw new Error('Request timed out');
    }
    throw fetchError;
  }

  clearTimeout(timeoutId);

  if (response.status === 401) {
    let errorMsg = 'Authentication failed - please login again';
    let shouldClearAuth = true;
    
    try {
      const responseText = await response.text();
      if (responseText) {
        try {
          const errorDetails = JSON.parse(responseText);
          errorMsg = errorDetails.message || errorMsg;
          
          if (errorMsg === 'Invalid password' || errorMsg.includes('password')) {
            shouldClearAuth = false;
          }
        } catch (parseError) {
          errorMsg = responseText || errorMsg;
        }
      }
    } catch (e) {
    }
    
    if (shouldClearAuth) {
      clearAuthData();
    }
    
    throw new Error(errorMsg);
  }

  if (response.status === 403) {
    let errorMsg = `API error: 403 Forbidden`;
    let errorDetails = {};
    
    try {
      const responseText = await response.text();
      if (responseText) {
        try {
          errorDetails = JSON.parse(responseText);
          errorMsg = errorDetails.message || errorMsg;
        } catch (parseError) {
          errorMsg = responseText || errorMsg;
        }
      }
    } catch (e) {
    }
    
    throw new Error(errorMsg);
  }

  if (!response.ok) {
    let errorMsg = `API error: ${response.status}`;
    let errorDetails = {};
    
    try {
      const responseText = await response.text();
      
      if (responseText) {
        try {
          errorDetails = JSON.parse(responseText);
          errorMsg = errorDetails.message || errorMsg;
        } catch (parseError) {
          errorMsg = responseText || errorMsg;
        }
      }
    } catch (e) {
    }
    
    if (response.status >= 500) {
      errorMsg = `[${response.status}] ${errorMsg}`;
    }
    
    throw new Error(errorMsg);
  }

  try {
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      const jsonResponse = await response.json();
      return jsonResponse;
    } else {
      const textResponse = await response.text();
      return textResponse || { success: true };
    }
  } catch (error) {
    throw error;
  }
};

export const checkServerHealth = async () => {
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 5000);
    
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'OPTIONS',
        headers: { 'Content-Type': 'application/json' },
        signal: controller.signal
      });
      
      clearTimeout(timeoutId);
      
      return true;
    } catch (fetchError) {
      clearTimeout(timeoutId);
      throw new Error('Server is not available: ' + fetchError.message);
    }
  } catch (error) {
    throw error;
  }
};

export const getApiBaseUrl = () => API_BASE_URL;

export default {
  API_BASE_URL,
  storeAuthData,
  clearAuthData,
  fetchWithAuth,
  checkServerHealth,
  getApiBaseUrl
};
