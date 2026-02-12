import React, { useEffect, useRef } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';
import './PauseDialog.css';

const PauseDialog = ({ open, onResume }) => {
  const onResumeRef = useRef(onResume);

  useEffect(() => {
    onResumeRef.current = onResume;
  }, [onResume]);

  useEffect(() => {
    if (!open) return;

    const handleKeyPress = (e) => {
      if (e.key === ' ') {
        e.preventDefault();
        onResumeRef.current?.();
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [open]);

  return (
    <Dialog
      open={open}
      onClose={onResume}
      className="pause-dialog"
      disableEnforceFocus
      disableAutoFocus
      disableRestoreFocus
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
      <DialogTitle sx={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1a1a1a' }}>
        Your game has been paused
      </DialogTitle>
      <DialogContent sx={{ padding: '1.5rem', textAlign: 'center' }} className="pause-dialog__content">
        <p className="pause-dialog__hint">
          💡 Press <kbd>Space</kbd> to resume
        </p>
      </DialogContent>
      <DialogActions sx={{ padding: '1rem 1.5rem', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
        <Button
          onClick={onResume}
          variant="contained"
          sx={{
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
          Resume
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default PauseDialog;
