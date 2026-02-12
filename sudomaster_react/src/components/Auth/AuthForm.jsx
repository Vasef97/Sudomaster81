import React, { useState, useEffect } from 'react';
import { Button, TextField, CircularProgress, Alert, Backdrop, Snackbar, IconButton, InputAdornment } from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import SudokuIcon from '../Common/SudokuIcon/SudokuIcon';
import './AuthForm.css';

export default function AuthForm({
  mode = 'login',
  onSubmit,
  onSwitchMode,
  submitButtonColor = '#1a1a1a',
  title = 'Sudomaster81',
}) {
  const isLogin = mode === 'login';

  const [formData, setFormData] = useState({
    email: '',
    username: '',
    password: '',
    confirmPassword: '',
  });

  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'error',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleTogglePassword = () => setShowPassword(!showPassword);

  const handleToggleConfirmPassword = () => setShowConfirmPassword(!showConfirmPassword);

  useEffect(() => {
    setFormData({
      email: '',
      username: '',
      password: '',
      confirmPassword: '',
    });
    setShowPassword(false);
    setShowConfirmPassword(false);
  }, [isLogin]);

  const showSnackbarMessage = (message, severity = 'error') => {
    setSnackbar({ open: true, message, severity });
  };

  const closeSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const validateRegisterForm = () => {
    if (!formData.email || !formData.username || !formData.password || !formData.confirmPassword) {
      showSnackbarMessage('Please fill in all fields', 'error');
      return false;
    }

    if (formData.username.length < 3) {
      showSnackbarMessage('Username must be at least 3 characters long', 'error');
      return false;
    }

    if (formData.username.length > 16) {
      showSnackbarMessage('Username must not exceed 16 characters', 'error');
      return false;
    }

    if (formData.password.length < 8) {
      showSnackbarMessage('Password must be at least 8 characters long', 'error');
      return false;
    }

    if (formData.password.length > 22) {
      showSnackbarMessage('Password must not exceed 22 characters', 'error');
      return false;
    }

    if (formData.password !== formData.confirmPassword) {
      showSnackbarMessage('Passwords do not match', 'error');
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      showSnackbarMessage('Please enter a valid email address', 'error');
      return false;
    }

    return true;
  };

  const validateLoginForm = () => {
    if (!formData.username || !formData.password) {
      showSnackbarMessage('Please fill in all fields', 'error');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (isLogin) {
      if (!validateLoginForm()) return;
    } else {
      if (!validateRegisterForm()) return;
    }

    setLoading(true);

    try {
      await onSubmit(
        {
          email: isLogin ? '' : formData.email,
          username: formData.username,
          password: formData.password,
        },
        setLoading,
        showSnackbarMessage
      );
    } catch (err) {
      const errorMessage = err.message || (isLogin ? 'Login failed' : 'Registration failed');
      showSnackbarMessage(errorMessage, 'error');
      setLoading(false);
    }
  };

  const containerClass = isLogin ? 'login-container' : 'register-container';
  const cardClass = isLogin ? 'login-card' : 'register-card';
  const headerClass = isLogin ? 'login-header' : 'register-header';
  const titleClass = isLogin ? 'login-title' : 'register-title';
  const titleOrangeClass = isLogin ? 'login-title-orange' : 'register-title-orange';
  const subtitleClass = isLogin ? 'login-subtitle' : 'register-subtitle';
  const formClass = isLogin ? 'login-form' : 'register-form';
  const footerClass = isLogin ? 'login-footer' : 'register-footer';
  const buttonText = isLogin ? 'Login' : 'Register';
  const switchText = isLogin ? 'Register here' : 'Login here';
  const switchPrompt = isLogin ? "Don't have an account?" : 'Already have an account?';
  const switchButtonColor = isLogin ? '#ffa500' : '#1a1a1a';

  return (
    <div className={containerClass}>
      <Backdrop
        sx={{
          color: '#fff',
          zIndex: 1300,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 2,
        }}
        open={loading}
      >
        <CircularProgress color="inherit" size={60} />
        <p style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>Loading...</p>
      </Backdrop>

      <div className={cardClass}>
        <div className={headerClass}>
          <SudokuIcon size="tiny" />
          <h1 className={titleClass}>
            Sudomaster
            <span className={titleOrangeClass}>81</span>
          </h1>
        </div>

        <p className={subtitleClass}>
          {isLogin ? 'Login to Your Account' : 'Create Your Account'}
        </p>
        {isLogin && <p className={subtitleClass}>Master the Sudoku</p>}

        <form onSubmit={handleSubmit} className={formClass}>
          {!isLogin && (
            <TextField
              fullWidth
              label="Email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              disabled={loading}
              margin="normal"
              variant="outlined"
              placeholder="Enter your email"
            />
          )}

          <TextField
            fullWidth
            label={isLogin ? 'Email or Username' : 'Username'}
            name="username"
            type="text"
            value={formData.username}
            onChange={handleChange}
            disabled={loading}
            margin="normal"
            variant="outlined"
            placeholder={isLogin ? 'Enter email or username' : 'Choose a username'}
          />

          <TextField
            fullWidth
            label="Password"
            name="password"
            type={showPassword ? 'text' : 'password'}
            value={formData.password}
            onChange={handleChange}
            disabled={loading}
            margin="normal"
            variant="outlined"
            placeholder={isLogin ? 'Enter password' : 'Min 8 characters'}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    onClick={handleTogglePassword}
                    edge="end"
                    disabled={loading}
                  >
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />

          {!isLogin && (
            <TextField
              fullWidth
              label="Confirm Password"
              name="confirmPassword"
              type={showConfirmPassword ? 'text' : 'password'}
              value={formData.confirmPassword}
              onChange={handleChange}
              disabled={loading}
              margin="normal"
              variant="outlined"
              placeholder="Confirm your password"
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={handleToggleConfirmPassword}
                      edge="end"
                      disabled={loading}
                    >
                      {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
          )}

          <Button
            fullWidth
            type="submit"
            variant="contained"
            size="large"
            disabled={loading}
            sx={{
              mt: 3,
              mb: 2,
              backgroundColor: submitButtonColor,
              color: '#fff',
              fontSize: '1rem',
              fontWeight: 'bold',
              padding: '12px',
              '&:hover': {
                backgroundColor: isLogin ? '#333' : '#ff8c00',
              },
              '&:disabled': {
                backgroundColor: '#ccc',
                color: '#999',
              },
            }}
          >
            {loading ? <CircularProgress size={24} color="inherit" /> : buttonText}
          </Button>
        </form>

        <div className={footerClass}>
          <p>{switchPrompt}</p>
          <Button
            variant="text"
            onClick={onSwitchMode}
            disabled={loading}
            sx={{
              color: switchButtonColor,
              fontSize: '0.9rem',
              textTransform: 'none',
              fontWeight: isLogin ? 'normal' : '600',
              '&:hover': {
                backgroundColor: 'transparent',
                textDecoration: 'underline',
              },
            }}
          >
            {switchText}
          </Button>
        </div>
      </div>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={5000}
        onClose={closeSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={closeSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </div>
  );
}
