import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';
import { useDialogKeyboardShortcuts } from '../../../hooks/useDialogKeyboardShortcuts';
import './ConfirmDialog.css';

const ConfirmDialog = ({ open, onCancel, onConfirm, title, message, confirmText = 'Yes, exit', isDangerous = false }) => {
  const { handleKeyDown } = useDialogKeyboardShortcuts({ onConfirm, onCancel });

  return (
    <Dialog
      open={open}
      onClose={onCancel}
      className="confirm-dialog"
      disableEnforceFocus
      disableAutoFocus
      disableRestoreFocus
      onKeyDown={handleKeyDown}
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
      <DialogTitle sx={{ fontSize: '1.3rem', fontWeight: 'bold', color: '#1a1a1a' }}>
        {title}
      </DialogTitle>
      <DialogContent sx={{ padding: '1.5rem', textAlign: 'center' }}>
        <p style={{ color: '#555', fontSize: '0.95rem' }}>{message}</p>
      </DialogContent>
      <DialogActions sx={{ padding: '1rem 1.5rem', gap: '1rem' }}>
        <Button
          onClick={onCancel}
          variant="outlined"
          sx={{
            color: '#1a1a1a',
            borderColor: '#1a1a1a',
            fontWeight: 'bold',
            padding: '0.7rem 1.5rem',
            borderRadius: '0.5rem',
            '&:hover': {
              backgroundColor: '#f5f5f5',
              borderColor: '#333',
            },
          }}
        >
          Cancel
        </Button>
        <Button
          onClick={onConfirm}
          variant={isDangerous ? 'outlined' : 'contained'}
          sx={isDangerous ? {
            color: '#ff4444',
            borderColor: '#ff4444',
            fontWeight: 'bold',
            padding: '0.7rem 1.5rem',
            borderRadius: '0.5rem',
            '&:hover': {
              backgroundColor: '#ff4444',
              color: 'white',
              borderColor: '#ff4444',
            },
          } : {
            backgroundColor: '#1a1a1a',
            color: 'white',
            fontWeight: 'bold',
            padding: '0.7rem 1.5rem',
            borderRadius: '0.5rem',
            '&:hover': {
              backgroundColor: '#333',
            },
          }}
        >
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;
