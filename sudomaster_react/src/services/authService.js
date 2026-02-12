import * as UserAuth from './userAuthService.js';
import * as GameOps from './gameService.js';
import * as Leaderboard from './leaderboardService.js';
import { checkServerHealth } from './httpClient.js';

export const authService = {
  register: UserAuth.register,
  login: UserAuth.login,
  logout: UserAuth.logout,
  deleteAccount: UserAuth.deleteAccount,
  savePreferences: UserAuth.savePreferences,
  
  isLoggedIn: UserAuth.isLoggedIn,
  getCurrentUser: UserAuth.getCurrentUser,
  
  checkServerHealth: checkServerHealth,
};

export const gameService = {
  createGame: GameOps.createGame,
  makeMove: GameOps.makeMove,
  completeGame: GameOps.completeGame,
  checkAnswer: GameOps.checkAnswer,
  getSavedGame: GameOps.getSavedGame,
  saveGameState: GameOps.saveGameState,
  abandonGame: GameOps.abandonGame,
  
  getLeaderboard: Leaderboard.getLeaderboard,
};

export default authService;
