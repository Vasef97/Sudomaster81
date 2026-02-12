import { storeAuthData, clearAuthData, fetchWithAuth, getApiBaseUrl } from './httpClient.js';

const API_BASE_URL = getApiBaseUrl();

export const register = async (username, email, password) => {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 15000);
  try {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password }),
      signal: controller.signal
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Registration failed');
    }

    const data = await response.json();
    storeAuthData(data);
    return data;
  } catch (error) {
    if (error.name === 'AbortError') throw new Error('Request timed out');
    throw error;
  } finally {
    clearTimeout(timeoutId);
  }
};

export const login = async (username, password) => {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 15000);
  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ emailOrUsername: username, password }),
      signal: controller.signal
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }

    const data = await response.json();
    storeAuthData(data);
    return data;
  } catch (error) {
    if (error.name === 'AbortError') throw new Error('Request timed out');
    throw error;
  } finally {
    clearTimeout(timeoutId);
  }
};

export const logout = async () => {
  try {
    const url = `${API_BASE_URL}/auth/logout`;
    await fetchWithAuth(url, { method: 'POST' });
  } catch (error) {
  } finally {
    clearAuthData();
  }
};

export const deleteAccount = async (password) => {
  try {
    if (!password || password.trim() === '') {
      throw new Error('Password is required');
    }
    
    const url = `${API_BASE_URL}/auth/delete-account`;
    const response = await fetchWithAuth(url, { 
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ password })
    });
    
    clearAuthData();
    
    return response;
  } catch (error) {
    throw error;
  }
};

export const savePreferences = async (preferencesJson) => {
  const url = `${API_BASE_URL}/auth/preferences`;
  return await fetchWithAuth(url, {
    method: 'PUT',
    body: JSON.stringify({ preferencesJson }),
  });
};

export const getToken = () => localStorage.getItem('token');

export const getTokenExpiry = () => localStorage.getItem('tokenExpiry');

export const isTokenValid = () => {
  const token = localStorage.getItem('token');
  const expiry = localStorage.getItem('tokenExpiry');
  
  if (!token || !expiry) return false;
  
  const now = Date.now();
  const expiryTime = parseInt(expiry);
  
  return now < expiryTime;
};

export const getTimeRemaining = () => {
  const expiry = localStorage.getItem('tokenExpiry');
  if (!expiry) return 0;
  
  const now = Date.now();
  const expiryTime = parseInt(expiry);
  const remaining = Math.max(0, expiryTime - now);
  
  return Math.round(remaining / (1000 * 60 * 60));
};

export const isLoggedIn = () => {
  const token = localStorage.getItem('token');
  const expiry = localStorage.getItem('tokenExpiry');
  
  if (!token || !expiry) return false;
  
  const now = Date.now();
  const expiryTime = parseInt(expiry);
  
  return now < expiryTime;
};

export const getCurrentUser = () => {
  const token = localStorage.getItem('token');
  const expiry = localStorage.getItem('tokenExpiry');
  
  if (!token || !expiry) return null;
  
  const now = Date.now();
  const expiryTime = parseInt(expiry);
  
  if (now >= expiryTime) return null;
  
  const userId = localStorage.getItem('userId');
  const username = localStorage.getItem('username');
  
  if (!userId || !username) return null;
  
  return { userId, username };
};

export default {
  register,
  login,
  logout,
  deleteAccount,
  savePreferences,
  getToken,
  getTokenExpiry,
  isTokenValid,
  getTimeRemaining,
  isLoggedIn,
  getCurrentUser
};
