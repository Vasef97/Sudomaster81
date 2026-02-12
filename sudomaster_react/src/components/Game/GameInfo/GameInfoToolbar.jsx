import React from 'react';
import { IconButton, Button, Tooltip } from '@mui/material';
import PauseCircleOutlineIcon from '@mui/icons-material/PauseCircleOutline';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import SettingsIcon from '@mui/icons-material/Settings';
import LogoutIcon from '@mui/icons-material/Logout';
import InfoOutlineIcon from '@mui/icons-material/InfoOutline';
import './GameInfoToolbar.css';

const GameInfoToolbar = ({
  onPause,
  onRestart,
  onNewGame,
  onInfo,
  onSettings,
  onLogoutClick,
}) => {
  return (
    <div className="game-info-toolbar">
      <Tooltip title="Restart Game" arrow>
        <IconButton
          className="game-info-toolbar__restart-btn"
          onClick={onRestart}
          size="large"
          sx={{
            color: '#1a1a1a',
            padding: '0.5rem',
            '&:hover': {
              backgroundColor: 'rgba(26, 26, 26, 0.1)',
            },
          }}
        >
          <RestartAltIcon sx={{ fontSize: '2rem' }} />
        </IconButton>
      </Tooltip>

      <Tooltip title="Pause Game" arrow>
        <div className="game-info-toolbar__pause-wrapper">
          <IconButton
            className="game-info-toolbar__pause-btn"
            onClick={onPause}
            size="large"
            sx={{
              color: '#1a1a1a',
              padding: '0.5rem',
              '&:hover': {
                backgroundColor: 'rgba(26, 26, 26, 0.1)',
              },
            }}
          >
            <PauseCircleOutlineIcon sx={{ fontSize: '2rem' }} />
          </IconButton>
        </div>
      </Tooltip>

      <Tooltip title="Back to Menu" arrow>
        <Button
          onClick={onNewGame}
          className="game-info-toolbar__menu-btn"
          sx={{
            color: '#1a1a1a',
            fontWeight: 'bold',
            fontSize: '1.05rem',
            textTransform: 'none',
            padding: '0.5rem 1rem',
            border: 'none',
            backgroundColor: 'transparent',
            cursor: 'pointer',
            transition: 'all 0.2s ease',
            '&:hover': {
              backgroundColor: 'rgba(26, 26, 26, 0.1)',
            },
          }}
        >
          Menu
        </Button>
      </Tooltip>

      {onInfo && (
        <Tooltip title="Instructions" arrow>
          <IconButton
            className="game-info-toolbar__info-btn"
            onClick={() => onInfo?.()}
            size="large"
            sx={{
              color: '#1a1a1a',
              padding: '0.5rem',
              '&:hover': {
                backgroundColor: 'rgba(26, 26, 26, 0.1)',
              },
              '@media (max-width: 810px)': {
                display: 'none',
              },
            }}
          >
            <InfoOutlineIcon sx={{ fontSize: '2rem' }} />
          </IconButton>
        </Tooltip>
      )}

      {onSettings && (
        <Tooltip title="Settings" arrow>
          <IconButton
            className="game-info-toolbar__settings-btn"
            onClick={onSettings}
            size="large"
            sx={{
              color: '#1a1a1a',
              padding: '0.5rem',
              '&:hover': {
                backgroundColor: 'rgba(26, 26, 26, 0.1)',
              },
            }}
          >
            <SettingsIcon sx={{ fontSize: '2rem' }} />
          </IconButton>
        </Tooltip>
      )}

      {onLogoutClick && (
        <Tooltip title="Logout" arrow>
          <IconButton
            className="game-info-toolbar__logout-btn"
            onClick={onLogoutClick}
            size="large"
            sx={{
              color: '#1a1a1a',
              padding: '0.5rem',
              '&:hover': {
                backgroundColor: 'rgba(26, 26, 26, 0.1)',
              },
            }}
          >
            <LogoutIcon sx={{ fontSize: '2rem' }} />
          </IconButton>
        </Tooltip>
      )}
    </div>
  );
};

export default GameInfoToolbar;
