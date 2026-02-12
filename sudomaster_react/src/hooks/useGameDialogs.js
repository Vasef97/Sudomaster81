import { useState, useCallback, useImperativeHandle } from 'react';
import { DIALOGS } from '../constants/gameConstants';

export const useGameDialogs = (ref) => {
  const [dialogs, setDialogs] = useState({
    [DIALOGS.PAUSE]: false,
    [DIALOGS.EXIT]: false,
    [DIALOGS.LOGOUT]: false,
    [DIALOGS.INSTRUCTIONS]: false,
    [DIALOGS.WIN]: false,
    [DIALOGS.SETTINGS]: false,
    [DIALOGS.ABOUT]: false,
    [DIALOGS.GAME_OVER]: false,
  });

  const openDialog = useCallback((dialogName) => {
    setDialogs(prev => ({ ...prev, [dialogName]: true }));
  }, []);

  const closeDialog = useCallback((dialogName) => {
    setDialogs(prev => ({ ...prev, [dialogName]: false }));
  }, []);

  const toggleDialog = useCallback((dialogName) => {
    setDialogs(prev => ({ ...prev, [dialogName]: !prev[dialogName] }));
  }, []);

  const closeAllDialogs = useCallback(() => {
    setDialogs({
      [DIALOGS.PAUSE]: false,
      [DIALOGS.EXIT]: false,
      [DIALOGS.LOGOUT]: false,
      [DIALOGS.INSTRUCTIONS]: false,
      [DIALOGS.WIN]: false,
      [DIALOGS.SETTINGS]: false,
      [DIALOGS.ABOUT]: false,
      [DIALOGS.GAME_OVER]: false,
    });
  }, []);

  const anyDialogOpen = Object.values(dialogs).some(isOpen => isOpen);

  useImperativeHandle(ref, () => ({
    openAbout: () => openDialog(DIALOGS.ABOUT)
  }), [openDialog]);

  return {
    dialogs,
    openDialog,
    closeDialog,
    toggleDialog,
    closeAllDialogs,
    anyDialogOpen,
  };
};
