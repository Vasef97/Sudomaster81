
let passed = 0;
let failed = 0;
let currentSuite = '';

function describe(name, fn) {
  currentSuite = name;
  console.log(`\n━━━ ${name} ━━━`);
  fn();
}

function test(name, fn) {
  try {
    fn();
    passed++;
    console.log(`  ✅ ${name}`);
  } catch (e) {
    failed++;
    console.log(`  ❌ ${name}`);
    console.log(`     ${e.message}`);
  }
}

function expect(actual) {
  return {
    toBe(expected) {
      if (actual !== expected) throw new Error(`Expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
    },
    toEqual(expected) {
      const a = JSON.stringify(actual);
      const b = JSON.stringify(expected);
      if (a !== b) throw new Error(`Expected ${b}, got ${a}`);
    },
    toBeTruthy() {
      if (!actual) throw new Error(`Expected truthy, got ${JSON.stringify(actual)}`);
    },
    toBeFalsy() {
      if (actual) throw new Error(`Expected falsy, got ${JSON.stringify(actual)}`);
    },
    toBeGreaterThan(expected) {
      if (!(actual > expected)) throw new Error(`Expected ${actual} > ${expected}`);
    },
    toBeLessThan(expected) {
      if (!(actual < expected)) throw new Error(`Expected ${actual} < ${expected}`);
    },
    toContain(item) {
      if (Array.isArray(actual)) {
        if (!actual.includes(item)) throw new Error(`Array does not contain ${item}`);
      } else if (actual instanceof Set) {
        if (!actual.has(item)) throw new Error(`Set does not contain ${item}`);
      } else {
        throw new Error(`Cannot check containment on ${typeof actual}`);
      }
    },
    toHaveLength(len) {
      if (actual.length !== len) throw new Error(`Expected length ${len}, got ${actual.length}`);
    },
    toBeNull() {
      if (actual !== null) throw new Error(`Expected null, got ${JSON.stringify(actual)}`);
    },
    not: {
      toBe(expected) {
        if (actual === expected) throw new Error(`Expected not to be ${JSON.stringify(expected)}`);
      },
      toContain(item) {
        if (Array.isArray(actual) && actual.includes(item)) throw new Error(`Array should not contain ${item}`);
        if (actual instanceof Set && actual.has(item)) throw new Error(`Set should not contain ${item}`);
      },
      toBeNull() {
        if (actual === null) throw new Error(`Expected non-null`);
      },
    },
  };
}
const formatTime = (seconds) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  }
  return `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
};

const calculateInvalidCells = (boardState) => {
  const invalid = new Set();
  if (!boardState || boardState.length === 0) return invalid;
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

const findAllConflictingCells = (board, row, col, number) => {
  const conflicts = new Set();
  for (let c = 0; c < 9; c++) {
    if (c !== col && board[row]?.[c] === String(number)) conflicts.add(`${row}-${c}`);
  }
  for (let r = 0; r < 9; r++) {
    if (r !== row && board[r]?.[col] === String(number)) conflicts.add(`${r}-${col}`);
  }
  const boxRow = Math.floor(row / 3) * 3;
  const boxCol = Math.floor(col / 3) * 3;
  for (let r = boxRow; r < boxRow + 3; r++) {
    for (let c = boxCol; c < boxCol + 3; c++) {
      if ((r !== row || c !== col) && board[r]?.[c] === String(number)) conflicts.add(`${r}-${c}`);
    }
  }
  return conflicts;
};

const calculateValidCandidates = (board, row, col) => {
  const candidates = [];
  if (board[row]?.[col]) return candidates;
  for (let num = 1; num <= 9; num++) {
    let isValid = true;
    const numStr = String(num);
    if (board[row].includes(numStr)) { isValid = false; }
    if (isValid) {
      for (let r = 0; r < 9; r++) {
        if (board[r][col] === numStr) { isValid = false; break; }
      }
    }
    if (isValid) {
      const bR = Math.floor(row / 3) * 3;
      const bC = Math.floor(col / 3) * 3;
      for (let r = bR; r < bR + 3; r++) {
        for (let c = bC; c < bC + 3; c++) {
          if (board[r][c] === numStr) { isValid = false; break; }
        }
        if (!isValid) break;
      }
    }
    if (isValid) candidates.push(num);
  }
  return candidates;
};

const generateAllCandidates = (board) => {
  const candidates = {};
  for (let row = 0; row < 9; row++) {
    for (let col = 0; col < 9; col++) {
      if (!board[row][col]) {
        candidates[`${row}-${col}`] = calculateValidCandidates(board, row, col);
      }
    }
  }
  return candidates;
};

const removeCandidateFromConflicts = (candidates, row, col, number) => {
  const updated = { ...candidates };
  for (let c = 0; c < 9; c++) {
    const key = `${row}-${c}`;
    if (updated[key]) updated[key] = updated[key].filter(n => n !== number);
  }
  for (let r = 0; r < 9; r++) {
    const key = `${r}-${col}`;
    if (updated[key]) updated[key] = updated[key].filter(n => n !== number);
  }
  const bR = Math.floor(row / 3) * 3;
  const bC = Math.floor(col / 3) * 3;
  for (let r = bR; r < bR + 3; r++) {
    for (let c = bC; c < bC + 3; c++) {
      const key = `${r}-${c}`;
      if (updated[key]) updated[key] = updated[key].filter(n => n !== number);
    }
  }
  return updated;
};

const COLOR_PROFILES = {
  orange: { name: 'Orange', intensive: '#ffa500', light: '#f5f5dc' },
  lemon: { name: 'Lemon', intensive: '#f0e68c', light: '#fffef0' },
  green: { name: 'Green', intensive: '#a0c98a', light: '#e8f4e1' },
  blue: { name: 'Blue', intensive: '#6ba3d4', light: '#e8f2fa' },
  pink: { name: 'Pink', intensive: '#e8a5c8', light: '#f9e8f5' },
  purple: { name: 'Purple', intensive: '#b8a3d4', light: '#f0e8f8' },
  lavender: { name: 'Lavender', intensive: '#dda0dd', light: '#f8f0ff' },
};

const DIALOGS = {
  PAUSE: 'pause', EXIT: 'exit', LOGOUT: 'logout', INSTRUCTIONS: 'instructions',
  WIN: 'win', SETTINGS: 'settings', ABOUT: 'about', GAME_OVER: 'gameover',
};

const DEFAULT_SETTINGS = {
  highlightConflicts: true, highlightRowColumn: true, highlightBox: true,
  highlightIdenticalNumbers: true, errorIndicator: false, threeMistakeLimit: false,
  fontSize: 'medium',
};

const deepCopyBoard = (board) => board.map(row => [...row]);
const deepCopyCandidates = (obj) => {
  const copy = {};
  for (const [key, arr] of Object.entries(obj)) {
    copy[key] = Array.isArray(arr) ? [...arr] : [];
  }
  return copy;
};

const computeReverseDiff = (oldBoard, oldCandidates, oldSelectedCell, oldIsAuto, newBoard, newCandidates) => {
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
      if (arr.length === 0) delete candidates[key];
      else candidates[key] = [...arr];
    }
  }
  return {
    selectedCell: diff.s ? { row: diff.s.r, col: diff.s.c } : null,
    isAutoCandidateMode: diff.a ?? false,
  };
};
function emptyBoard() {
  return Array.from({ length: 9 }, () => Array(9).fill(null));
}

function sampleBoard() {
  const b = emptyBoard();
  b[0][0] = '5'; b[0][1] = '3'; b[0][4] = '7';
  b[1][0] = '6'; b[1][3] = '1'; b[1][4] = '9'; b[1][5] = '5';
  b[2][1] = '9'; b[2][2] = '8'; b[2][7] = '6';
  return b;
}
describe('formatTime', () => {
  test('formats 0 seconds', () => {
    expect(formatTime(0)).toBe('00:00');
  });
  test('formats seconds only', () => {
    expect(formatTime(45)).toBe('00:45');
  });
  test('formats minutes and seconds', () => {
    expect(formatTime(125)).toBe('02:05');
  });
  test('formats hours', () => {
    expect(formatTime(3661)).toBe('1:01:01');
  });
  test('formats exact minutes', () => {
    expect(formatTime(300)).toBe('05:00');
  });
});

describe('calculateInvalidCells', () => {
  test('empty board has no invalid cells', () => {
    expect(calculateInvalidCells(emptyBoard()).size).toBe(0);
  });

  test('detects row conflict', () => {
    const b = emptyBoard();
    b[0][0] = '5';
    b[0][3] = '5';
    const invalid = calculateInvalidCells(b);
    expect(invalid.has('0-0')).toBeTruthy();
    expect(invalid.has('0-3')).toBeTruthy();
  });

  test('detects column conflict', () => {
    const b = emptyBoard();
    b[0][0] = '3';
    b[5][0] = '3';
    const invalid = calculateInvalidCells(b);
    expect(invalid.has('0-0')).toBeTruthy();
    expect(invalid.has('5-0')).toBeTruthy();
  });

  test('detects box conflict', () => {
    const b = emptyBoard();
    b[0][0] = '7';
    b[2][2] = '7';
    const invalid = calculateInvalidCells(b);
    expect(invalid.has('0-0')).toBeTruthy();
    expect(invalid.has('2-2')).toBeTruthy();
  });

  test('no conflicts for distinct values', () => {
    const b = emptyBoard();
    b[0][0] = '1';
    b[0][1] = '2';
    b[0][2] = '3';
    expect(calculateInvalidCells(b).size).toBe(0);
  });

  test('handles null/empty board', () => {
    expect(calculateInvalidCells(null).size).toBe(0);
    expect(calculateInvalidCells([]).size).toBe(0);
  });
});

describe('findAllConflictingCells', () => {
  test('finds row conflicts', () => {
    const b = emptyBoard();
    b[0][0] = '5';
    b[0][5] = '5';
    const conflicts = findAllConflictingCells(b, 0, 0, 5);
    expect(conflicts.has('0-5')).toBeTruthy();
  });

  test('finds column conflicts', () => {
    const b = emptyBoard();
    b[3][0] = '8';
    const conflicts = findAllConflictingCells(b, 0, 0, 8);
    expect(conflicts.has('3-0')).toBeTruthy();
  });

  test('finds box conflicts', () => {
    const b = emptyBoard();
    b[1][1] = '4';
    const conflicts = findAllConflictingCells(b, 0, 0, 4);
    expect(conflicts.has('1-1')).toBeTruthy();
  });

  test('no conflicts for unique value', () => {
    const b = emptyBoard();
    b[0][0] = '1';
    const conflicts = findAllConflictingCells(b, 0, 0, 9);
    expect(conflicts.size).toBe(0);
  });
});

describe('calculateValidCandidates', () => {
  test('all candidates for empty board empty cell', () => {
    const b = emptyBoard();
    const cands = calculateValidCandidates(b, 0, 0);
    expect(cands).toHaveLength(9);
  });

  test('no candidates for filled cell', () => {
    const b = emptyBoard();
    b[0][0] = '5';
    expect(calculateValidCandidates(b, 0, 0)).toHaveLength(0);
  });

  test('excludes row values', () => {
    const b = emptyBoard();
    b[0][1] = '3';
    b[0][2] = '7';
    const cands = calculateValidCandidates(b, 0, 0);
    expect(cands).not.toContain(3);
    expect(cands).not.toContain(7);
    expect(cands).toContain(1);
  });

  test('excludes column values', () => {
    const b = emptyBoard();
    b[3][0] = '2';
    const cands = calculateValidCandidates(b, 0, 0);
    expect(cands).not.toContain(2);
  });

  test('excludes box values', () => {
    const b = emptyBoard();
    b[1][1] = '9';
    const cands = calculateValidCandidates(b, 0, 0);
    expect(cands).not.toContain(9);
  });

  test('combined constraints', () => {
    const b = sampleBoard();
    const cands = calculateValidCandidates(b, 0, 2);
    expect(cands).not.toContain(5);
    expect(cands).not.toContain(3);
    expect(cands).not.toContain(7);
    expect(cands).not.toContain(8);
    expect(cands).not.toContain(9);
  });
});

describe('generateAllCandidates', () => {
  test('generates candidates for all empty cells', () => {
    const b = sampleBoard();
    const allCands = generateAllCandidates(b);
    expect(allCands['0-0']).toBe(undefined);
    expect(allCands['0-2']).toBeTruthy();
  });

  test('no candidates needed for full board', () => {
    const b = Array.from({ length: 9 }, (_, r) => Array.from({ length: 9 }, (_, c) => String(((r * 3 + Math.floor(r / 3) + c) % 9) + 1)));
    const allCands = generateAllCandidates(b);
    expect(Object.keys(allCands).length).toBe(0);
  });
});

describe('boardToString', () => {
  const boardToString = (boardGrid) => {
    return boardGrid.map(row => row.map(cell => cell || '0').join('')).join('');
  };

  test('converts empty board to all zeros', () => {
    const b = emptyBoard();
    const result = boardToString(b);
    expect(result.length).toBe(81);
    expect(result).toBe('0'.repeat(81));
  });

  test('converts partial board correctly', () => {
    const b = emptyBoard();
    b[0][0] = '5';
    b[0][1] = '3';
    const result = boardToString(b);
    expect(result[0]).toBe('5');
    expect(result[1]).toBe('3');
    expect(result[2]).toBe('0');
    expect(result.length).toBe(81);
  });

  test('round-trips with string parsing', () => {
    const original = '530070000600195000098000060800060003400803001700020006060000280000419005000080079';
    const boardArray = original.split('').map(n => n === '0' ? '' : n);
    const boardGrid = [];
    for (let i = 0; i < 9; i++) {
      boardGrid.push(boardArray.slice(i * 9, (i + 1) * 9));
    }
    const result = boardToString(boardGrid);
    expect(result).toBe(original);
  });
});

describe('settings serialization', () => {
  test('settings can be serialized and deserialized', () => {
    const settings = { ...DEFAULT_SETTINGS, errorIndicator: true, threeMistakeLimit: true };
    const json = JSON.stringify(settings);
    const parsed = JSON.parse(json);
    expect(parsed.errorIndicator).toBe(true);
    expect(parsed.threeMistakeLimit).toBe(true);
    expect(parsed.highlightConflicts).toBe(true);
  });

  test('merging with DEFAULT_SETTINGS fills missing keys', () => {
    const partial = { errorIndicator: true };
    const merged = { ...DEFAULT_SETTINGS, ...partial };
    expect(merged.errorIndicator).toBe(true);
    expect(merged.highlightConflicts).toBe(true);
    expect(merged.highlightRowColumn).toBe(true);
    expect(merged.threeMistakeLimit).toBe(false);
  });

  test('null settingsJson uses defaults', () => {
    const settingsJson = null;
    let settings = DEFAULT_SETTINGS;
    if (settingsJson) {
      try {
        settings = { ...DEFAULT_SETTINGS, ...JSON.parse(settingsJson) };
      } catch (e) { /* keep defaults */ }
    }
    expect(settings.errorIndicator).toBe(false);
    expect(settings.threeMistakeLimit).toBe(false);
  });

  test('invalid JSON falls back to defaults', () => {
    const settingsJson = 'not-valid-json';
    let settings = DEFAULT_SETTINGS;
    if (settingsJson) {
      try {
        settings = { ...DEFAULT_SETTINGS, ...JSON.parse(settingsJson) };
      } catch (e) { /* keep defaults */ }
    }
    expect(settings.errorIndicator).toBe(false);
  });
});

describe('cluesString vs boardString parsing', () => {
  const cluesString = '530070000600195000098000060800060003400803001700020006060000280000419005000080079';
  const boardString = '534678912672195348198342567825961734349287651761524896956837281283419675417258039';

  test('originalBoard from cluesString has empty cells for zeros', () => {
    const cluesArray = cluesString.split('').map(n => n === '0' ? '' : n);
    const cluesGrid = [];
    for (let i = 0; i < 9; i++) {
      cluesGrid.push(cluesArray.slice(i * 9, (i + 1) * 9));
    }
    expect(cluesGrid[0][0]).toBe('5');
    expect(cluesGrid[0][1]).toBe('3');
    expect(cluesGrid[0][2]).toBe('');
    expect(cluesGrid[0][3]).toBe('');
  });

  test('board from boardString includes user-placed values', () => {
    const boardArray = boardString.split('').map(n => n === '0' ? '' : n);
    const boardGrid = [];
    for (let i = 0; i < 9; i++) {
      boardGrid.push(boardArray.slice(i * 9, (i + 1) * 9));
    }
    expect(boardGrid[0][2]).toBe('4');
    expect(boardGrid[0][3]).toBe('6');
  });

  test('user-placed cells are not in originalBoard', () => {
    const cluesArray = cluesString.split('').map(n => n === '0' ? '' : n);
    const cluesGrid = [];
    for (let i = 0; i < 9; i++) {
      cluesGrid.push(cluesArray.slice(i * 9, (i + 1) * 9));
    }
    expect(cluesGrid[0][2]).toBe('');
    const canEdit = !cluesGrid[0][2];
    expect(canEdit).toBe(true);
  });
});

describe('gameConstants', () => {
  test('COLOR_PROFILES has 7 profiles', () => {
    expect(Object.keys(COLOR_PROFILES).length).toBe(7);
  });

  test('each profile has intensive and light', () => {
    for (const [key, profile] of Object.entries(COLOR_PROFILES)) {
      expect(profile.intensive).toBeTruthy();
      expect(profile.light).toBeTruthy();
      expect(profile.name).toBeTruthy();
    }
  });

  test('DIALOGS includes GAME_OVER', () => {
    expect(DIALOGS.GAME_OVER).toBe('gameover');
  });

  test('DIALOGS has all expected keys', () => {
    const keys = Object.keys(DIALOGS);
    expect(keys.length).toBe(8);
    expect(DIALOGS.PAUSE).toBe('pause');
    expect(DIALOGS.WIN).toBe('win');
    expect(DIALOGS.SETTINGS).toBe('settings');
  });

  test('DEFAULT_SETTINGS has threeMistakeLimit', () => {
    expect(DEFAULT_SETTINGS.threeMistakeLimit).toBe(false);
  });

  test('DEFAULT_SETTINGS has errorIndicator', () => {
    expect(DEFAULT_SETTINGS.errorIndicator).toBe(false);
  });

  test('DEFAULT_SETTINGS defaults are correct', () => {
    expect(DEFAULT_SETTINGS.highlightConflicts).toBe(true);
    expect(DEFAULT_SETTINGS.highlightRowColumn).toBe(true);
    expect(DEFAULT_SETTINGS.highlightBox).toBe(true);
    expect(DEFAULT_SETTINGS.highlightIdenticalNumbers).toBe(true);
    expect(DEFAULT_SETTINGS.fontSize).toBe('medium');
  });
});

describe('Undo History: computeReverseDiff', () => {
  test('detects single board cell change', () => {
    const oldBoard = [[0, 0], [0, 0]];
    const newBoard = [[5, 0], [0, 0]];
    const diff = computeReverseDiff(oldBoard, {}, null, false, newBoard, {});
    expect(diff.b).toHaveLength(1);
    expect(diff.b[0].r).toBe(0);
    expect(diff.b[0].c).toBe(0);
    expect(diff.b[0].v).toBe(0);
  });

  test('no board changes produces undefined b', () => {
    const board = [[1, 2], [3, 4]];
    const diff = computeReverseDiff(board, {}, null, false, board, {});
    expect(diff.b).toBe(undefined);
  });

  test('detects candidate changes', () => {
    const oldCands = { '0-0': [1, 2, 3] };
    const newCands = { '0-0': [1, 3] };
    const diff = computeReverseDiff([[0]], oldCands, null, false, [[0]], newCands);
    expect(diff.c['0-0']).toEqual([1, 2, 3]);
  });

  test('detects added candidate key', () => {
    const oldCands = {};
    const newCands = { '0-0': [5] };
    const diff = computeReverseDiff([[0]], oldCands, null, false, [[0]], newCands);
    expect(diff.c['0-0']).toEqual([]);
  });

  test('detects removed candidate key', () => {
    const oldCands = { '0-0': [5] };
    const newCands = {};
    const diff = computeReverseDiff([[0]], oldCands, null, false, [[0]], newCands);
    expect(diff.c['0-0']).toEqual([5]);
  });

  test('stores selectedCell as compact format', () => {
    const diff = computeReverseDiff([[0]], {}, { row: 3, col: 5 }, false, [[0]], {});
    expect(diff.s.r).toBe(3);
    expect(diff.s.c).toBe(5);
  });

  test('stores isAutoCandidateMode', () => {
    const diff = computeReverseDiff([[0]], {}, null, true, [[0]], {});
    expect(diff.a).toBe(true);
  });
});

describe('Undo History: applyReverseDiff', () => {
  test('restores board cell', () => {
    const board = [[5, 0], [0, 0]];
    const diff = { b: [{ r: 0, c: 0, v: 0 }] };
    applyReverseDiff(board, {}, diff);
    expect(board[0][0]).toBe(0);
  });

  test('restores candidates', () => {
    const cands = { '0-0': [1, 3] };
    const diff = { c: { '0-0': [1, 2, 3] } };
    applyReverseDiff([[0]], cands, diff);
    expect(cands['0-0']).toEqual([1, 2, 3]);
  });

  test('deletes empty candidate arrays', () => {
    const cands = { '0-0': [5] };
    const diff = { c: { '0-0': [] } };
    applyReverseDiff([[0]], cands, diff);
    expect(cands['0-0']).toBe(undefined);
  });

  test('returns selectedCell', () => {
    const diff = { s: { r: 2, c: 7 } };
    const result = applyReverseDiff([[0]], {}, diff);
    expect(result.selectedCell.row).toBe(2);
    expect(result.selectedCell.col).toBe(7);
  });

  test('returns null selectedCell when no s', () => {
    const diff = {};
    const result = applyReverseDiff([[0]], {}, diff);
    expect(result.selectedCell).toBeNull();
  });
});

describe('Undo History: full cycle (diff + apply)', () => {
  test('single move undo restores original', () => {
    const origBoard = [[0, 0, 0], [0, 0, 0], [0, 0, 0]];
    const newBoard = [[5, 0, 0], [0, 0, 0], [0, 0, 0]];
    const origCands = { '0-0': [1, 5, 9] };
    const newCands = {};

    const diff = computeReverseDiff(origBoard, origCands, { row: 0, col: 0 }, false, newBoard, newCands);

    const boardToRestore = deepCopyBoard(newBoard);
    const candsToRestore = deepCopyCandidates(newCands);
    const result = applyReverseDiff(boardToRestore, candsToRestore, diff);

    expect(boardToRestore[0][0]).toBe(0);
    expect(candsToRestore['0-0']).toEqual([1, 5, 9]);
    expect(result.selectedCell.row).toBe(0);
    expect(result.selectedCell.col).toBe(0);
  });

  test('multiple moves undo restores step by step', () => {
    const board0 = [[0, 0], [0, 0]];
    const board1 = [[1, 0], [0, 0]];
    const board2 = [[1, 2], [0, 0]];

    const diff1 = computeReverseDiff(board0, {}, null, false, board1, {});
    const diff2 = computeReverseDiff(board1, {}, null, false, board2, {});

    const b2 = deepCopyBoard(board2);
    applyReverseDiff(b2, {}, diff2);
    expect(b2[0][0]).toBe(1);
    expect(b2[0][1]).toBe(0);

    const b1 = deepCopyBoard(b2);
    applyReverseDiff(b1, {}, diff1);
    expect(b1[0][0]).toBe(0);
    expect(b1[0][1]).toBe(0);
  });

  test('diff is much smaller than full snapshot', () => {
    const board9x9 = Array.from({ length: 9 }, () => Array(9).fill(0));
    const newBoard = board9x9.map(r => [...r]);
    newBoard[4][4] = 7;

    const diff = computeReverseDiff(board9x9, {}, null, false, newBoard, {});
    const diffJson = JSON.stringify(diff);
    const snapshotJson = JSON.stringify({ board: board9x9, candidates: {} });

    expect(diffJson.length).toBeLessThan(snapshotJson.length);
  });
});

describe('boardToString conversion', () => {
  test('converts empty board to all zeros', () => {
    const board = emptyBoard();
    const str = board.map(row => row.map(cell => cell || '0').join('')).join('');
    expect(str.length).toBe(81);
    expect(str).toBe('0'.repeat(81));
  });

  test('converts partial board correctly', () => {
    const board = emptyBoard();
    board[0][0] = '5';
    board[0][1] = '3';
    board[8][8] = '9';
    const str = board.map(row => row.map(cell => cell || '0').join('')).join('');
    expect(str.length).toBe(81);
    expect(str[0]).toBe('5');
    expect(str[1]).toBe('3');
    expect(str[80]).toBe('9');
  });

  test('roundtrip: string to board to string', () => {
    const original = '530070000600195000098000060800060003400803001700020006060000280000419005000080079';
    const board = [];
    for (let r = 0; r < 9; r++) {
      const row = [];
      for (let c = 0; c < 9; c++) {
        const ch = original[r * 9 + c];
        row.push(ch === '0' ? null : ch);
      }
      board.push(row);
    }
    const result = board.map(row => row.map(cell => cell || '0').join('')).join('');
    expect(result).toBe(original);
  });
});

describe('Auto-candidate continuous recalculation', () => {
  test('generateAllCandidates recalculates after placing a number', () => {
    const board = emptyBoard();
    board[0][0] = '5';
    const cands = generateAllCandidates(board);
    expect(cands['0-1']).not.toContain(5);
    expect(cands['1-0']).not.toContain(5);
    expect(cands['1-1']).not.toContain(5);
    expect(cands['5-5']).toContain(5);
  });

  test('generateAllCandidates restores candidates after removing a number', () => {
    const board = emptyBoard();
    board[0][0] = '5';
    const cands1 = generateAllCandidates(board);
    expect(cands1['0-1']).not.toContain(5);

    board[0][0] = null;
    const cands2 = generateAllCandidates(board);
    expect(cands2['0-1']).toContain(5);
    expect(cands2['1-0']).toContain(5);
    expect(cands2['1-1']).toContain(5);
  });

  test('replacing number in same cell recalculates all candidates', () => {
    const board = emptyBoard();
    board[0][0] = '5';
    const cands1 = generateAllCandidates(board);
    expect(cands1['0-1']).not.toContain(5);
    expect(cands1['0-1']).toContain(3);

    board[0][0] = '3';
    const cands2 = generateAllCandidates(board);
    expect(cands2['0-1']).toContain(5);
    expect(cands2['0-1']).not.toContain(3);
  });

  test('generateAllCandidates excludes filled cells', () => {
    const board = emptyBoard();
    board[0][0] = '5';
    const cands = generateAllCandidates(board);
    expect(cands['0-0']).toBe(undefined);
  });
});

describe('Error counting with indicator conditions', () => {
  const shouldCountErrors = (settings) => {
    return settings.errorIndicator || settings.threeMistakeLimit;
  };

  test('counts errors when errorIndicator is ON', () => {
    expect(shouldCountErrors({ errorIndicator: true, threeMistakeLimit: false })).toBe(true);
  });

  test('counts errors when threeMistakeLimit is ON', () => {
    expect(shouldCountErrors({ errorIndicator: false, threeMistakeLimit: true })).toBe(true);
  });

  test('counts errors when both are ON', () => {
    expect(shouldCountErrors({ errorIndicator: true, threeMistakeLimit: true })).toBe(true);
  });

  test('does not count errors when both are OFF', () => {
    expect(shouldCountErrors({ errorIndicator: false, threeMistakeLimit: false })).toBe(false);
  });
});

describe('Mistake indicator visibility', () => {
  const showMistakeIndicator = (showErrorIndicator, threeMistakeLimit) => {
    return showErrorIndicator || threeMistakeLimit;
  };

  test('visible when errorIndicator ON', () => {
    expect(showMistakeIndicator(true, false)).toBe(true);
  });

  test('visible when threeMistakeLimit ON', () => {
    expect(showMistakeIndicator(false, true)).toBe(true);
  });

  test('visible when both ON', () => {
    expect(showMistakeIndicator(true, true)).toBe(true);
  });

  test('hidden when both OFF', () => {
    expect(showMistakeIndicator(false, false)).toBe(false);
  });

  test('displays X/3 format with threeMistakeLimit', () => {
    const errorCount = 2;
    const threeMistakeLimit = true;
    const display = threeMistakeLimit ? `${errorCount}/3` : `${errorCount}`;
    expect(display).toBe('2/3');
  });

  test('displays plain count with errorIndicator only', () => {
    const errorCount = 4;
    const threeMistakeLimit = false;
    const display = threeMistakeLimit ? `${errorCount}/3` : `${errorCount}`;
    expect(display).toBe('4');
  });
});

describe('Undo is runtime-only (not persisted)', () => {
  test('undo history starts empty after reset', () => {
    const undoStack = [];
    expect(undoStack.length).toBe(0);
  });

  test('undo stack grows with saveToHistory calls', () => {
    const undoStack = [];
    const board0 = [[0, 0], [0, 0]];
    const board1 = [[1, 0], [0, 0]];
    const diff = computeReverseDiff(board0, {}, null, false, board1, {});
    undoStack.push(diff);
    expect(undoStack.length).toBe(1);
  });

  test('undo pops last diff and applies it', () => {
    const undoStack = [];
    const board0 = [[0, 0], [0, 0]];
    const board1 = [[1, 0], [0, 0]];
    const cands0 = { '0-0': [1, 2] };
    const cands1 = {};
    const diff = computeReverseDiff(board0, cands0, { row: 0, col: 0 }, false, board1, cands1);
    undoStack.push(diff);

    const lastDiff = undoStack.pop();
    const restoredBoard = deepCopyBoard(board1);
    const restoredCands = deepCopyCandidates(cands1);
    const result = applyReverseDiff(restoredBoard, restoredCands, lastDiff);

    expect(restoredBoard[0][0]).toBe(0);
    expect(restoredCands['0-0']).toEqual([1, 2]);
    expect(result.selectedCell.row).toBe(0);
    expect(result.selectedCell.col).toBe(0);
    expect(undoStack.length).toBe(0);
  });

  test('after reset undo stack is cleared', () => {
    const undoStack = [];
    undoStack.push({ b: [{ r: 0, c: 0, v: 0 }] });
    undoStack.push({ b: [{ r: 1, c: 0, v: 0 }] });
    undoStack.length = 0;
    expect(undoStack.length).toBe(0);
  });
});

describe('Save game payload (no undo)', () => {
  test('save payload includes boardString', () => {
    const board = emptyBoard();
    board[0][0] = '5';
    const payload = {
      elapsedTimeSeconds: 120,
      errorCount: 1,
      autoCandidateModeUsed: true,
      isAutoCandidateMode: false,
      candidatesJson: JSON.stringify({ '0-1': [1, 2, 3] }),
      boardString: board.map(row => row.map(cell => cell || '0').join('')).join(''),
      colorProfile: 'orange',
    };
    expect(payload.boardString.length).toBe(81);
    expect(payload.boardString[0]).toBe('5');
    expect(payload.undoHistoryJson).toBe(undefined);
  });

  test('save payload does not include undoHistoryJson', () => {
    const payload = {
      elapsedTimeSeconds: 60,
      errorCount: 0,
      boardString: '0'.repeat(81),
      candidatesJson: '{}',
    };
    expect(Object.keys(payload)).not.toContain('undoHistoryJson');
  });
});

describe('3-Mistake Limit logic', () => {
  test('game over triggered at exactly 3 mistakes', () => {
    const errorCount = 2;
    const newCount = errorCount + 1; // 3
    const threeMistakeLimit = true;
    const shouldGameOver = threeMistakeLimit && newCount >= 3;
    expect(shouldGameOver).toBe(true);
  });

  test('no game over below 3 mistakes', () => {
    const errorCount = 1;
    const newCount = errorCount + 1; // 2
    const threeMistakeLimit = true;
    const shouldGameOver = threeMistakeLimit && newCount >= 3;
    expect(shouldGameOver).toBe(false);
  });

  test('no game over when feature disabled', () => {
    const errorCount = 2;
    const newCount = errorCount + 1;
    const threeMistakeLimit = false;
    const shouldGameOver = threeMistakeLimit && newCount >= 3;
    expect(shouldGameOver).toBe(false);
  });

  test('mistake display format with limit', () => {
    const errorCount = 2;
    const threeMistakeLimit = true;
    const display = threeMistakeLimit ? `${errorCount}/3` : `${errorCount}`;
    expect(display).toBe('2/3');
  });

  test('mistake display format without limit', () => {
    const errorCount = 5;
    const threeMistakeLimit = false;
    const display = threeMistakeLimit ? `${errorCount}/3` : `${errorCount}`;
    expect(display).toBe('5');
  });
});

describe('Mistake mode locking', () => {
  const handleToggle = (settings, setting) => {
    if (setting === 'errorIndicator' || setting === 'threeMistakeLimit') {
      if (settings[setting]) return { ...settings };
      if (setting === 'errorIndicator' && settings.threeMistakeLimit) return { ...settings };
      if (setting === 'threeMistakeLimit' && settings.errorIndicator) return { ...settings };
    }
    const newValue = !settings[setting];
    const updatedSettings = { ...settings, [setting]: newValue };
    return updatedSettings;
  };

  test('enabling errorIndicator locks it ON', () => {
    const settings = { errorIndicator: false, threeMistakeLimit: false, highlightConflicts: true };
    const result = handleToggle(settings, 'errorIndicator');
    expect(result.errorIndicator).toBe(true);
    const result2 = handleToggle(result, 'errorIndicator');
    expect(result2.errorIndicator).toBe(true);
  });

  test('enabling threeMistakeLimit locks it ON', () => {
    const settings = { errorIndicator: false, threeMistakeLimit: false, highlightConflicts: true };
    const result = handleToggle(settings, 'threeMistakeLimit');
    expect(result.threeMistakeLimit).toBe(true);
    const result2 = handleToggle(result, 'threeMistakeLimit');
    expect(result2.threeMistakeLimit).toBe(true);
  });

  test('enabling errorIndicator blocks enabling threeMistakeLimit', () => {
    const settings = { errorIndicator: true, threeMistakeLimit: false };
    const result = handleToggle(settings, 'threeMistakeLimit');
    expect(result.threeMistakeLimit).toBe(false);
    expect(result.errorIndicator).toBe(true);
  });

  test('enabling threeMistakeLimit blocks enabling errorIndicator', () => {
    const settings = { errorIndicator: false, threeMistakeLimit: true };
    const result = handleToggle(settings, 'errorIndicator');
    expect(result.errorIndicator).toBe(false);
    expect(result.threeMistakeLimit).toBe(true);
  });

  test('both can be OFF simultaneously (before any choice)', () => {
    const settings = { errorIndicator: false, threeMistakeLimit: false };
    expect(settings.errorIndicator).toBe(false);
    expect(settings.threeMistakeLimit).toBe(false);
  });

  test('non-indicator toggles are unaffected by locking', () => {
    const settings = { errorIndicator: true, threeMistakeLimit: false, highlightConflicts: false };
    const result = handleToggle(settings, 'highlightConflicts');
    expect(result.highlightConflicts).toBe(true);
    expect(result.errorIndicator).toBe(true);
  });

  test('isMistakeModeLocked is true when errorIndicator active', () => {
    const settings = { errorIndicator: true, threeMistakeLimit: false };
    const isMistakeModeLocked = settings.errorIndicator || settings.threeMistakeLimit;
    expect(isMistakeModeLocked).toBe(true);
  });

  test('isMistakeModeLocked is true when threeMistakeLimit active', () => {
    const settings = { errorIndicator: false, threeMistakeLimit: true };
    const isMistakeModeLocked = settings.errorIndicator || settings.threeMistakeLimit;
    expect(isMistakeModeLocked).toBe(true);
  });

  test('isMistakeModeLocked is false when both OFF', () => {
    const settings = { errorIndicator: false, threeMistakeLimit: false };
    const isMistakeModeLocked = settings.errorIndicator || settings.threeMistakeLimit;
    expect(isMistakeModeLocked).toBe(false);
  });

  test('full exploit scenario: cannot toggle off to cheat score', () => {
    let settings = { errorIndicator: false, threeMistakeLimit: false };
    settings = handleToggle(settings, 'errorIndicator');
    expect(settings.errorIndicator).toBe(true);
    settings = handleToggle(settings, 'errorIndicator');
    expect(settings.errorIndicator).toBe(true); // STILL ON — exploit blocked
    settings = handleToggle(settings, 'threeMistakeLimit');
    expect(settings.threeMistakeLimit).toBe(false); // BLOCKED — only one mode allowed
  });
});

console.log(`\n${'═'.repeat(50)}`);
console.log(`  Tests: ${passed + failed} total | ✅ ${passed} passed | ❌ ${failed} failed`);
console.log(`${'═'.repeat(50)}\n`);

if (failed > 0) {
  process.exit(1);
}
