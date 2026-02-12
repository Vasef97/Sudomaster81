import { useState } from 'react';

export const useAppGame = (onAuthStateChange) => {
  const [gameStarted, setGameStarted] = useState(false);
  const [difficulty, setDifficulty] = useState(null);

  const handleStartGame = (level) => {
    setDifficulty(level);
    setGameStarted(true);
    if (onAuthStateChange) {
      onAuthStateChange('game');
    }
  };

  const handleNewGame = () => {
    setGameStarted(false);
    setDifficulty(null);
    if (onAuthStateChange) {
      onAuthStateChange('menu');
    }
  };

  const resetGameState = () => {
    setGameStarted(false);
    setDifficulty(null);
  };

  return {
    gameStarted,
    difficulty,
    handleStartGame,
    handleNewGame,
    resetGameState,
  };
};
