import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { formatTime } from '../../../utils/formatters';
import './WinDialog.css';

const WinDialog = ({ open, onClose, onNewGame, difficulty, elapsedTime, message, score, errorCount, showMistakes, threeMistakeLimit, autoCandidateModeUsed, colorProfile = {} }) => {
  const accentColor = colorProfile.intensive || '#ffa500';

  return (
    <Dialog
      open={open}
      onClose={(event, reason) => {
        if (reason === 'backdropClick') return;
        onClose();
      }}
      className="win-dialog"
      PaperProps={{
        sx: {
          borderRadius: '0.8rem',
          boxShadow: '0 10px 40px rgba(0, 0, 0, 0.3)',
          backgroundColor: '#ffffff',
          minWidth: '380px',
          width: 'fit-content',
        },
      }}
      BackdropProps={{
        sx: {
          backdropFilter: 'blur(4px)',
          backgroundColor: 'rgba(0, 0, 0, 0.4)',
        },
      }}
    >
      <DialogTitle sx={{ fontSize: '2rem', fontWeight: 'bold', color: '#1a1a1a', textAlign: 'center', paddingBottom: '0.5rem' }}>
        Congratulations!
      </DialogTitle>
      <DialogContent sx={{ padding: '1.5rem 2rem', textAlign: 'center', overflow: 'auto', pb: 0 }}>
        <p className="win-dialog__message">
          {message || 'You solved the puzzle!'}
        </p>
        
        {score !== null && score !== undefined && (
          <div className="win-dialog__score-section">
            <p className="win-dialog__score-label">Score</p>
            <p className="win-dialog__score-value" style={{ color: accentColor }}>
              {score}
            </p>
          </div>
        )}

        <div className="win-dialog__details-row">
          <div className="win-dialog__detail">
            <span className="win-dialog__detail-label">Difficulty</span>
            <span className="win-dialog__detail-value">{difficulty}</span>
          </div>
          <div className="win-dialog__detail">
            <span className="win-dialog__detail-label win-dialog__detail-label--time">
              <AccessTimeIcon sx={{ fontSize: '0.85rem', color: '#666' }} />
              Time
            </span>
            <span className="win-dialog__detail-value">{formatTime(elapsedTime)}</span>
          </div>
          {autoCandidateModeUsed && (
            <div className="win-dialog__detail">
              <span className="win-dialog__detail-label">Assisted</span>
              <span className="win-dialog__detail-value">Yes</span>
            </div>
          )}
          {showMistakes && (
            <div className="win-dialog__detail">
              <span className="win-dialog__detail-label">Mistakes</span>
              <span className="win-dialog__detail-value">
                {threeMistakeLimit ? `${errorCount}/3` : errorCount}
              </span>
            </div>
          )}
        </div>
      </DialogContent>
      <DialogActions sx={{ padding: '1rem 1.5rem', gap: '1rem', justifyContent: 'center' }}>
        <Button
          onClick={onNewGame}
          variant="contained"
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
          New Game
        </Button>
        <Button
          onClick={onClose}
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
          Exit
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default WinDialog;
