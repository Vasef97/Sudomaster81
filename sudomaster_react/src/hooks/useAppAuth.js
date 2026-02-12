import { useState, useEffect, useCallback, useRef } from 'react';
import { authService } from '../services/authService';

export const useAppAuth = (onLogout) => {
  const [authState, setAuthState] = useState('checking');
  const [user, setUser] = useState(null);
  const [hasServerError, setHasServerError] = useState(false);
  const [isWakingUp, setIsWakingUp] = useState(false);
  const retryCountRef = useRef(0);
  const maxRetries = 40;

  const performAuthCheck = async () => {
    try {
      try {
        await authService.checkServerHealth();
      } catch (healthError) {
        if (retryCountRef.current < maxRetries) {
          setIsWakingUp(true);
          retryCountRef.current += 1;
          setTimeout(() => performAuthCheck(), 3000);
          return;
        }
        setIsWakingUp(false);
        setHasServerError(true);
        return;
      }

      retryCountRef.current = 0;
      setIsWakingUp(false);

      const token = localStorage.getItem('token');
      const expiry = localStorage.getItem('tokenExpiry');

      if (token && expiry) {
        const now = Date.now();
        const expiryTime = parseInt(expiry);

        if (now >= expiryTime) {
          await authService.logout();
        }
      }

      const isLoggedIn = authService.isLoggedIn();

      if (isLoggedIn) {
        const currentUser = authService.getCurrentUser();
        setUser(currentUser);
        setAuthState('menu');
      } else {
        setAuthState('login');
      }
    } catch (error) {
      if (error.message?.includes('Failed to fetch') || error.code === 'ERR_NETWORK') {
        setHasServerError(true);
      } else {
        setAuthState('login');
      }
    }
  };

  useEffect(() => {
    performAuthCheck();
  }, []);

  const logoutTriggeredRef = useRef(false);

  useEffect(() => {
    if (authState !== 'menu' && authState !== 'game') return;

    const checkTokenExpiry = () => {
      const tokenExpiry = localStorage.getItem('tokenExpiry');
      if (!tokenExpiry) return;

      const now = Date.now();
      const expiryTime = parseInt(tokenExpiry);

      if (now >= expiryTime && !logoutTriggeredRef.current) {
        logoutTriggeredRef.current = true;
        window.dispatchEvent(new Event('tokenExpired'));
        setTimeout(() => handleLogout(), 1000);
      }
    };

    const intervalId = setInterval(checkTokenExpiry, 60000);
    return () => clearInterval(intervalId);
  }, [authState]);

  const handleLoginSuccess = (response) => {
    logoutTriggeredRef.current = false;
    setUser(response);
    setAuthState('menu');
  };

  const handleRegisterSuccess = (response) => {
    logoutTriggeredRef.current = false;
    setUser(response);
    setAuthState('menu');
  };

  const handleSwitchToRegister = () => {
    setAuthState('register');
  };

  const handleSwitchToLogin = () => {
    setAuthState('login');
  };

  const handleLogout = async () => {
    await authService.logout();
    setUser(null);
    setAuthState('login');
    if (onLogout) {
      onLogout();
    }
  };

  const handleRetryServer = async () => {
    setAuthState('checking');
    setHasServerError(false);

    if (user) {
      await authService.logout();
      setUser(null);
    }

    await performAuthCheck();
  };

  const handleServerError = (error) => {

    if (error.message?.includes('Failed to fetch') ||
        error.message?.includes('Server is not available') ||
        error.message?.includes('timeout') ||
        error.message?.includes('[5') ||
        error.code === 'ERR_NETWORK') {
      setHasServerError(true);
      setAuthState('checking');
    }
  };

  return {
    authState,
    user,
    hasServerError,
    isWakingUp,
    handleLoginSuccess,
    handleRegisterSuccess,
    handleSwitchToRegister,
    handleSwitchToLogin,
    handleLogout,
    handleRetryServer,
    handleServerError,
    setAuthState,
  };
};
