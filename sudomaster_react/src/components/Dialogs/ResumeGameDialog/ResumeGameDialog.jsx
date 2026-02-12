import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  IconButton,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { formatTime } from '../../../utils/formatters';
import './ResumeGameDialog.css';

const ResumeGameDialog = ({ open, onContinue, onDiscard, onCancel, savedGameData, accentColor = '#ffa500' }) => {
  if (!savedGameData) return null;

  return (
    <Dialog
      open={open}
      onClose={onCancel}
      className="resume-dialog"
      PaperProps={{
        sx: {
          borderRadius: '0.8rem',
          boxShadow: '0 10px 40px rgba(0, 0, 0, 0.3)',
          backgroundColor: '#ffffff',
          width: '400px',
          maxWidth: '90vw',
          position: 'relative',
        },
      }}
      BackdropProps={{
        sx: {
          backdropFilter: 'blur(4px)',
          backgroundColor: 'rgba(0, 0, 0, 0.4)',
        },
      }}
    >
      <IconButton
        onClick={onCancel}
        className="resume-dialog__close-button"
        sx={{
          position: 'absolute',
          top: 8,
          right: 8,
          color: '#999',
          '&:hover': {
            color: '#333',
            backgroundColor: 'rgba(0, 0, 0, 0.05)',
          },
          '@media (max-width: 810px)': {
            display: 'none',
          },
        }}
      >
        <CloseIcon />
      </IconButton>
      <DialogTitle sx={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1a1a1a', textAlign: 'center', paddingBottom: '0.5rem' }}>
        Resume Game?
      </DialogTitle>
      <DialogContent sx={{ padding: '1.5rem 2rem', textAlign: 'center' }}>
        <p className="resume-dialog__subtitle">
          You have a saved game
        </p>
        <div className="resume-dialog__stats">
          <div className="resume-dialog__stat-row">
            <span className="resume-dialog__stat-label">{savedGameData.difficulty}</span>
            <span className="resume-dialog__stat-separator">·</span>
            <AccessTimeIcon sx={{ fontSize: '1rem', color: '#666' }} />
            <span className="resume-dialog__stat-value">{formatTime(savedGameData.elapsedTimeSeconds || 0)}</span>
          </div>
        </div>
      </DialogContent>
      <DialogActions sx={{ padding: '1rem 1.5rem', gap: '1rem', justifyContent: 'center' }}>
        <Button
          onClick={onDiscard}
          variant="outlined"
          sx={{
            color: '#1a1a1a',
            borderColor: '#1a1a1a',
            fontWeight: 'bold',
            padding: '0.7rem 1.5rem',
            borderRadius: '0.5rem',
            '&:hover': {
              backgroundColor: 'rgba(26, 26, 26, 0.1)',
              borderColor: '#333',
            },
          }}
        >
          New Game
        </Button>
        <Button
          onClick={onContinue}
          variant="contained"
          autoFocus
          sx={{
            backgroundColor: accentColor,
            color: 'white',
            fontWeight: 'bold',
            padding: '0.7rem 1.5rem',
            borderRadius: '0.5rem',
            '&:hover': {
              backgroundColor: accentColor,
              opacity: 0.85,
            },
          }}
        >
          Continue
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ResumeGameDialog;