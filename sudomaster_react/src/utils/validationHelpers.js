export const calculateInvalidCells = (boardState) => {
  const invalid = new Set();
  
  if (!boardState || boardState.length === 0) {
    return invalid;
  }
  
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      const cellValue = boardState[row]?.[col];
      
      if (cellValue) {
        const number = parseInt(cellValue);
        
        for (let c = 0; c < 9; c++) {
          if (c !== col && boardState[row]?.[c] === String(number)) {
            invalid.add(`${row}-${col}`);
            invalid.add(`${row}-${c}`);
          }
        }
        

        for (let r = 0; r < 9; r++) {
          if (r !== row && boardState[r]?.[col] === String(number)) {
            invalid.add(`${row}-${col}`);
            invalid.add(`${r}-${col}`);
          }
        }
        
        const boxRow = Math.floor(row / 3) * 3;
        const boxCol = Math.floor(col / 3) * 3;
        for (let r = boxRow; r < boxRow + 3; r++) {
          for (let c = boxCol; c < boxCol + 3; c++) {
            if ((r !== row || c !== col) && boardState[r]?.[c] === String(number)) {
              invalid.add(`${row}-${col}`);
              invalid.add(`${r}-${c}`);
            }
          }
        }
      }
    }
  }
  
  return invalid;
};

export const findAllConflictingCells = (board, row, col, number) => {
  const conflicts = new Set();
  
  for (let c = 0; c < 9; c++) {
    if (c !== col && board[row]?.[c] === String(number)) {
      conflicts.add(`${row}-${c}`);
    }
  }
  
  for (let r = 0; r < 9; r++) {
    if (r !== row && board[r]?.[col] === String(number)) {
      conflicts.add(`${r}-${col}`);
    }
  }
  
  const boxRow = Math.floor(row / 3) * 3;
  const boxCol = Math.floor(col / 3) * 3;
  for (let r = boxRow; r < boxRow + 3; r++) {
    for (let c = boxCol; c < boxCol + 3; c++) {
      if ((r !== row || c !== col) && board[r]?.[c] === String(number)) {
        conflicts.add(`${r}-${c}`);
      }
    }
  }
  
  return conflicts;
};
