import React from 'react';
import Cell from '../Cell/Cell';
import './Board.css';

const Board = ({
  board,
  originalBoard,
  candidates,
  selectedCell,
  isCandidateMode,
  onCellClick,
  invalidCells,
  isWon,
  settings = {
    highlightConflicts: true,
    highlightRowColumn: true,
    highlightBox: true,
    highlightIdenticalNumbers: true,
  },
  colorProfile,
  errorCell,
  fontSize,
}) => {
  const getHighlightedCells = (row, col) => {
    const highlighted = new Set();

    if (settings.highlightRowColumn) {
      for (let c = 0; c < 9; c++) {
        highlighted.add(`${row}-${c}`);
      }

      for (let r = 0; r < 9; r++) {
        highlighted.add(`${r}-${col}`);
      }
    }

    if (settings.highlightBox) {
      const boxRow = Math.floor(row / 3) * 3;
      const boxCol = Math.floor(col / 3) * 3;
      for (let r = boxRow; r < boxRow + 3; r++) {
        for (let c = boxCol; c < boxCol + 3; c++) {
          highlighted.add(`${r}-${c}`);
        }
      }
    }

    return highlighted;
  };

  const getValueMatchedCells = (selectedValue) => {
    const matched = new Set();
    if (!selectedValue || !settings.highlightIdenticalNumbers) return matched;
    
    for (let r = 0; r < 9; r++) {
      for (let c = 0; c < 9; c++) {
        if (board[r][c] === selectedValue && !(r === selectedCell?.row && c === selectedCell?.col)) {
          matched.add(`${r}-${c}`);
        }
      }
    }
    return matched;
  };

  const highlightedCells = selectedCell
    ? getHighlightedCells(selectedCell.row, selectedCell.col)
    : new Set();

  const selectedValue = selectedCell ? board[selectedCell.row]?.[selectedCell.col] : null;
  const valueMatchedCells = selectedValue ? getValueMatchedCells(selectedValue) : new Set();

  return (
    <div className={`board ${isWon ? 'board--won' : ''}`}>
      {board.map((row, rowIndex) =>
        row.map((cell, colIndex) => {
          const key = `${rowIndex}-${colIndex}`;
          const isSelected = selectedCell?.row === rowIndex && selectedCell?.col === colIndex;
          const isHighlightedByLocation = highlightedCells.has(key) && !isSelected;
          const isHighlightedByValue = valueMatchedCells.has(key) && !isSelected;
          const isPrefilled = !!originalBoard[rowIndex]?.[colIndex];
          const isDuplicate = (settings.highlightConflicts && invalidCells?.has(key)) || false;
          const cellCandidates = candidates[key] || [];
          const isErrorCell = errorCell?.row === rowIndex && errorCell?.col === colIndex;

          return (
            <Cell
              key={key}
              value={cell}
              candidates={cellCandidates}
              isSelected={isSelected}
              isHighlightedByLocation={isHighlightedByLocation}
              isHighlightedByValue={isHighlightedByValue}
              isPrefilled={isPrefilled}
              isCandidateMode={isCandidateMode}
              isDuplicate={isDuplicate}
              isErrorCell={isErrorCell}
              onClick={() => onCellClick(rowIndex, colIndex)}
              colorProfile={colorProfile}
              fontSize={fontSize}
            />
          );
        })
      )}
    </div>
  );
};

export default Board;
