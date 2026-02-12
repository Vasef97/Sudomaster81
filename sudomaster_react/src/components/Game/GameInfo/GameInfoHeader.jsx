import React, { useEffect, useRef } from 'react';
import { formatTime } from '../../../utils/formatters';
import './GameInfoHeader.css';

const GameInfoHeader = ({
  difficulty,
  elapsedTime,
  onElapsedTimeChange,
  isPaused,
  isGameWon,
  errorCount = 0,
  showErrorIndicator = false,
  threeMistakeLimit = false,
}) => {
  const timerRef = useRef(null);
  const onElapsedTimeChangeRef = useRef(onElapsedTimeChange);

  useEffect(() => {
    onElapsedTimeChangeRef.current = onElapsedTimeChange;
  }, [onElapsedTimeChange]);

  useEffect(() => {
    if (isPaused || isGameWon) {
      if (timerRef.current !== null) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
      return;
    }

    timerRef.current = setInterval(() => {
      onElapsedTimeChangeRef.current?.(prev => prev + 1);
    }, 1000);

    return () => {
      if (timerRef.current !== null) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
    };
  }, [isPaused, isGameWon]);

  return (
    <div className="game-info-header">
      <div className="game-info-header__item">
        <span className="game-info-header__label">Difficulty</span>
        <span className="game-info-header__value">{difficulty}</span>
      </div>

      <div className="game-info-header__item">
        <span className="game-info-header__label">Time</span>
        <span className="game-info-header__value game-info-header__timer">
          {formatTime(elapsedTime)}
        </span>
      </div>

      {(showErrorIndicator || threeMistakeLimit) && (
        <div className="game-info-header__item game-info-header__error-indicator">
          <span className="game-info-header__label">MISTAKES</span>
          <span className="game-info-header__value game-info-header__error-count">
            {threeMistakeLimit ? `${errorCount}/3` : errorCount}
          </span>
        </div>
      )}
    </div>
  );
};

export default GameInfoHeader;
