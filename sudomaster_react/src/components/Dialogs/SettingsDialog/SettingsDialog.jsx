import React, { useState, useCallback } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControlLabel,
  Switch,
  TextField,
  Slider,
} from '@mui/material';
import { useDialogKeyboardShortcuts } from '../../../hooks/useDialogKeyboardShortcuts';
import './SettingsDialog.css';

const SettingsDialog = ({ open, onClose, settings, onSettingsChange, isAutoCandidateMode, onToggleAutoCandidateMode, colorProfile, onColorProfileChange, colorProfiles, onDeleteAccount, hideGameModes }) => {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');
  const [deleteError, setDeleteError] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const [affectsScoreKeys, setAffectsScoreKeys] = useState({});
  const { handleKeyDown } = useDialogKeyboardShortcuts({ onClose });

  const triggerAffectsScore = useCallback((key) => {
    setAffectsScoreKeys(prev => ({ ...prev, [key]: Date.now() }));
    setTimeout(() => {
      setAffectsScoreKeys(prev => {
        const next = { ...prev };
        delete next[key];
        return next;
      });
    }, 2000);
  }, []);

  const handleDeleteConfirm = async () => {
    if (!deletePassword.trim()) {
      setDeleteError('Password is required');
      return;
    }
    
    setIsDeleting(true);
    try {
      if (onDeleteAccount) {
        await onDeleteAccount(deletePassword);
      }
      setShowDeleteConfirm(false);
      setDeletePassword('');
      setDeleteError('');
      onClose();
    } catch (error) {
      setDeleteError(error.message || 'Failed to delete account. Please check your password and try again.');
      setIsDeleting(false);
    }
  };

  const handleDeleteConfirmKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      e.stopPropagation();
      if (deletePassword.trim() && !isDeleting) {
        handleDeleteConfirm();
      }
    }
    if (e.key === 'Escape') {
      e.preventDefault();
      setShowDeleteConfirm(false);
      setDeletePassword('');
      setDeleteError('');
    }
  };

  const handleToggle = (setting) => {
    if (setting === 'errorIndicator' || setting === 'threeMistakeLimit') {
      if (settings[setting]) return;
      if (setting === 'errorIndicator' && settings.threeMistakeLimit) return;
      if (setting === 'threeMistakeLimit' && settings.errorIndicator) return;
    }

    const newValue = !settings[setting];

    if (newValue && (setting === 'errorIndicator' || setting === 'threeMistakeLimit')) {
      triggerAffectsScore(setting);
    }

    const updatedSettings = { ...settings, [setting]: newValue };
    onSettingsChange(updatedSettings);
  };

  const handleAutoCandidateToggle = () => {
    if (!isAutoCandidateMode) {
      triggerAffectsScore('autoCandidate');
    }
    onToggleAutoCandidateMode();
  };

  const handleFontSizeChange = (event, newValue) => {
    const fontSizeMap = { 0: 'normal', 1: 'medium', 2: 'large' };
    const fontSize = fontSizeMap[newValue] || 'medium';
    onSettingsChange({ ...settings, fontSize });
  };

  const isMistakeModeLocked = settings.errorIndicator || settings.threeMistakeLimit;
  const accentColor = colorProfiles[colorProfile]?.intensive || '#ffa500';

  const switchSx = {
    '& .MuiSwitch-switchBase.Mui-checked': { color: accentColor },
    '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': { backgroundColor: accentColor },
  };

  const toggleSx = {
    fontSize: '1rem',
    color: '#333',
    padding: '0.25rem',
    borderRadius: '0.4rem',
    transition: 'background-color 0.2s ease',
    '&:hover': { backgroundColor: '#f0f0f0' },
  };

  const renderAffectsScore = (key) => (
    affectsScoreKeys[key] ? (
      <span key={affectsScoreKeys[key]} className="settings-affects-score">affects score</span>
    ) : null
  );

  return (
    <Dialog
      open={open}
      onClose={onClose}
      className="settings-dialog"
      disableEnforceFocus
      disableAutoFocus
      disableRestoreFocus
      onKeyDown={handleKeyDown}
      PaperProps={{
        sx: {
          borderRadius: '0.8rem',
          boxShadow: '0 10px 40px rgba(0, 0, 0, 0.3)',
          backgroundColor: '#ffffff',
          minWidth: '400px',
          maxHeight: '90vh',
        },
      }}
      BackdropProps={{
        sx: {
          backdropFilter: 'blur(4px)',
          backgroundColor: 'rgba(0, 0, 0, 0.4)',
        },
      }}
    >
      <DialogTitle sx={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1a1a1a', paddingBottom: '1.5rem' }}>
        {hideGameModes ? 'Settings' : 'Game Settings'}
      </DialogTitle>
      <DialogContent sx={{ padding: '0 2rem 0.5rem 2rem', display: 'flex', flexDirection: 'column', gap: '1rem', maxHeight: '60vh', overflow: 'auto' }}>

        <FormControlLabel
          control={<Switch checked={settings.highlightConflicts} onChange={() => handleToggle('highlightConflicts')} sx={switchSx} />}
          label="Highlight conflicts"
          sx={toggleSx}
        />
        <FormControlLabel
          control={<Switch checked={settings.highlightRowColumn} onChange={() => handleToggle('highlightRowColumn')} sx={switchSx} />}
          label="Highlight row and column"
          sx={toggleSx}
        />
        <FormControlLabel
          control={<Switch checked={settings.highlightBox} onChange={() => handleToggle('highlightBox')} sx={switchSx} />}
          label="Highlight 3x3 box"
          sx={toggleSx}
        />
        <FormControlLabel
          control={<Switch checked={settings.highlightIdenticalNumbers} onChange={() => handleToggle('highlightIdenticalNumbers')} sx={switchSx} />}
          label="Highlight identical numbers"
          sx={toggleSx}
        />

        {!hideGameModes && <div style={{ borderTop: '1px solid #e0e0e0', margin: '0.5rem 0' }} />}

        {!hideGameModes && (
          <>
            <FormControlLabel
              control={<Switch checked={isAutoCandidateMode} onChange={handleAutoCandidateToggle} sx={switchSx} />}
              label={
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                  Auto candidate mode
                  {renderAffectsScore('autoCandidate')}
                </span>
              }
              sx={toggleSx}
            />
            <FormControlLabel
              control={
                <Switch
                  checked={settings.errorIndicator || false}
                  onChange={() => handleToggle('errorIndicator')}
                  disabled={isMistakeModeLocked && !settings.errorIndicator}
                  sx={switchSx}
                />
              }
              label={
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                  Mistake Indicator
                  {settings.errorIndicator && <span style={{ fontSize: '0.75rem', color: '#999' }}>🔒</span>}
                  {renderAffectsScore('errorIndicator')}
                </span>
              }
              sx={{
                ...toggleSx,
                color: isMistakeModeLocked && !settings.errorIndicator ? '#aaa' : '#333',
                '&:hover': {
                  backgroundColor: isMistakeModeLocked && !settings.errorIndicator ? 'transparent' : '#f0f0f0',
                },
              }}
            />
            <FormControlLabel
              control={
                <Switch
                  checked={settings.threeMistakeLimit || false}
                  onChange={() => handleToggle('threeMistakeLimit')}
                  disabled={isMistakeModeLocked && !settings.threeMistakeLimit}
                  sx={switchSx}
                />
              }
              label={
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
                  3 Mistake Limit
                  {settings.threeMistakeLimit && <span style={{ fontSize: '0.75rem', color: '#999' }}>🔒</span>}
                  {renderAffectsScore('threeMistakeLimit')}
                </span>
              }
              sx={{
                ...toggleSx,
                color: isMistakeModeLocked && !settings.threeMistakeLimit ? '#aaa' : '#333',
                '&:hover': {
                  backgroundColor: isMistakeModeLocked && !settings.threeMistakeLimit ? 'transparent' : '#f0f0f0',
                },
              }}
            />
          </>
        )}

        <div style={{ borderTop: '1px solid #e0e0e0', margin: '0.5rem 0' }} />

        <div>
          <p style={{ fontSize: '1rem', fontWeight: '600', color: '#333', margin: '0 0 1rem 0' }}>
            Choose Profile
          </p>
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            {Object.entries(colorProfiles).map(([key, profile]) => (
              <button
                key={key}
                onClick={() => onColorProfileChange(key)}
                style={{
                  width: '2rem',
                  height: '2rem',
                  borderRadius: '50%',
                  backgroundColor: profile.intensive,
                  border: colorProfile === key ? '3px solid #333' : 'none',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                  boxShadow: 'none',
                }}
                title={profile.name}
              />
            ))}
          </div>
        </div>

        <div style={{ borderTop: '1px solid #e0e0e0', margin: '0.5rem 0' }} />

        <div>
          <p style={{ fontSize: '1rem', fontWeight: '600', color: '#333', margin: '0 0 0.5rem 0' }}>
            Grid Font Size
          </p>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.75rem' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#666' }}>a</span>
            <Slider
              value={settings.fontSize === 'large' ? 2 : settings.fontSize === 'medium' ? 1 : 0}
              onChange={handleFontSizeChange}
              step={1}
              min={0}
              max={2}
              sx={{
                width: '33%',
                color: accentColor,
                '& .MuiSlider-thumb': {
                  width: 20,
                  height: 20,
                },
              }}
            />
            <span style={{ fontSize: '1.15rem', fontWeight: 'bold', color: '#666' }}>A</span>
          </div>
        </div>

        <div style={{ borderTop: '1px solid #e0e0e0', margin: '0.5rem 0' }} />

        <div>
          <Button
            onClick={() => setShowDeleteConfirm(true)}
            sx={{
              textTransform: 'none',
              fontSize: '0.9rem',
              color: '#ff6666',
              fontWeight: '600',
              padding: '0.5rem 1rem',
              minWidth: 'auto',
              marginRight: '-0.7rem',
              '&:hover': {
                backgroundColor: '#ffe6e6',
              },
            }}
          >
            Delete Account
          </Button>
        </div>
      </DialogContent>
      <DialogActions sx={{ padding: '1rem 1.5rem', gap: '0.5rem', justifyContent: 'flex-end' }}>
        <Button
          onClick={onClose}
          sx={{
            textTransform: 'none',
            fontSize: '1rem',
            color: '#1a1a1a',
            padding: '0.5rem 1.5rem',
            border: '1px solid #ddd',
            borderRadius: '0.4rem',
            backgroundColor: 'transparent',
            cursor: 'pointer',
            transition: 'all 0.2s ease',
            '&:hover': {
              backgroundColor: 'rgba(26, 26, 26, 0.05)',
              borderColor: '#999',
            },
          }}
        >
          Close
        </Button>
      </DialogActions>
      <Dialog
        open={showDeleteConfirm}
        onClose={() => {
          setShowDeleteConfirm(false);
          setDeletePassword('');
          setDeleteError('');
        }}
        maxWidth="sm"
        fullWidth
        disableEnforceFocus
        disableAutoFocus
        disableRestoreFocus
        onKeyDown={handleDeleteConfirmKeyDown}
        PaperProps={{
          sx: {
            borderRadius: '0.8rem',
            boxShadow: '0 10px 40px rgba(0, 0, 0, 0.3)',
          },
        }}
        BackdropProps={{
          sx: {
            backdropFilter: 'blur(4px)',
            backgroundColor: 'rgba(0, 0, 0, 0.4)',
          },
        }}
      >
        <DialogTitle sx={{ fontSize: '1.3rem', fontWeight: 'bold', color: '#ff6666', paddingBottom: '1rem' }}>
          Delete Account
        </DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <p style={{ color: '#555', fontSize: '0.95rem', margin: 0 }}>
            This action is permanent and cannot be undone. All your data will be permanently removed.
          </p>
          <p style={{ color: '#999', fontSize: '0.9rem', margin: 0, fontWeight: '600' }}>
            Please enter your password to confirm account deletion:
          </p>
          <TextField
            type="password"
            placeholder="Enter your password"
            value={deletePassword}
            onChange={(e) => {
              setDeletePassword(e.target.value);
              setDeleteError('');
            }}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                e.stopPropagation();
                if (deletePassword.trim() && !isDeleting) {
                  handleDeleteConfirm();
                }
              }
            }}
            error={!!deleteError}
            helperText={deleteError}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '0.4rem',
                '& fieldset': {
                  borderColor: deleteError ? '#ff6666' : '#ddd',
                },
                '&:hover fieldset': {
                  borderColor: deleteError ? '#ff6666' : '#999',
                },
              },
            }}
            autoFocus
          />
        </DialogContent>
        <DialogActions sx={{ padding: '1rem 1.5rem', gap: '0.5rem', justifyContent: 'flex-end' }}>
          <Button
            onClick={() => {
              setShowDeleteConfirm(false);
              setDeletePassword('');
              setDeleteError('');
            }}
            sx={{
              textTransform: 'none',
              fontSize: '0.95rem',
              color: '#1a1a1a',
              padding: '0.5rem 1.5rem',
              border: '1px solid #ddd',
              borderRadius: '0.4rem',
              backgroundColor: 'transparent',
              '&:hover': {
                backgroundColor: 'rgba(26, 26, 26, 0.05)',
                borderColor: '#999',
              },
            }}
          >
            Cancel
          </Button>
          <Button
            onClick={handleDeleteConfirm}
            variant="contained"
            disabled={isDeleting}
            sx={{
              textTransform: 'none',
              fontSize: '0.95rem',
              backgroundColor: '#ff6666',
              color: 'white',
              padding: '0.5rem 1.5rem',
              borderRadius: '0.4rem',
              fontWeight: '600',
              '&:hover': {
                backgroundColor: '#ff4444',
              },
              '&:disabled': {
                backgroundColor: '#cccccc',
                color: '#666666',
              },
            }}
          >
            {isDeleting ? 'Deleting...' : 'Delete Account'}
          </Button>
        </DialogActions>
      </Dialog>
    </Dialog>
  );
};

export default SettingsDialog;
