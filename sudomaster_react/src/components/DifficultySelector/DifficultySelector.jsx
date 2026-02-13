import React, { useState, useEffect, useCallback } from 'react';
import { IconButton, Tooltip } from '@mui/material';
import LeaderboardIcon from '@mui/icons-material/Leaderboard';
import InfoOutlineIcon from '@mui/icons-material/InfoOutline';
import SettingsIcon from '@mui/icons-material/Settings';
import LogoutIcon from '@mui/icons-material/Logout';
import SudokuIcon from '../Common/SudokuIcon/SudokuIcon';
import Leaderboard from '../Leaderboard/Leaderboard';
import InstructionsDialog from '../Dialogs/InstructionsDialog/InstructionsDialog';
import AboutDialog from '../Dialogs/AboutDialog/AboutDialog';
import ConfirmDialog from '../Dialogs/ConfirmDialog/ConfirmDialog';
import ResumeGameDialog from '../Dialogs/ResumeGameDialog/ResumeGameDialog';
import SettingsDialog from '../Dialogs/SettingsDialog/SettingsDialog';
import { gameService, authService } from '../../services/authService';
import { COLOR_PROFILES, DEFAULT_SETTINGS } from '../../constants/gameConstants';
import './DifficultySelector.css';

const DifficultySelector = ({ onSelectDifficulty, onLogout, user, colorProfile, setColorProfile, buildPreferencesJson, getUserSettingsForGame, applyPreferencesFromResponse }) => {
  const [leaderboardOpen, setLeaderboardOpen] = useState(false);
  const [isInstructionsOpen, setIsInstructionsOpen] = useState(false);
  const [isAboutOpen, setIsAboutOpen] = useState(false);
  const [isLogoutDialogOpen, setIsLogoutDialogOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [menuSettings, setMenuSettings] = useState(() => {
    const prefs = getUserSettingsForGame ? getUserSettingsForGame() : {};
    return { ...DEFAULT_SETTINGS, ...prefs };
  });
  const [showResumeDialog, setShowResumeDialog] = useState(false);
  const [pendingSavedGame, setPendingSavedGame] = useState(null);
  const [pendingDifficulty, setPendingDifficulty] = useState(null);
  const [isCheckingSavedGame, setIsCheckingSavedGame] = useState(false);

  const difficulties = [
    { level: 'EASY', label: 'Easy' },
    { level: 'MEDIUM', label: 'Medium' },
    { level: 'HARD', label: 'Hard' },
    { level: 'INSANE', label: 'Insane' },
  ];

  const handleDifficultyClick = async (level) => {
    if (isCheckingSavedGame) return;
    setIsCheckingSavedGame(true);
    try {
      const savedGame = await gameService.getSavedGame(level);
      if (savedGame && savedGame.sessionId) {
        setPendingDifficulty(level);
        setPendingSavedGame(savedGame);
        setShowResumeDialog(true);
      } else {
        onSelectDifficulty(level);
      }
    } catch (e) {
      onSelectDifficulty(level);
    } finally {
      setIsCheckingSavedGame(false);
    }
  };

  const handleContinueGame = () => {
    setShowResumeDialog(false);
    onSelectDifficulty(pendingDifficulty, pendingSavedGame);
    setPendingSavedGame(null);
    setPendingDifficulty(null);
  };

  const handleCancelDialog = () => {
    setShowResumeDialog(false);
    setPendingSavedGame(null);
    setPendingDifficulty(null);
  };

  const handleDiscardGame = async () => {
    setShowResumeDialog(false);
    const sessionId = pendingSavedGame?.sessionId;
    const diff = pendingDifficulty;
    setPendingSavedGame(null);
    setPendingDifficulty(null);
    if (sessionId) {
      try {
        await gameService.abandonGame(sessionId);
      } catch (e) { }
    }
    onSelectDifficulty(diff);
  };

  const handleLeaderboardOpen = () => {
    document.activeElement?.blur();
    setLeaderboardOpen(true);
  };

  const handleInstructionsClick = () => {
    document.activeElement?.blur();
    setIsInstructionsOpen(true);
  };

  const handleLogoutClick = () => {
    document.activeElement?.blur();
    setIsLogoutDialogOpen(true);
  };

  const handleLogoutConfirm = () => {
    setIsLogoutDialogOpen(false);
    onLogout();
  };

  const handleLogoutCancel = () => {
    setIsLogoutDialogOpen(false);
  };

  const handleAboutClick = () => {
    setIsInstructionsOpen(false);
    setIsAboutOpen(true);
  };

  const handleSettingsClick = () => {
    document.activeElement?.blur();
    setIsSettingsOpen(true);
  };

  const handleSettingsClose = useCallback(() => {
    setIsSettingsOpen(false);
    if (buildPreferencesJson) {
      const prefsJson = buildPreferencesJson({
        colorProfile,
        fontSize: menuSettings.fontSize,
        highlightConflicts: menuSettings.highlightConflicts,
        highlightRowColumn: menuSettings.highlightRowColumn,
        highlightBox: menuSettings.highlightBox,
        highlightIdenticalNumbers: menuSettings.highlightIdenticalNumbers,
      });
      authService.savePreferences(prefsJson).then(() => {
        if (applyPreferencesFromResponse) applyPreferencesFromResponse(prefsJson);
      }).catch(() => {});
    }
  }, [colorProfile, menuSettings, buildPreferencesJson, applyPreferencesFromResponse]);

  const handleMenuSettingsChange = (newSettings) => {
    setMenuSettings(newSettings);
  };

  const handleMenuColorProfileChange = (newProfile) => {
    if (setColorProfile) setColorProfile(newProfile);
  };

  const handleDeleteAccount = async (password) => {
    try {
      await authService.deleteAccount(password);
      setTimeout(() => {
        window.location.href = '/';
      }, 500);
    } catch (error) {
      const errorMessage = error.message || 'Failed to delete account. Please check your password and try again.';
      throw new Error(errorMessage);
    }
  };

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (showResumeDialog) {
        if (event.key === 'Escape') {
          event.preventDefault();
          handleCancelDialog();
        } else if (event.key === 'Enter') {
          event.preventDefault();
          handleContinueGame();
        }
        return;
      }

      if (event.key === 'Escape') {
        if (leaderboardOpen) {
          setLeaderboardOpen(false);
        } else if (isSettingsOpen) {
          handleSettingsClose();
        } else if (isInstructionsOpen) {
          setIsInstructionsOpen(false);
        } else if (isAboutOpen) {
          setIsAboutOpen(false);
        } else if (isLogoutDialogOpen) {
          handleLogoutCancel();
        } else {
          handleLogoutClick();
        }
      } else if (event.key === 'Enter' && isLogoutDialogOpen) {
        handleLogoutConfirm();
      } else if (!leaderboardOpen && !isInstructionsOpen && !isAboutOpen && !isLogoutDialogOpen && !isSettingsOpen) {
        if (event.key.toLowerCase() === 'i') {
          event.preventDefault();
          handleInstructionsClick();
        } else if (event.key.toLowerCase() === 'a') {
          event.preventDefault();
          setIsAboutOpen(true);
        } else if (event.key.toLowerCase() === 's') {
          event.preventDefault();
          handleSettingsClick();
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [leaderboardOpen, isInstructionsOpen, isAboutOpen, isLogoutDialogOpen, isSettingsOpen, showResumeDialog, handleSettingsClose]);

  const accentColor = COLOR_PROFILES[colorProfile]?.intensive || '#ffa500';

  return (
    <div className="difficulty-selector">
      <div className="difficulty-selector__header">
        <Tooltip title="View Leaderboard" arrow>
          <IconButton
            onClick={handleLeaderboardOpen}
            sx={{
              color: accentColor,
              fontSize: '1.5rem',
              padding: '0.5rem',
              '&:hover': {
                backgroundColor: `${accentColor}1a`,
              },
              '@media (max-width: 810px)': {
                padding: '1rem',
              },
            }}
          >
            <LeaderboardIcon sx={{ fontSize: '2rem', '@media (max-width: 810px)': { fontSize: '2.5rem' } }} />
          </IconButton>
        </Tooltip>

        <div className="difficulty-selector__spacer" />

        <Tooltip title="Instructions" arrow>
          <IconButton
            onClick={handleInstructionsClick}
            sx={{
              color: '#999999',
              fontSize: '1.5rem',
              padding: '0.5rem',
              '&:hover': {
                backgroundColor: 'rgba(153, 153, 153, 0.1)',
              },
              '@media (max-width: 810px)': {
                padding: '1rem',
              },
            }}
          >
            <InfoOutlineIcon sx={{ fontSize: '2rem', '@media (max-width: 810px)': { fontSize: '2.5rem' } }} />
          </IconButton>
        </Tooltip>

        <Tooltip title="Settings" arrow>
          <IconButton
            onClick={handleSettingsClick}
            sx={{
              color: '#999999',
              fontSize: '1.5rem',
              padding: '0.5rem',
              '&:hover': {
                backgroundColor: 'rgba(153, 153, 153, 0.1)',
              },
              '@media (max-width: 810px)': {
                padding: '1rem',
              },
            }}
          >
            <SettingsIcon sx={{ fontSize: '2rem', '@media (max-width: 810px)': { fontSize: '2.5rem' } }} />
          </IconButton>
        </Tooltip>

        <Tooltip title="Logout" arrow>
          <IconButton
            onClick={handleLogoutClick}
            sx={{
              color: '#999999',
              fontSize: '1.5rem',
              padding: '0.5rem',
              '&:hover': {
                backgroundColor: 'rgba(153, 153, 153, 0.1)',
              },
              '@media (max-width: 810px)': {
                padding: '1rem',
              },
            }}
          >
            <LogoutIcon sx={{ fontSize: '2rem', '@media (max-width: 810px)': { fontSize: '2.5rem' } }} />
          </IconButton>
        </Tooltip>
      </div>

      <div className="difficulty-selector__logo">
        <SudokuIcon size="medium" colorProfile={COLOR_PROFILES[colorProfile]} />
      </div>

      <h1 className="difficulty-selector__title">
        Sudomaster<span className="difficulty-selector__title-number" style={{ color: accentColor }}>81</span>
      </h1>

      <p className="difficulty-selector__subtitle">
        "Because your brain deserves better than scrolling"
      </p>

      <p className="difficulty-selector__prompt">Choose Your Puzzle:</p>

      <div className="difficulty-selector__buttons">
        {difficulties.map(({ level, label }) => (
          <button
            key={level}
            className="difficulty-selector__button"
            onClick={() => handleDifficultyClick(level)}
            disabled={isCheckingSavedGame}
          >
            {label}
          </button>
        ))}
      </div>

      <Leaderboard open={leaderboardOpen} onClose={() => setLeaderboardOpen(false)} currentUsername={user?.username} accentColor={accentColor} />

      <InstructionsDialog open={isInstructionsOpen} onClose={() => setIsInstructionsOpen(false)} onAboutClick={handleAboutClick} colorProfile={colorProfile} colorProfiles={COLOR_PROFILES} />

      <AboutDialog open={isAboutOpen} onClose={() => setIsAboutOpen(false)} colorProfile={colorProfile} colorProfiles={COLOR_PROFILES} />

      <ConfirmDialog
        open={isLogoutDialogOpen}
        onCancel={handleLogoutCancel}
        onConfirm={handleLogoutConfirm}
        title="Logout?"
        message="Are you sure you want to logout?"
        confirmText="Yes, logout"
      />

      <ResumeGameDialog
        open={showResumeDialog}
        onContinue={handleContinueGame}
        onDiscard={handleDiscardGame}
        onCancel={handleCancelDialog}
        savedGameData={pendingSavedGame}
        accentColor={accentColor}
      />

      <SettingsDialog
        open={isSettingsOpen}
        onClose={handleSettingsClose}
        settings={menuSettings}
        onSettingsChange={handleMenuSettingsChange}
        isAutoCandidateMode={false}
        onToggleAutoCandidateMode={() => {}}
        colorProfile={colorProfile}
        onColorProfileChange={handleMenuColorProfileChange}
        colorProfiles={COLOR_PROFILES}
        onDeleteAccount={handleDeleteAccount}
        hideGameModes
      />
    </div>
  );
};

export default DifficultySelector;
