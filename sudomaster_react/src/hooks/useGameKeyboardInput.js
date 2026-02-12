import { useEffect } from 'react';

export const useGameKeyboardInput = (config) => {
  const {
    selectedCell,
    sessionId,
    isGameWon,
    anyDialogOpen,
    isCandidateMode,
    dialogs,
    onNumberClick,
    onClear,
    onUndo,
    onRestart,
    onDialogToggle,
    onDialogOpen,
    onDialogClose,
    onDialogCloseAll,
    onPauseClick,
    onCellNavigate,
    onCandidateModeToggle,
    onExitConfirm,
    onLogoutConfirm,
  } = config;

  useEffect(() => {
    const handleKeyDown = (e) => {
      const code = e.code.toLowerCase();

      if (e.key === 'Escape') {
        e.preventDefault();
        if (dialogs.instructions || dialogs.settings || dialogs.about) {
          onDialogCloseAll();
        } else if (dialogs.pause) {
          onDialogClose('pause');
        } else if (dialogs.exit) {
          onDialogClose('exit');
        } else if (dialogs.logout) {
          onDialogClose('logout');
        } else {
          onDialogToggle('exit');
        }
        return;
      }

      if (e.key === 'Enter') {
        e.preventDefault();
        if (dialogs.instructions || dialogs.about) onDialogCloseAll();
        else if (dialogs.logout) onLogoutConfirm?.();
        else if (dialogs.exit) onExitConfirm?.();
        return;
      }

      if (code === 'keyl' && !anyDialogOpen) {
        e.preventDefault();
        onDialogToggle('logout');
        return;
      }

      if (code === 'keyr' && !anyDialogOpen) {
        e.preventDefault();
        onRestart();
        return;
      }

      if (code === 'keym' && !anyDialogOpen) {
        e.preventDefault();
        onDialogToggle('exit');
        return;
      }

      if (code === 'keys' && !anyDialogOpen) {
        e.preventDefault();
        onDialogToggle('settings');
        return;
      }

      if (code === 'keyi' && !anyDialogOpen) {
        e.preventDefault();
        onDialogOpen('instructions');
        return;
      }

      if (code === 'keya' && !anyDialogOpen) {
        e.preventDefault();
        onDialogOpen('about');
        return;
      }

      if (e.key === ' ' && !anyDialogOpen) {
        e.preventDefault();
        onPauseClick();
        return;
      }

      if (selectedCell && sessionId && !isGameWon && !anyDialogOpen) {
        const { row, col } = selectedCell;

        if (e.key >= '1' && e.key <= '9') {
          e.preventDefault();
          onNumberClick(parseInt(e.key), isCandidateMode);
          return;
        }

        if (e.key === 'Delete' || e.key === 'Backspace') {
          e.preventDefault();
          onClear();
          return;
        }

        if (e.key === 'Shift' || e.key === 'Tab') {
          e.preventDefault();
          onCandidateModeToggle();
          return;
        }

        if (e.key === 'ArrowUp' && row > 0) {
          e.preventDefault();
          onCellNavigate(row - 1, col);
        } else if (e.key === 'ArrowDown' && row < 8) {
          e.preventDefault();
          onCellNavigate(row + 1, col);
        } else if (e.key === 'ArrowLeft' && col > 0) {
          e.preventDefault();
          onCellNavigate(row, col - 1);
        } else if (e.key === 'ArrowRight' && col < 8) {
          e.preventDefault();
          onCellNavigate(row, col + 1);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [
    selectedCell,
    sessionId,
    isCandidateMode,
    anyDialogOpen,
    dialogs,
    isGameWon,
    onNumberClick,
    onClear,
    onUndo,
    onRestart,
    onDialogToggle,
    onDialogOpen,
    onDialogClose,
    onDialogCloseAll,
    onPauseClick,
    onCellNavigate,
    onCandidateModeToggle,
    onExitConfirm,
    onLogoutConfirm,
  ]);
};
