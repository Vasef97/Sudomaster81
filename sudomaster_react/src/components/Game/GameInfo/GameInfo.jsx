import React from 'react';
import GameInfoHeader from './GameInfoHeader';
import GameInfoToolbar from './GameInfoToolbar';
import MobileGameMenu from './MobileGameMenu';
import './GameInfo.css';

const GameInfo = ({
  difficulty,
  onPause,
  onNewGame,
  onRestart,
  onSettings,
  onLogoutClick,
  onInfo,
  elapsedTime,
  onElapsedTimeChange,
  isPaused,
  isGameWon,
  errorCount = 0,
  showErrorIndicator = false,
  threeMistakeLimit = false,
  colorProfile = {},
}) => {

  return (
    <div
      className="game-info"
      style={{
        '--color-intensive': colorProfile.intensive || '#ffa500',
        '--color-light': colorProfile.light || '#f5f5dc',
      }}
    >
      <GameInfoHeader
        difficulty={difficulty}
        elapsedTime={elapsedTime}
        onElapsedTimeChange={onElapsedTimeChange}
        isPaused={isPaused}
        isGameWon={isGameWon}
        errorCount={errorCount}
        showErrorIndicator={showErrorIndicator}
        threeMistakeLimit={threeMistakeLimit}
      />

      <GameInfoToolbar
        onPause={onPause}
        onRestart={onRestart}
        onNewGame={onNewGame}
        onInfo={onInfo}
        onSettings={onSettings}
        onLogoutClick={onLogoutClick}
      />

      <MobileGameMenu
        onRestart={onRestart}
        onExit={onNewGame}
        onInfo={onInfo}
        onSettings={onSettings}
        onLogout={onLogoutClick}
        colorProfile={colorProfile}
      />
    </div>
  );
};

export default GameInfo;
