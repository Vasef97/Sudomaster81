export const COLOR_PROFILES = {
  orange: { name: 'Orange', intensive: '#ffa500', light: '#f5f5dc' },
  lemon: { name: 'Lemon', intensive: '#f0e68c', light: '#fffef0' },
  green: { name: 'Green', intensive: '#a0c98a', light: '#e8f4e1' },
  blue: { name: 'Blue', intensive: '#6ba3d4', light: '#e8f2fa' },
  pink: { name: 'Pink', intensive: '#e8a5c8', light: '#f9e8f5' },
  purple: { name: 'Purple', intensive: '#b8a3d4', light: '#f0e8f8' },
  lavender: { name: 'Lavender', intensive: '#dda0dd', light: '#f8f0ff' },
};

export const DIALOGS = {
  PAUSE: 'pause',
  EXIT: 'exit',
  LOGOUT: 'logout',
  INSTRUCTIONS: 'instructions',
  WIN: 'win',
  SETTINGS: 'settings',
  ABOUT: 'about',
  GAME_OVER: 'gameover',
};

export const DEFAULT_SETTINGS = {
  highlightConflicts: true,
  highlightRowColumn: true,
  highlightBox: true,
  highlightIdenticalNumbers: true,
  errorIndicator: false,
  threeMistakeLimit: false,
  fontSize: 'medium',
};
