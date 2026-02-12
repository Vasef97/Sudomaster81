import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';
import './GameOverDialog.css';

const GameOverDialog = ({ open, onNewGame, onExit, colorProfile = {} }) => {
  const accentColor = colorProfile.intensive || '#ffa500';

  return (
    <Dialog
      open={open}
      className="gameover-dialog"
      PaperProps={{
        sx: {
          borderRadius: '0.8rem',
          boxShadow: '0 10px 40px rgba(0, 0, 0, 0.3)',
          backgroundColor: '#ffffff',
          width: '380px',
          maxWidth: '90vw',
        },
      }}
      BackdropProps={{
        sx: {
          backdropFilter: 'blur(4px)',
          backgroundColor: 'rgba(0, 0, 0, 0.4)',
        },
      }}
    >
      <DialogTitle sx={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#1a1a1a', textAlign: 'center', paddingBottom: '0.5rem' }}>
        Game Over
      </DialogTitle>
      <DialogContent sx={{ padding: '1.5rem 2rem', textAlign: 'center' }}>
        <p className="gameover-dialog__message">
          You made 3 mistakes.
        </p>
        <p className="gameover-dialog__submessage">
          Better luck next time!
        </p>
      </DialogContent>
      <DialogActions sx={{ padding: '1rem 1.5rem', gap: '1rem', justifyContent: 'center' }}>
        <Button
          onClick={onNewGame}
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
          New Game
        </Button>
        <Button
          onClick={onExit}
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
          Menu
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default GameOverDialog;
