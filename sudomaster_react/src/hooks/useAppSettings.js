import { useState, useCallback } from 'react';
import { DEFAULT_SETTINGS } from '../constants/gameConstants';

const USER_PREF_KEYS = ['colorProfile', 'fontSize', 'highlightConflicts', 'highlightRowColumn', 'highlightBox', 'highlightIdenticalNumbers'];

const loadStoredPreferences = () => {
  try {
    const raw = localStorage.getItem('userPreferences');
    if (raw) return JSON.parse(raw);
  } catch (e) { }
  return null;
};

export const useAppSettings = () => {
  const stored = loadStoredPreferences();

  const [colorProfile, setColorProfile] = useState(stored?.colorProfile || 'orange');
  const [userPreferences, setUserPreferences] = useState(stored || null);
  const [showAboutDialog, setShowAboutDialog] = useState(false);

  const applyPreferencesFromResponse = useCallback((preferencesJson) => {
    if (!preferencesJson) {
      setUserPreferences(null);
      setColorProfile('orange');
      localStorage.removeItem('userPreferences');
      return;
    }
    try {
      const prefs = typeof preferencesJson === 'string' ? JSON.parse(preferencesJson) : preferencesJson;
      setUserPreferences(prefs);
      if (prefs.colorProfile) setColorProfile(prefs.colorProfile);
      localStorage.setItem('userPreferences', typeof preferencesJson === 'string' ? preferencesJson : JSON.stringify(prefs));
    } catch (e) { }
  }, []);

  const resetPreferences = useCallback(() => {
    setUserPreferences(null);
    setColorProfile('orange');
    localStorage.removeItem('userPreferences');
  }, []);

  const buildPreferencesJson = useCallback((overrides = {}) => {
    const current = loadStoredPreferences() || {};
    const merged = {
      colorProfile: overrides.colorProfile ?? current.colorProfile ?? colorProfile,
      fontSize: overrides.fontSize ?? current.fontSize ?? DEFAULT_SETTINGS.fontSize,
      highlightConflicts: overrides.highlightConflicts ?? current.highlightConflicts ?? DEFAULT_SETTINGS.highlightConflicts,
      highlightRowColumn: overrides.highlightRowColumn ?? current.highlightRowColumn ?? DEFAULT_SETTINGS.highlightRowColumn,
      highlightBox: overrides.highlightBox ?? current.highlightBox ?? DEFAULT_SETTINGS.highlightBox,
      highlightIdenticalNumbers: overrides.highlightIdenticalNumbers ?? current.highlightIdenticalNumbers ?? DEFAULT_SETTINGS.highlightIdenticalNumbers,
    };
    return JSON.stringify(merged);
  }, [colorProfile]);

  const getUserSettingsForGame = useCallback(() => {
    const prefs = loadStoredPreferences();
    if (!prefs) return {};
    const result = {};
    if (prefs.fontSize) result.fontSize = prefs.fontSize;
    if (prefs.highlightConflicts !== undefined) result.highlightConflicts = prefs.highlightConflicts;
    if (prefs.highlightRowColumn !== undefined) result.highlightRowColumn = prefs.highlightRowColumn;
    if (prefs.highlightBox !== undefined) result.highlightBox = prefs.highlightBox;
    if (prefs.highlightIdenticalNumbers !== undefined) result.highlightIdenticalNumbers = prefs.highlightIdenticalNumbers;
    return result;
  }, []);

  return {
    colorProfile,
    setColorProfile,
    userPreferences,
    showAboutDialog,
    setShowAboutDialog,
    applyPreferencesFromResponse,
    buildPreferencesJson,
    getUserSettingsForGame,
    resetPreferences,
    USER_PREF_KEYS,
  };
};
