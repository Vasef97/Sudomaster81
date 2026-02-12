import React, { useRef, useEffect } from 'react';
import AuthForm from '../Auth/AuthForm';
import { authService } from '../../services/authService';

export default function Register({ onRegisterSuccess, onSwitchToLogin }) {
  const pendingTimersRef = useRef([]);

  useEffect(() => {
    return () => {
      pendingTimersRef.current.forEach(timerId => clearTimeout(timerId));
      pendingTimersRef.current = [];
    };
  }, []);

  const handleRegisterSubmit = async (credentials, setLoading, showSnackbar) => {
    try {
      const response = await authService.register(
        credentials.username,
        credentials.email,
        credentials.password
      );
      
      const autoLoginTimer = setTimeout(() => {
        authService.login(credentials.username, credentials.password)
          .then(loginResponse => {
            onRegisterSuccess(loginResponse);
          })
          .catch(err => {
            const errorMsg = err.message || 'Auto-login failed';
            showSnackbar(`Registration successful, but login failed: ${errorMsg}. Please try logging in manually.`, 'error');
            setLoading(false);
            const switchTimer = setTimeout(() => onSwitchToLogin(), 3000);
            pendingTimersRef.current.push(switchTimer);
          });
      }, 1500);
      pendingTimersRef.current.push(autoLoginTimer);
    } catch (err) {
      const errorMessage = err.message || 'Registration failed. Please try again.';
      showSnackbar(errorMessage, 'error');
      setLoading(false);
    }
  };

  return (
    <AuthForm
      mode="register"
      onSubmit={handleRegisterSubmit}
      onSwitchMode={onSwitchToLogin}
      submitButtonColor="#ffa500"
    />
  );
}
