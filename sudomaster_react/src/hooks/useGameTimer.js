import { useState, useRef, useEffect, useCallback } from 'react';
import { formatTime } from '../utils/formatters';

export const useGameTimer = () => {
  const [elapsedTime, setElapsedTime] = useState(0);
  const [isPaused, setIsPaused] = useState(false);

  const elapsedTimeRef = useRef(0);
  useEffect(() => {
    elapsedTimeRef.current = elapsedTime;
  }, [elapsedTime]);

  const pauseStartTimeRef = useRef(null);

  const formatElapsedTime = useCallback((seconds) => {
    return formatTime(seconds);
  }, []);

  const resetTimer = useCallback(() => {
    setElapsedTime(0);
    setIsPaused(false);
    elapsedTimeRef.current = 0;
    pauseStartTimeRef.current = null;
  }, []);

  return {
    elapsedTime,
    setElapsedTime,
    isPaused,
    setIsPaused,
    elapsedTimeRef,
    pauseStartTimeRef,
    formatElapsedTime,
    resetTimer,
  };
};
