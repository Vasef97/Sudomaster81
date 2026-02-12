import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';
import { useDialogKeyboardShortcuts } from '../../../hooks/useDialogKeyboardShortcuts';
import './InstructionsDialog.css';

export default function InstructionsDialog({ open, onClose, onAboutClick, colorProfile = 'orange', colorProfiles = {} }) {
  const { handleKeyDown } = useDialogKeyboardShortcuts({ onClose });

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      disableEnforceFocus
      disableAutoFocus
      disableRestoreFocus
      onKeyDown={handleKeyDown}
      PaperProps={{
        sx: {
          borderRadius: '12px',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.15)',
        },
      }}
    >
      <DialogTitle sx={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1a1a1a', borderBottom: `2px solid ${colorProfiles[colorProfile]?.intensive || '#ffa500'}`, paddingBottom: '16px' }}>
        📖 Game Instructions
      </DialogTitle>
      <DialogContent sx={{ padding: '24px', fontSize: '0.95rem', lineHeight: '1.6', color: '#333' }}>
        <div className="instructions-section">
          <h3 className="instructions-title">🎯 Objective</h3>
          <p className="instructions-text">
            Fill the 9x9 grid so that each number from 1 to 9 appears exactly once in each row, column, and 3x3 box.
          </p>
        </div>

        <div className="instructions-section">
          <h3 className="instructions-title">🖱️ How to Play</h3>
          <ul className="instructions-list">
            <li><strong>Click a cell:</strong> to select it</li>
            <li><strong>Enter numbers:</strong> using the number pad or keyboard (1-9)</li>
            <li><strong>Click "Clear":</strong> to remove a number</li>
            <li><strong>Red cells:</strong> indicate conflicting numbers</li>
            <li><strong>Darker cells:</strong> are the given clues (cannot be changed)</li>
          </ul>
        </div>

        <div className="instructions-section">
          <h3 className="instructions-title">💡 Smart Tips</h3>
          <ul className="instructions-list">
            <li>Start by looking for rows, columns, or boxes where only one number fits</li>
            <li>Use candidates strategically to eliminate possibilities</li>
            <li>Focus on areas with many given clues first to build momentum</li>
            <li>The more you play, the better you'll recognize patterns</li>
            <li>Don't be afraid to use Pause, it helps with complex puzzles!</li>
          </ul>
        </div>

        <div className="instructions-section">
          <h3 className="instructions-title">✨ Features</h3>
          <ul className="instructions-list">
            <li><strong>Candidate Mode:</strong> Mark potential numbers in a cell as small digits</li>
            <li><strong>Auto Candidate:</strong> Automatically fills in all possible candidates, but it affects your score</li>
            <li><strong>Mistake Indicator:</strong> Tracks your mistakes with a visible counter and each wrong answer lowers your score</li>
            <li><strong>3 Mistake Limit:</strong> The ultimate challenge where the game ends after 3 wrong answers</li>
            <li><strong>Undo:</strong> Revert your last move(s) at any time</li>
            <li><strong>Color Profiles:</strong> Choose from 7 color themes to personalize your board</li>
            <li><strong>Save & Resume:</strong> Your game saves automatically when you leave and you can continue where you left off</li>
            <li><strong>Settings:</strong> Fine-tune your gameplay preferences and visual style</li>
          </ul>
        </div>

        <div className="instructions-section">
          <h3 className="instructions-title">⭐ Scoring System</h3>
          <ul className="instructions-list">
            <li><strong>Base Score:</strong> 1.000 (Easy) → 25.000 (Insane) points</li>
            <li><strong>Speed Bonus:</strong> Faster completion = higher score multiplier</li>
            <li><strong>Mistake Penalty:</strong> Each mistake reduces score (0 mistakes = 100%, 5+ = 50%)</li>
            <li><strong>Auto-Candidate Penalty:</strong> Using hints reduces multiplier to 40%</li>
            <li><strong>Leaderboards:</strong> Ranked by difficulty and strategy</li>
          </ul>
        </div>
      </DialogContent>
      <DialogActions sx={{ padding: '16px 24px', gap: '12px', justifyContent: 'center' }}>
        <Button
          onClick={onClose}
          variant="contained"
          sx={{
            backgroundColor: colorProfiles[colorProfile]?.intensive || '#ffa500',
            color: 'white',
            fontWeight: 'bold',
            '&:hover': {
              backgroundColor: colorProfiles[colorProfile]?.intensive ? `${colorProfiles[colorProfile].intensive}dd` : '#ff8c00',
            },
          }}
        >
          Got It!
        </Button>
      </DialogActions>
    </Dialog>
  );
}
