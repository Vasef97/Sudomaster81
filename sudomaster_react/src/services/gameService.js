import { fetchWithAuth, getApiBaseUrl } from './httpClient.js';

const API_BASE_URL = getApiBaseUrl();

export const createGame = async (difficulty) => {
  try {
    const userId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');
    
    const url = `${API_BASE_URL}/game/new`;
    const response = await fetchWithAuth(url, {
      method: 'POST',
      body: JSON.stringify({ difficulty })
    });
    
    const boardString = response.boardString;
    
    if (!boardString || typeof boardString !== 'string' || boardString.length !== 81) {
      throw new Error('Invalid boardString from API - expected 81-character string');
    }
    
    const puzzleArray = boardString.split('').map(char => char === '0' ? '' : char);
    
    return {
      ...response,
      boardString: boardString,
      puzzle: puzzleArray
    };
  } catch (error) {
    throw error;
  }
};

export const makeMove = async (sessionId, row, col, value) => {
  const position = row * 9 + col;
  const url = `${API_BASE_URL}/game/${sessionId}/move`;
  const payload = {
    position: position,
    value: value ? parseInt(value) : 0
  };

  try {
    const response = await fetchWithAuth(url, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    return response;
  } catch (error) {
    if (error.message?.includes('Concurrent update conflict')) {
      await new Promise(r => setTimeout(r, 100));
      const response = await fetchWithAuth(url, {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      return response;
    }
    throw error;
  }
};

export const completeGame = async (sessionId, completionData) => {
  try {
    if (!sessionId || !completionData) {
      throw new Error(`Invalid parameters: sessionId=${sessionId}, completionData=${completionData}`);
    }

    const { elapsedTime, mistakes = 0, autoCandidateMode = false } = completionData;
    
    if (!elapsedTime || elapsedTime <= 0) {
      throw new Error(`Invalid elapsedTime: ${elapsedTime}. Must be greater than 0.`);
    }
    
    const payload = {
      elapsedTime,
      mistakes,
      autoCandidateMode
    };
    
    const url = `${API_BASE_URL}/game/${sessionId}/complete`;
    const response = await fetchWithAuth(url, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    return response;
  } catch (error) {
    if (error.message && error.message.includes('Concurrent update conflict')) {
      await new Promise(resolve => setTimeout(resolve, 150));
      try {
        const url = `${API_BASE_URL}/game/${sessionId}/complete`;
        const response = await fetchWithAuth(url, {
          method: 'POST',
          body: JSON.stringify({ elapsedTime: completionData.elapsedTime, mistakes: completionData.mistakes || 0, autoCandidateMode: completionData.autoCandidateMode || false })
        });
        return response;
      } catch (retryError) {
        throw retryError;
      }
    }
    throw error;
  }
};

export const checkAnswer = async (sessionId, row, col, value) => {
  try {
    const payload = { 
      sessionId, 
      row, 
      column: col,
      value: parseInt(value)
    };
    
    const url = `${API_BASE_URL}/game/iscorrect`;
    const response = await fetchWithAuth(url, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    return response;
  } catch (error) {
    throw error;
  }
};

export const getSavedGame = async (difficulty) => {
  try {
    const url = `${API_BASE_URL}/game/saved?difficulty=${encodeURIComponent(difficulty)}`;
    const response = await fetchWithAuth(url);
    if (response && response.sessionId) {
      return response;
    }
    return null;
  } catch (error) {
    return null;
  }
};

export const saveGameState = async (sessionId, data) => {
  try {
    const url = `${API_BASE_URL}/game/${sessionId}/save`;
    const response = await fetchWithAuth(url, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
    return response;
  } catch (error) {
    return null;
  }
};

export const abandonGame = async (sessionId) => {
  try {
    const url = `${API_BASE_URL}/game/${sessionId}/abandon`;
    const response = await fetchWithAuth(url, {
      method: 'DELETE'
    });
    return response;
  } catch (error) {
    throw error;
  }
};

export default {
  createGame,
  makeMove,
  completeGame,
  checkAnswer,
  getSavedGame,
  saveGameState,
  abandonGame
};
