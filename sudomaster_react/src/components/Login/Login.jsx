import React from 'react';
import AuthForm from '../Auth/AuthForm';
import { authService } from '../../services/authService';

export default function Login({ onLoginSuccess, onSwitchToRegister }) {
  const handleLoginSubmit = async (credentials, setLoading, showSnackbar) => {
    try {
      const response = await authService.login(credentials.username, credentials.password);
      onLoginSuccess(response);
    } catch (err) {
      const errorMessage = err.message || 'Login failed. Please check your credentials.';
      showSnackbar(errorMessage, 'error');
      setLoading(false);
    }
  };

  return (
    <AuthForm
      mode="login"
      onSubmit={handleLoginSubmit}
      onSwitchMode={onSwitchToRegister}
      submitButtonColor="#1a1a1a"
    />
  );
}
