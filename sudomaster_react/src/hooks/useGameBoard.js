import { useState, useCallback } from 'react';

export const useGameBoard = () => {
  const [board, setBoard] = useState([]);
  const [originalBoard, setOriginalBoard] = useState([]);
  const [candidates, setCandidates] = useState({});
  const [selectedCell, setSelectedCell] = useState(null);
  const [isCandidateMode, setIsCandidateMode] = useState(false);
  const [isAutoCandidateMode, setIsAutoCandidateMode] = useState(false);
  const [autoCandidateModeUsed, setAutoCandidateModeUsed] = useState(false);

  const resetBoard = useCallback(() => {
    setBoard([]);
    setOriginalBoard([]);
    setCandidates({});
    setSelectedCell(null);
    setIsCandidateMode(false);
    setIsAutoCandidateMode(false);
    setAutoCandidateModeUsed(false);
  }, []);

  return {
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
  };
};
