import { useState, useRef } from 'react';

const deepCopyBoard = (board) => board.map(row => [...row]);

const deepCopyCandidates = (candidatesObj) => {
  const copy = {};
  for (const [key, candArray] of Object.entries(candidatesObj)) {
    copy[key] = Array.isArray(candArray) ? [...candArray] : [];
  }
  return copy;
};

const computeReverseDiff = (oldBoard, oldCandidates, oldSelectedCell, oldIsAuto,
                             newBoard, newCandidates) => {
  const boardChanges = [];
  for (let r = 0; r < newBoard.length; r++) {
    for (let c = 0; c < (newBoard[r]?.length || 0); c++) {
      if (newBoard[r][c] !== oldBoard[r]?.[c]) {
        boardChanges.push({ r, c, v: oldBoard[r]?.[c] ?? 0 });
      }
    }
  }

  const candidateChanges = {};
  const allKeys = new Set([...Object.keys(oldCandidates), ...Object.keys(newCandidates)]);
  for (const key of allKeys) {
    const oldArr = oldCandidates[key] || [];
    const newArr = newCandidates[key] || [];
    if (oldArr.length !== newArr.length || oldArr.some((v, i) => v !== newArr[i])) {
      candidateChanges[key] = [...oldArr];
    }
  }

  return {
    b: boardChanges.length > 0 ? boardChanges : undefined,
    c: Object.keys(candidateChanges).length > 0 ? candidateChanges : undefined,
    s: oldSelectedCell ? { r: oldSelectedCell.row, c: oldSelectedCell.col } : undefined,
    a: oldIsAuto,
  };
};

const applyReverseDiff = (board, candidates, diff) => {
  if (diff.b) {
    for (const { r, c, v } of diff.b) {
      board[r][c] = v;
    }
  }
  if (diff.c) {
    for (const [key, arr] of Object.entries(diff.c)) {
      if (arr.length === 0) {
        delete candidates[key];
      } else {
        candidates[key] = [...arr];
      }
    }
  }
  return {
    selectedCell: diff.s ? { row: diff.s.r, col: diff.s.c } : null,
    isAutoCandidateMode: diff.a ?? false,
  };
};

export const useGameHistory = (initialBoard, initialCandidates) => {
  const currentBoardRef = useRef(initialBoard.length ? deepCopyBoard(initialBoard) : []);
  const currentCandidatesRef = useRef(deepCopyCandidates(initialCandidates || {}));
  const currentSelectedCellRef = useRef(null);
  const currentIsAutoRef = useRef(false);
  const [undoStack, setUndoStack] = useState([]);
  const undoStackRef = useRef(undoStack);
  undoStackRef.current = undoStack;

  const saveToHistory = (board, candidates, selectedCell, isAutoCandidateMode) => {
    const diff = computeReverseDiff(
      currentBoardRef.current, currentCandidatesRef.current,
      currentSelectedCellRef.current, currentIsAutoRef.current,
      board, candidates,
    );

    currentBoardRef.current = deepCopyBoard(board);
    currentCandidatesRef.current = deepCopyCandidates(candidates);
    currentSelectedCellRef.current = selectedCell ? { ...selectedCell } : null;
    currentIsAutoRef.current = isAutoCandidateMode;

    setUndoStack(prev => [...prev, diff]);
  };

  const undo = () => {
    const stack = undoStackRef.current;
    if (stack.length === 0) return null;

    const diff = stack[stack.length - 1];
    const board = deepCopyBoard(currentBoardRef.current);
    const candidates = deepCopyCandidates(currentCandidatesRef.current);
    const { selectedCell, isAutoCandidateMode } = applyReverseDiff(board, candidates, diff);

    currentBoardRef.current = board;
    currentCandidatesRef.current = candidates;
    currentSelectedCellRef.current = selectedCell;
    currentIsAutoRef.current = isAutoCandidateMode;

    setUndoStack(prev => prev.slice(0, -1));

    return {
      board: deepCopyBoard(board),
      candidates: deepCopyCandidates(candidates),
      selectedCell: selectedCell ? { ...selectedCell } : null,
      isAutoCandidateMode,
    };
  };

  const resetHistory = (board, candidates) => {
    currentBoardRef.current = deepCopyBoard(board);
    currentCandidatesRef.current = deepCopyCandidates(candidates || {});
    currentSelectedCellRef.current = null;
    currentIsAutoRef.current = false;
    setUndoStack([]);
  };

  const canUndo = () => undoStackRef.current.length > 0;

  return {
    saveToHistory,
    undo,
    resetHistory,
    canUndo,
  };
};

export default useGameHistory;
