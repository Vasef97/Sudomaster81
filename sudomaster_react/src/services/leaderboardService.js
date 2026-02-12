import { fetchWithAuth, getApiBaseUrl } from './httpClient.js';

const API_BASE_URL = getApiBaseUrl();

export const getLeaderboard = async (difficulty = 'EASY', limit = 10) => {
  try {
    const url = `${API_BASE_URL}/leaderboard?difficulty=${difficulty}&limit=${limit}`;
    const response = await fetchWithAuth(url);
    return response;
  } catch (error) {
    throw error;
  }
};

export const getUserProfile = async () => {
  try {
    const url = `${API_BASE_URL}/user/profile`;
    const response = await fetchWithAuth(url);
    return response;
  } catch (error) {
    throw error;
  }
};

export const getUserStats = async () => {
  try {
    const url = `${API_BASE_URL}/user/stats`;
    const response = await fetchWithAuth(url);
    return response;
  } catch (error) {
    throw error;
  }
};

export default {
  getLeaderboard,
  getUserProfile,
  getUserStats
};
