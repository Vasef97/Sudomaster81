export const calculateValidCandidates = (board, row, col) => {
  const candidates = [];
  
  if (board[row]?.[col]) {
    return candidates;
  }
  
  for (let num = 1; num <= 9; num++) {
    let isValid = true;
    const numStr = String(num);
    
    if (board[row].includes(numStr)) {
      isValid = false;
    }
    
    if (isValid) {
      for (let r = 0; r < 9; r++) {
        if (board[r][col] === numStr) {
          isValid = false;
          break;
        }
      }
    }
    
    if (isValid) {
      const boxRow = Math.floor(row / 3) * 3;
      const boxCol = Math.floor(col / 3) * 3;
      for (let r = boxRow; r < boxRow + 3; r++) {
        for (let c = boxCol; c < boxCol + 3; c++) {
          if (board[r][c] === numStr) {
            isValid = false;
            break;
          }
        }
        if (!isValid) break;
      }
    }
    
    if (isValid) {
      candidates.push(num);
    }
  }
  
  return candidates;
};

export const generateAllCandidates = (board) => {
  const candidates = {};
  
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      if (!board[row][col]) {
        const key = `${row}-${col}`;
        candidates[key] = calculateValidCandidates(board, row, col);
      }
    }
  }
  
  return candidates;
};
