import React, { useRef, useEffect, useState } from 'react';
import DifficultySelector from './components/DifficultySelector/DifficultySelector';
import GamePage from './pages/GamePage/GamePage';
import Login from './components/Login/Login';
import Register from './components/Register/Register';
import ServerErrorPage from './pages/ServerErrorPage/ServerErrorPage';
import WakingUpPage from './pages/WakingUpPage/WakingUpPage';
import Footer from './components/Footer/Footer';
import AboutDialog from './components/Dialogs/AboutDialog/AboutDialog';
import { COLOR_PROFILES } from './constants/gameConstants';
import { useAppAuth } from './hooks/useAppAuth';
import { useAppGame } from './hooks/useAppGame';
import { useAppSettings } from './hooks/useAppSettings';
import './App.css';

function App() {
  const {
    authState,
    user,
    hasServerError,
    isWakingUp,
    handleLoginSuccess: handleLoginSuccessAuth,
    handleRegisterSuccess: handleRegisterSuccessAuth,
    handleSwitchToRegister,
    handleSwitchToLogin,
    handleLogout: handleLogoutAuth,
    handleRetryServer,
    handleServerError,
    setAuthState,
  } = useAppAuth();

  const {
    gameStarted,
    difficulty,
    handleStartGame,
    handleNewGame,
    resetGameState,
  } = useAppGame(setAuthState);

  const [savedGameData, setSavedGameData] = useState(null);

  const handleLoginSuccess = (response) => {
    if (response?.preferencesJson) {
      applyPreferencesFromResponse(response.preferencesJson);
    }
    handleLoginSuccessAuth(response);
  };

  const handleRegisterSuccess = (response) => {
    if (response?.preferencesJson) {
      applyPreferencesFromResponse(response.preferencesJson);
    }
    handleRegisterSuccessAuth(response);
  };

  const handleLogout = async () => {
    setSavedGameData(null);
    resetGameState();
    resetPreferences();
    await handleLogoutAuth();
  };

  const {
    colorProfile,
    setColorProfile,
    showAboutDialog,
    setShowAboutDialog,
    applyPreferencesFromResponse,
    buildPreferencesJson,
    getUserSettingsForGame,
    resetPreferences,
  } = useAppSettings();

  const gamePageRef = useRef(null);

  const handleDifficultySelected = (level, savedData) => {
    setSavedGameData(savedData || null);
    handleStartGame(level);
  };

  const handleNewGameFromPage = () => {
    setSavedGameData(null);
    handleNewGame();
  };

  const handleLogoutFromPage = async () => {
    setSavedGameData(null);
    resetGameState();
    resetPreferences();
    await handleLogoutAuth();
  };

  useEffect(() => {
    const handleStorageChange = (e) => {
      if (e.key === 'token' && e.newValue === null) {
        resetGameState();
        setAuthState('login');
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [resetGameState, setAuthState]);

  const handleRetryServerWithGameCleanup = async () => {
    resetGameState();
    await handleRetryServer();
  };

  const handleAboutClick = () => {
    if (authState === 'game' && gamePageRef.current?.openAbout) {
      gamePageRef.current.openAbout();
    } else {
      setShowAboutDialog(true);
    }
  };

  return (
    <>
      {isWakingUp && <WakingUpPage />}
      {!isWakingUp && hasServerError && <ServerErrorPage onRetry={handleRetryServerWithGameCleanup} />}
      {!isWakingUp && !hasServerError && (
        <>
          {authState === 'checking' && <div className="app__loading">⏳ Loading...</div>}
          {authState === 'login' && (
            <Login onLoginSuccess={handleLoginSuccess} onSwitchToRegister={handleSwitchToRegister} />
          )}
          {authState === 'register' && (
            <Register onRegisterSuccess={handleRegisterSuccess} onSwitchToLogin={handleSwitchToLogin} />
          )}
          {authState === 'menu' && user && (
            <DifficultySelector 
              onSelectDifficulty={handleDifficultySelected} 
              onLogout={handleLogout} 
              user={user}
              colorProfile={colorProfile}
              setColorProfile={setColorProfile}
              buildPreferencesJson={buildPreferencesJson}
              getUserSettingsForGame={getUserSettingsForGame}
              applyPreferencesFromResponse={applyPreferencesFromResponse}
            />
          )}
          {authState === 'game' && user && (
            <GamePage 
              ref={gamePageRef} 
              difficulty={difficulty} 
              savedGameData={savedGameData}
              onNewGame={handleNewGameFromPage} 
              onLogout={handleLogoutFromPage} 
              onServerError={handleServerError} 
              colorProfile={colorProfile} 
              setColorProfile={setColorProfile}
              buildPreferencesJson={buildPreferencesJson}
              getUserSettingsForGame={getUserSettingsForGame}
              applyPreferencesFromResponse={applyPreferencesFromResponse}
            />
          )}
          {!authState && <div className="app__error">⚠️ Error: Unknown state</div>}
        </>
      )}
      <Footer onAboutClick={handleAboutClick} />
      <AboutDialog 
        open={showAboutDialog} 
        onClose={() => setShowAboutDialog(false)} 
        colorProfile={colorProfile} 
        colorProfiles={COLOR_PROFILES} 
      />
    </>
  );
}

export default App;
