import React, { useEffect, useState, useRef, forwardRef, useCallback } from 'react';
import { IconButton, CircularProgress, Backdrop } from '@mui/material';
import PauseCircleOutlineIcon from '@mui/icons-material/PauseCircleOutline';
import GameTitle from '../../components/Game/GameTitle/GameTitle';
import Board from '../../components/Game/Board/Board';
import NumberPad from '../../components/Game/NumberPad/NumberPad';
import GameInfo from '../../components/Game/GameInfo/GameInfo';
import PauseDialog from '../../components/Dialogs/PauseDialog/PauseDialog';
import WinDialog from '../../components/Dialogs/WinDialog/WinDialog';
import ConfirmDialog from '../../components/Dialogs/ConfirmDialog/ConfirmDialog';
import SettingsDialog from '../../components/Dialogs/SettingsDialog/SettingsDialog';
import InstructionsDialog from '../../components/Dialogs/InstructionsDialog/InstructionsDialog';
import AboutDialog from '../../components/Dialogs/AboutDialog/AboutDialog';
import GameOverDialog from '../../components/Dialogs/GameOverDialog/GameOverDialog';
import { gameService, authService } from '../../services/authService';
import { getApiBaseUrl } from '../../services/httpClient';
import { useGameHistory } from '../../hooks/useGameHistory';
import { useGameDialogs } from '../../hooks/useGameDialogs';
import { useGameBoard } from '../../hooks/useGameBoard';
import { useGameTimer } from '../../hooks/useGameTimer';
import { useGameKeyboardInput } from '../../hooks/useGameKeyboardInput';
import { COLOR_PROFILES, DEFAULT_SETTINGS } from '../../constants/gameConstants.js';
import { generateAllCandidates } from '../../utils/sudokuLogic.js';
import { calculateInvalidCells, findAllConflictingCells } from '../../utils/validationHelpers.js';
import './GamePage.css';

const GamePage = forwardRef(({ difficulty, savedGameData, onNewGame, onLogout, onServerError, colorProfile: appColorProfile, setColorProfile: setAppColorProfile, buildPreferencesJson, getUserSettingsForGame, applyPreferencesFromResponse }, ref) => {
  const {
    board,
    setBoard,
    originalBoard,
    setOriginalBoard,
    candidates,
    setCandidates,
    selectedCell,
    setSelectedCell,
    isCandidateMode,
    setIsCandidateMode,
    isAutoCandidateMode,
    setIsAutoCandidateMode,
    autoCandidateModeUsed,
    setAutoCandidateModeUsed,
    resetBoard,
  } = useGameBoard();

  const { dialogs, openDialog, closeDialog, toggleDialog, closeAllDialogs, anyDialogOpen } = useGameDialogs(ref);

  const [sessionId, setSessionId] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState(null);
  const [isGameWon, setIsGameWon] = useState(false);
  
  const {
    elapsedTime,
    setElapsedTime,
    isPaused,
    setIsPaused,
    elapsedTimeRef,
    pauseStartTimeRef,
    formatElapsedTime,
    resetTimer,
  } = useGameTimer();

  const [winTime, setWinTime] = useState(null);
  const [winScore, setWinScore] = useState(null);
  const [invalidCells, setInvalidCells] = useState(new Set());

  const [winMessage, setWinMessage] = useState(null);
  const [settings, setSettings] = useState(() => {
    const prefs = getUserSettingsForGame ? getUserSettingsForGame() : {};
    return { ...DEFAULT_SETTINGS, ...prefs };
  });
  const [errorCount, setErrorCount] = useState(0);
  const [errorCell, setErrorCell] = useState(null);
  const [colorProfile, setColorProfileLocal] = useState(appColorProfile || 'orange');

  const setColorProfile = useCallback((newProfile) => {
    setColorProfileLocal(newProfile);
    if (setAppColorProfile) setAppColorProfile(newProfile);
  }, [setAppColorProfile]);

  const { saveToHistory, undo, resetHistory, canUndo } = useGameHistory([], {});

  const gameInitializedRef = useRef(false);
  const completionSentRef = useRef(false);
  const pendingTimersRef = useRef([]);
  const pendingMoveRef = useRef(false);
  const pendingCheckAnswerRef = useRef(false);

  const handleGameWin = (winScore, delayMs = 1500) => {
    if (isGameWon) {
      return;
    }

    if (completionSentRef.current) {
      return;
    }

    setIsGameWon(true);
    setWinTime(winScore);
    setWinMessage('You solved the puzzle!');
    
    const dialogTimer = setTimeout(() => {
      openDialog('win');
    }, delayMs);
    pendingTimersRef.current.push(dialogTimer);
    
    const completionTimer = setTimeout(() => {
      completionSentRef.current = true;
      
      const mistakesCount = (settings.errorIndicator || settings.threeMistakeLimit) ? errorCount : 0;
      const completionData = {
        elapsedTime: winScore,
        mistakes: mistakesCount,
        autoCandidateMode: autoCandidateModeUsed
      };
      
      gameService.completeGame(sessionId, completionData)
        .then((response) => {
          if (response && response.score) {
            setWinScore(response.score);
          }
          if (response && response.elapsedTime) {
            setWinTime(response.elapsedTime);
          }
        })
        .catch((err) => {
        });
    }, 200);
    pendingTimersRef.current.push(completionTimer);
  };

  const initializeGame = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const gameData = await gameService.createGame(difficulty);

      if (!gameData) {
        throw new Error('No game data received from API');
      }

      const boardString = gameData.boardString;
      if (!boardString || typeof boardString !== 'string') {
        throw new Error('Invalid puzzle data from API');
      }

      setSessionId(gameData.sessionId);

      const boardArray = gameData.boardString
        .split('')
        .map((num) => (num === '0' ? '' : num));
      const boardGrid = [];
      for (let i = 0; i < 9; i++) {
        boardGrid.push(boardArray.slice(i * 9, (i + 1) * 9));
      }

      setBoard(boardGrid);
      setOriginalBoard(JSON.parse(JSON.stringify(boardGrid)));
      
      const candMap = gameData.candidates || {};
      const normalizedCandidates = {};
      boardGrid.forEach((row, i) => {
        row.forEach((cell, j) => {
          const key = `${i}-${j}`;
          const cellIndex = i * 9 + j;
          const cellIndexStr = String(cellIndex);
          if (!cell) {
            const candFromAPI = candMap[cellIndexStr] || candMap[cellIndex] || [];
            normalizedCandidates[key] = Array.isArray(candFromAPI) ? [...candFromAPI] : [];
          }
        });
      });
      
      setCandidates(normalizedCandidates);
      resetHistory(boardGrid, normalizedCandidates);
      setIsLoading(false);
    } catch (err) {
      
      const isNetworkError = err.message?.includes('Failed to fetch') || 
                            err.message?.includes('Server is not available') ||
                            err.message?.includes('timeout');
      const isServerError = err.response?.status >= 500 || err.message?.includes('[5');
      
      if (isNetworkError || isServerError) {
        if (onServerError) {
          onServerError(err);
        }
      } else {
        setError(`Failed to load puzzle: ${err.message}`);
      }
      
      setIsLoading(false);
    }
  };

  const loadSavedGame = (data) => {
    try {
      setIsLoading(true);
      setError(null);

      setSessionId(data.sessionId);

      const boardArray = data.boardString
        .split('')
        .map((num) => (num === '0' ? '' : num));
      const boardGrid = [];
      for (let i = 0; i < 9; i++) {
        boardGrid.push(boardArray.slice(i * 9, (i + 1) * 9));
      }

      const cluesArray = data.cluesString
        .split('')
        .map((num) => (num === '0' ? '' : num));
      const cluesGrid = [];
      for (let i = 0; i < 9; i++) {
        cluesGrid.push(cluesArray.slice(i * 9, (i + 1) * 9));
      }
      setOriginalBoard(cluesGrid);

      let parsedCandidates = {};
      if (data.candidatesJson) {
        try {
          const raw = typeof data.candidatesJson === 'string'
            ? JSON.parse(data.candidatesJson)
            : data.candidatesJson;
          const firstKey = Object.keys(raw)[0];
          if (firstKey && firstKey.includes('-')) {
            parsedCandidates = raw;
          } else {
            boardGrid.forEach((row, i) => {
              row.forEach((cell, j) => {
                if (!cell) {
                  const cellIndex = i * 9 + j;
                  const key = `${i}-${j}`;
                  parsedCandidates[key] = raw[String(cellIndex)] || [];
                }
              });
            });
          }
        } catch (e) {
          parsedCandidates = {};
        }
      }

      setElapsedTime(data.elapsedTimeSeconds || 0);
      elapsedTimeRef.current = data.elapsedTimeSeconds || 0;

      setErrorCount(data.errorCount || 0);

      setAutoCandidateModeUsed(data.autoCandidateModeUsed || false);
      setIsAutoCandidateMode(data.isAutoCandidateMode || false);

      if (data.settingsJson) {
        try {
          const parsedSettings = JSON.parse(data.settingsJson);
          const prefs = getUserSettingsForGame ? getUserSettingsForGame() : {};
          setSettings({ ...DEFAULT_SETTINGS, ...parsedSettings, ...prefs });
        } catch (e) {
        }
      }

      setBoard(boardGrid);
      setCandidates(parsedCandidates);
      const recalculatedInvalid = calculateInvalidCells(boardGrid);
      setInvalidCells(recalculatedInvalid);

      resetHistory(boardGrid, parsedCandidates);

      const loadedErrorCount = data.errorCount || 0;
      let loadedSettings = null;
      if (data.settingsJson) {
        try { loadedSettings = JSON.parse(data.settingsJson); } catch (e) { /* ignore */ }
      }
      if (loadedSettings?.threeMistakeLimit && loadedErrorCount >= 3) {
        setTimeout(() => {
          openDialog('gameover');
        }, 800);
      }

      setIsLoading(false);
    } catch (err) {
      setError(`Failed to load saved game: ${err.message}`);
      setIsLoading(false);
    }
  };

  const boardToString = (boardGrid) => {
    return boardGrid.map(row => row.map(cell => cell || '0').join('')).join('');
  };

  const saveCurrentGameState = useCallback(async () => {
    if (!sessionId || isGameWon) return;

    try {
      await gameService.saveGameState(sessionId, {
        elapsedTimeSeconds: elapsedTimeRef.current,
        errorCount: errorCount,
        autoCandidateModeUsed: autoCandidateModeUsed,
        isAutoCandidateMode: isAutoCandidateMode,
        candidatesJson: JSON.stringify(candidates),
        boardString: boardToString(board),
        settingsJson: JSON.stringify(settings),
      });
    } catch (err) {
    }
  }, [sessionId, isGameWon, candidates, board, errorCount, autoCandidateModeUsed, isAutoCandidateMode, settings]);

  useEffect(() => {
    if (gameInitializedRef.current) return;
    gameInitializedRef.current = true;
    
    if (savedGameData) {
      loadSavedGame(savedGameData);
    } else {
      initializeGame();
    }
  }, [difficulty]);

  useEffect(() => {
    const handleBeforeUnload = () => {
      if (sessionId && !isGameWon) {
        const token = localStorage.getItem('token');
        if (token) {
          const apiUrl = getApiBaseUrl();
          const data = JSON.stringify({
            elapsedTimeSeconds: elapsedTimeRef.current,
            errorCount: errorCount,
            autoCandidateModeUsed: autoCandidateModeUsed,
            isAutoCandidateMode: isAutoCandidateMode,
            candidatesJson: JSON.stringify(candidates),
            boardString: boardToString(board),
            settingsJson: JSON.stringify(settings),
          });

          fetch(`${apiUrl}/game/${sessionId}/save`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${token}`
            },
            body: data,
            keepalive: true
          }).catch(() => {});
        }
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [sessionId, isGameWon, errorCount, autoCandidateModeUsed, isAutoCandidateMode, candidates, board, settings]);

  const saveGameRef = useRef(saveCurrentGameState);
  useEffect(() => { saveGameRef.current = saveCurrentGameState; }, [saveCurrentGameState]);

  useEffect(() => {
    if (!sessionId || isGameWon) return;

    const autoSaveInterval = setInterval(() => {
      saveGameRef.current();
    }, 30000);

    return () => clearInterval(autoSaveInterval);
  }, [sessionId, isGameWon]);

  useEffect(() => {
    return () => {
      pendingTimersRef.current.forEach(timerId => clearTimeout(timerId));
      pendingTimersRef.current = [];
    };
  }, []);

  useEffect(() => {
    const handleTokenExpired = () => {
      saveGameRef.current();
    };
    window.addEventListener('tokenExpired', handleTokenExpired);
    return () => window.removeEventListener('tokenExpired', handleTokenExpired);
  }, []);

  useEffect(() => {
    if (anyDialogOpen && !isPaused && !isGameWon) {
      pauseStartTimeRef.current = Date.now();
      setIsPaused(true);
    } else if (!anyDialogOpen && isPaused && !isGameWon) {
      pauseStartTimeRef.current = null;
      setIsPaused(false);
    }
  }, [anyDialogOpen, isPaused, isGameWon]);

  const popStateInitRef = useRef(false);

  useEffect(() => {
    const handlePopState = (event) => {
      if (!dialogs.exit) {
        event.preventDefault();
        openDialog('exit');
        window.history.pushState({ type: 'game-pause' }, '', window.location.href);
      }
    };

    if (!popStateInitRef.current) {
      popStateInitRef.current = true;
      window.history.pushState({ type: 'game' }, '', window.location.href);
    }
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, [dialogs.exit, openDialog]);



  const handleCellClick = (row, col) => {
    if (isGameWon || anyDialogOpen) return;
    
    setSelectedCell({ row, col });
  };

  const handleNumberClick = async (number, isCandidate) => {
    if (!selectedCell || !sessionId || isGameWon || anyDialogOpen) return;

    if (pendingMoveRef.current) {
      return;
    }

    const { row, col } = selectedCell;
    const key = `${row}-${col}`;

    if (originalBoard[row]?.[col]) {
      return;
    }

    if (isCandidate) {
      const currentValue = board[row][col];
      const currentCandidates = candidates[key] || [];
      let newBoard = board.map(r => [...r]);
      let newCandidates;
      
      if (currentValue) {
        newBoard[row][col] = '';
        newCandidates = [number].sort((a, b) => a - b);
        if (sessionId) {
          gameService.makeMove(sessionId, row, col, 0).catch(() => {});
        }
      } else {
        if (currentCandidates.includes(number)) {
          newCandidates = currentCandidates.filter(n => n !== number);
        } else {
          newCandidates = [...currentCandidates, number].sort((a, b) => a - b);
        }
      }

      const updatedCandidates = { ...candidates, [key]: newCandidates };
      setBoard(newBoard);
      setCandidates(updatedCandidates);

      if (currentValue) {
        const recalculatedInvalid = calculateInvalidCells(newBoard);
        setInvalidCells(recalculatedInvalid);
      }

      saveToHistory(newBoard, updatedCandidates, selectedCell, isAutoCandidateMode);
    } else {
      pendingMoveRef.current = true;
      try {
        const newNumber = String(number);
      
        if (settings.errorIndicator || settings.threeMistakeLimit) {
          const currentCellValue = board[row][col];
          if (String(number) !== String(currentCellValue)) {
            if (!pendingCheckAnswerRef.current) {
              try {
                pendingCheckAnswerRef.current = true;
                const result = await gameService.checkAnswer(sessionId, row, col, number);
                if (!result.correct) {
                  setErrorCount(prev => {
                    const newCount = prev + 1;
                    if (settings.threeMistakeLimit && newCount >= 3) {
                      setTimeout(() => {
                        openDialog('gameover');
                      }, 600);
                    }
                    return newCount;
                  });
                  setErrorCell({ row, col });
                  setTimeout(() => setErrorCell(null), 600);
                }
              } catch (err) {
              } finally {
                pendingCheckAnswerRef.current = false;
              }
            }
          }
        }
      
        const conflictingCells = findAllConflictingCells(board, row, col, number);
        if (conflictingCells.size > 0) {
          const newBoard = board.map(r => [...r]);
          newBoard[row][col] = newNumber;
          const recalculatedInvalid = calculateInvalidCells(newBoard);
          setInvalidCells(recalculatedInvalid);
          setBoard(newBoard);

          let updatedCandidates = candidates;
          if (isAutoCandidateMode) {
            updatedCandidates = generateAllCandidates(newBoard);
            setCandidates(updatedCandidates);
          }

          saveToHistory(newBoard, updatedCandidates, selectedCell, isAutoCandidateMode);

          gameService.makeMove(sessionId, row, col, number).catch(() => {});
          return;
        }

        const newBoard = board.map(r => [...r]);
        newBoard[row][col] = newNumber;
        setBoard(newBoard);

        let updatedCandidates = candidates;
        if (isAutoCandidateMode) {
          updatedCandidates = generateAllCandidates(newBoard);
        }
        setCandidates(updatedCandidates);

        setInvalidCells(new Set());

        saveToHistory(newBoard, updatedCandidates, selectedCell, isAutoCandidateMode);

        const response = await gameService.makeMove(sessionId, row, col, number);
        if (response?.completionStatus === 'COMPLETED') {
          const winScore = elapsedTimeRef.current;
          handleGameWin(winScore, 1500);
          return;
        }
      } catch (err) {
        setMessage('❌ Error: ' + err.message);
        const messageTimer = setTimeout(() => setMessage(null), 3000);
        pendingTimersRef.current.push(messageTimer);
      } finally {
        pendingMoveRef.current = false;
      }
    }
  };

  const handleClear = () => {
    if (!selectedCell || !sessionId || isGameWon || anyDialogOpen) return;
    if (pendingMoveRef.current) return;

    const { row, col } = selectedCell;
    const key = `${row}-${col}`;

    if (!originalBoard[row]?.[col]) {
      const valueToRemove = board[row][col];
      const newBoard = board.map(r => [...r]);
      newBoard[row][col] = '';
      setBoard(newBoard);

      let updatedCandidates = {};
      if (isAutoCandidateMode && valueToRemove) {
        updatedCandidates = generateAllCandidates(newBoard);
      } else {
        updatedCandidates = { ...candidates };
        updatedCandidates[key] = [];
      }
      
      setCandidates(updatedCandidates);

      const recalculatedInvalid = calculateInvalidCells(newBoard);
      setInvalidCells(recalculatedInvalid);

      saveToHistory(newBoard, updatedCandidates, selectedCell, isAutoCandidateMode);

      if (valueToRemove && sessionId) {
        gameService.makeMove(sessionId, row, col, 0).catch(() => {});
      }
    }
  };

  const handleUndo = () => {
    if (isGameWon || anyDialogOpen) return;
    
    if (pendingMoveRef.current) {
      return;
    }
    
    if (!canUndo()) return;

    const restoredState = undo();
    
    if (!restoredState) return;

    setBoard(restoredState.board);
    setCandidates(restoredState.candidates);
    setSelectedCell(restoredState.selectedCell);
    setIsAutoCandidateMode(restoredState.isAutoCandidateMode);
    
    const recalculatedInvalid = calculateInvalidCells(restoredState.board);
    setInvalidCells(recalculatedInvalid);
  };

  const handleCandidateModeToggle = useCallback(() => {
    setIsCandidateMode(prev => !prev);
  }, []);

  const handleToggleAutoCandidateMode = () => {
    if (!board || board.length === 0 || !board[0]) {
      return;
    }
    
    const newAutoCandidateMode = !isAutoCandidateMode;
    let newCandidates = {};

    if (newAutoCandidateMode) {
      newCandidates = generateAllCandidates(board);
      setAutoCandidateModeUsed(true);
    } else {
      newCandidates = {};
    }

    setCandidates(newCandidates);
    setIsAutoCandidateMode(newAutoCandidateMode);
    saveToHistory(board, newCandidates, selectedCell, newAutoCandidateMode);
  };

  const handleRestart = async () => {
    const currentSessionId = sessionId;
    resetGameState();
    if (currentSessionId) {
      try {
        await gameService.abandonGame(currentSessionId);
      } catch (e) { }
    }
    setTimeout(() => {
      initializeGame();
    }, 100);
  };

  const resetGameState = () => {
    pendingTimersRef.current.forEach(timerId => clearTimeout(timerId));
    pendingTimersRef.current = [];
    resetBoard();
    setSessionId(null);
    setIsGameWon(false);
    resetTimer();
    setWinTime(null);
    setWinScore(null);
    setErrorCount(0);
    setErrorCell(null);
    const prefs = getUserSettingsForGame ? getUserSettingsForGame() : {};
    setSettings({ ...DEFAULT_SETTINGS, ...prefs });
    resetHistory([], {});
    setInvalidCells(new Set());
    setMessage(null);
    setError(null);
    completionSentRef.current = false;
    pendingMoveRef.current = false;
    pendingCheckAnswerRef.current = false;
  };

  const handleWinDialogClose = () => {
    closeDialog('win');
    setWinMessage(null);
    onNewGame();
  };

  const handleWinDialogNewGame = () => {
    closeDialog('win');
    setWinMessage(null);
    resetGameState();
    setTimeout(() => {
      initializeGame();
    }, 100);
  };

  const handleGameOverNewGame = async () => {
    const currentSessionId = sessionId;
    closeDialog('gameover');
    resetGameState();
    if (currentSessionId) {
      try {
        await gameService.abandonGame(currentSessionId);
      } catch (e) { }
    }
    setTimeout(() => {
      initializeGame();
    }, 100);
  };

  const handleGameOverExit = async () => {
    const currentSessionId = sessionId;
    closeDialog('gameover');
    if (currentSessionId) {
      try {
        await gameService.abandonGame(currentSessionId);
      } catch (e) { }
    }
    onNewGame();
  };

  const handlePauseClick = () => {
    if (dialogs.pause) closeDialog('pause');
    else openDialog('pause');
  };

  const handleResumePause = () => {
    closeDialog('pause');
  };

  const handleSettingsClick = () => {
    openDialog('settings');
  };

  const handleSettingsClose = () => {
    closeDialog('settings');
    if (buildPreferencesJson) {
      const prefsJson = buildPreferencesJson({
        colorProfile,
        fontSize: settings.fontSize,
        highlightConflicts: settings.highlightConflicts,
        highlightRowColumn: settings.highlightRowColumn,
        highlightBox: settings.highlightBox,
        highlightIdenticalNumbers: settings.highlightIdenticalNumbers,
      });
      authService.savePreferences(prefsJson).then(() => {
        if (applyPreferencesFromResponse) applyPreferencesFromResponse(prefsJson);
      }).catch(() => {});
    }
  };

  const handleSettingsChange = (newSettings) => {
    setSettings(newSettings);

    if (newSettings.threeMistakeLimit && !settings.threeMistakeLimit && errorCount >= 3) {
      closeDialog('settings');
      setTimeout(() => {
        openDialog('gameover');
      }, 400);
    }
  };

  const handleInfo = () => {
    openDialog('instructions');
  };

  const handleBackClick = () => {
    openDialog('exit');
  };

  const handleExitConfirm = async () => {
    closeDialog('exit');
    await saveCurrentGameState();
    onNewGame();
  };

  const handleExitCancel = () => {
    closeDialog('exit');
  };

  const handleLogoutClick = () => {
    openDialog('logout');
  };

  const handleLogoutConfirm = async () => {
    closeDialog('logout');
    await saveCurrentGameState();
    onLogout();
  };

  const handleLogoutCancel = () => {
    closeDialog('logout');
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

  useGameKeyboardInput({
    selectedCell,
    sessionId,
    isGameWon,
    anyDialogOpen,
    isCandidateMode,
    dialogs,
    onNumberClick: handleNumberClick,
    onClear: handleClear,
    onUndo: handleUndo,
    onRestart: handleRestart,
    onDialogToggle: toggleDialog,
    onDialogOpen: openDialog,
    onDialogClose: closeDialog,
    onDialogCloseAll: closeAllDialogs,
    onPauseClick: handlePauseClick,
    onCellNavigate: (newRow, newCol) => setSelectedCell({ row: newRow, col: newCol }),
    onCandidateModeToggle: handleCandidateModeToggle,
    onExitConfirm: handleExitConfirm,
    onLogoutConfirm: handleLogoutConfirm,
  });

  if (isLoading) {
    return (
      <Backdrop
        sx={{
          color: '#fff',
          zIndex: 1300,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 2,
        }}
        open={isLoading}
      >
        <CircularProgress color="inherit" size={60} />
        <p style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>Generating puzzle...</p>
      </Backdrop>
    );
  }

  if (error) {
    return (
      <div className="game-page__error">
        <p>{error}</p>
        <button className="game-page__retry" onClick={() => window.location.reload()}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="game-page">
          <div className="game-page__header">
        <GameTitle colorProfile={COLOR_PROFILES[colorProfile]} />
        {message && <p className="game-page__message">{message}</p>}
        <GameInfo 
          difficulty={difficulty} 
          onPause={handlePauseClick} 
          onNewGame={handleBackClick}
          onRestart={handleRestart}
          onSettings={handleSettingsClick}
          onInfo={handleInfo}
          onLogoutClick={handleLogoutClick}
          elapsedTime={elapsedTime}
          onElapsedTimeChange={setElapsedTime}
          isPaused={isPaused}
          isGameWon={isGameWon}
          errorCount={errorCount}
          showErrorIndicator={settings.errorIndicator}
          threeMistakeLimit={settings.threeMistakeLimit}
          colorProfile={COLOR_PROFILES[colorProfile]}
        />
      </div>

      <div className="game-page__timer-display">
        {formatElapsedTime(elapsedTime)}
      </div>

      <div className="game-page__mobile-info-wrapper">
        <div className="game-page__pause-button" style={{ '--color-intensive': COLOR_PROFILES[colorProfile].intensive, '--color-light': COLOR_PROFILES[colorProfile].light }}>
          <div className="game-page__pause-controls">
            <span className="game-page__pause-timer">
              {formatElapsedTime(elapsedTime)}
            </span>
            <IconButton
              onClick={handlePauseClick}
              size="large"
              sx={{ 
                color: '#ffa500', 
                padding: '0.5rem',
                '&:hover': {
                  backgroundColor: 'rgba(255, 165, 0, 0.1)',
                },
              }}
            >
              <PauseCircleOutlineIcon sx={{ fontSize: '2rem' }} />
            </IconButton>
            <span className="game-page__pause-difficulty">
              {difficulty}
            </span>
          </div>
        </div>

        {(settings.errorIndicator || settings.threeMistakeLimit) && (
          <div className="game-page__mobile-mistake-counter">
            <span className="game-page__mobile-mistake-label">MISTAKES</span>
            <span className="game-page__mobile-mistake-value">{settings.threeMistakeLimit ? `${errorCount}/3` : errorCount}</span>
          </div>
        )}
      </div>

      <div className="game-page__content">
        <Board
          board={board}
          originalBoard={originalBoard}
          candidates={candidates}
          selectedCell={selectedCell}
          isCandidateMode={isCandidateMode}
          onCellClick={handleCellClick}
          invalidCells={invalidCells}
          isWon={isGameWon}
          settings={settings}
          colorProfile={COLOR_PROFILES[colorProfile]}
          errorCell={errorCell}
          fontSize={settings.fontSize}
        />

        <NumberPad
          onNumberClick={handleNumberClick}
          onClear={handleClear}
          onUndo={handleUndo}
          isCandidateMode={isCandidateMode}
          onToggleMode={handleCandidateModeToggle}
          isAutoCandidateMode={isAutoCandidateMode}
          onToggleAutoCandidateMode={handleToggleAutoCandidateMode}
        />
      </div>



      <PauseDialog
        open={dialogs.pause}
        onResume={handleResumePause}
      />

      <WinDialog
        open={dialogs.win}
        onClose={handleWinDialogClose}
        onNewGame={handleWinDialogNewGame}
        difficulty={difficulty}
        elapsedTime={winTime || elapsedTime}
        score={winScore}
        message={winMessage}
        errorCount={errorCount}
        showMistakes={settings.errorIndicator || settings.threeMistakeLimit}
        threeMistakeLimit={settings.threeMistakeLimit}
        autoCandidateModeUsed={autoCandidateModeUsed}
        colorProfile={COLOR_PROFILES[colorProfile]}
      />

      <GameOverDialog
        open={dialogs.gameover}
        onNewGame={handleGameOverNewGame}
        onExit={handleGameOverExit}
        colorProfile={COLOR_PROFILES[colorProfile]}
      />

      <ConfirmDialog
        open={dialogs.exit}
        onCancel={handleExitCancel}
        onConfirm={handleExitConfirm}
        title="Exit to main menu?"
        message="Your game will be saved for up to 7 days. You can continue later."
      />

      <ConfirmDialog
        open={dialogs.logout}
        onCancel={handleLogoutCancel}
        onConfirm={handleLogoutConfirm}
        title="Logout?"
        message="Your game will be saved for up to 7 days."
        confirmText="Yes, logout"
      />

      <SettingsDialog
        open={dialogs.settings}
        onClose={handleSettingsClose}
        settings={settings}
        onSettingsChange={handleSettingsChange}
        isAutoCandidateMode={isAutoCandidateMode}
        onToggleAutoCandidateMode={handleToggleAutoCandidateMode}
        colorProfile={colorProfile}
        onColorProfileChange={setColorProfile}
        colorProfiles={COLOR_PROFILES}
        onDeleteAccount={handleDeleteAccount}
      />

      <InstructionsDialog
        open={dialogs.instructions}
        onClose={() => closeDialog('instructions')}
        onAboutClick={() => openDialog('about')}
        colorProfile={colorProfile}
        colorProfiles={COLOR_PROFILES}
      />

      <AboutDialog
        open={dialogs.about}
        onClose={() => closeDialog('about')}
        colorProfile={colorProfile}
        colorProfiles={COLOR_PROFILES}
      />
    </div>
  );
});

GamePage.displayName = 'GamePage';

export default GamePage;
