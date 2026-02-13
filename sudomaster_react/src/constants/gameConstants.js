export const COLOR_PROFILES = {
  orange: { name: 'Orange', intensive: '#ffa500', light: '#f5f5dc', prefilledLight: '#dfd3b5' },
  lemon: { name: 'Lemon', intensive: '#f0e68c', light: '#fffef0', prefilledLight: '#e5e0ba' },
  green: { name: 'Green', intensive: '#a0c98a', light: '#e8f4e1', prefilledLight: '#cad5c3' },
  blue: { name: 'Blue', intensive: '#6ba3d4', light: '#e8f2fa', prefilledLight: '#c8d5df' },
  pink: { name: 'Pink', intensive: '#e8a5c8', light: '#f9e8f5', prefilledLight: '#dfc3d6' },
  purple: { name: 'Purple', intensive: '#b8a3d4', light: '#f0e8f8', prefilledLight: '#d0c5da' },
  lavender: { name: 'Lavender', intensive: '#dda0dd', light: '#f8f0ff', prefilledLight: '#d7cadf' },
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
