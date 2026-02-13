import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
} from '@mui/material';
import { useDialogKeyboardShortcuts } from '../../../hooks/useDialogKeyboardShortcuts';
import './AboutDialog.css';

export default function AboutDialog({ open, onClose, colorProfile = 'orange', colorProfiles = {} }) {
  const dynamicColor = colorProfiles[colorProfile]?.intensive || '#ffa500';
  const { handleKeyDown } = useDialogKeyboardShortcuts({ onClose });

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="lg"
      fullWidth
      className="about-dialog"
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
      <DialogTitle sx={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#1a1a1a', borderBottom: `2px solid ${dynamicColor}`, paddingBottom: '16px' }}>
        About Sudomaster<span style={{ color: dynamicColor, fontWeight: 'bold' }}>81</span>
      </DialogTitle>

      <DialogContent sx={{ padding: '24px', fontSize: '0.95rem', lineHeight: '1.6', color: '#333', maxHeight: '75vh', overflowY: 'auto' }}>
        <section style={{ marginBottom: '2.5rem' }}>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#1a1a1a', marginTop: 16, marginBottom: '1rem' }}>Project Overview</h2>
          <p style={{ color: '#555', marginBottom: '1rem' }}>
            Sudomaster<span style={{ color: dynamicColor, fontWeight: 'bold' }}>81</span> is a full-stack, fully responsive web application meticulously engineered to deliver a superior Sudoku solving experience. Built as a comprehensive exercise in modern web architecture, this application combines sophisticated puzzle generation algorithms, intelligent scoring mechanics, and an intuitive user interface designed for both casual players and dedicated Sudoku enthusiasts.
          </p>
          <p style={{ color: '#555' }}>
            Every aspect of Sudomaster<span style={{ color: dynamicColor, fontWeight: 'bold' }}>81</span>, from puzzle difficulty calibration to scoring precision, has been crafted with exacting standards to satisfy the most discerning puzzle solvers. The application seamlessly adapts to any device, providing a consistent, refined experience whether you're solving on desktop, tablet, or mobile.
          </p>
        </section>

        <section style={{ marginBottom: '2.5rem' }}>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#1a1a1a', marginTop: '1.5rem', marginBottom: '1rem' }}>Sudokugen: Intelligent Puzzle Generation</h2>
          <p style={{ color: '#555', marginBottom: '1rem' }}>
            Sudomaster<span style={{ color: dynamicColor, fontWeight: 'bold' }}>81</span> utilizes advanced puzzle generation technology to create unique, mathematically valid Sudoku puzzles across four distinct difficulty levels. Our Sudokugen system employs a sophisticated multi-stage process:
          </p>

          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: '#1a1a1a', marginTop: '1.5rem', marginBottom: '1rem' }}>Generation Algorithm</h3>
          <ul style={{ color: '#555', marginBottom: '1.5rem', paddingLeft: '1.5rem' }}>
            <li style={{ marginBottom: '0.5rem' }}><strong>Complete Board Generation:</strong> Creates a fully solved 9x9 Sudoku grid using randomized backtracking with constraint satisfaction</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>Strategic Cell Removal:</strong> Carefully removes cells while maintaining solution uniqueness verification</li>
            <li><strong>Difficulty Calibration:</strong> Analyzes required solving techniques to classify puzzle difficulty accurately</li>
          </ul>

          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: '#1a1a1a', marginTop: '1.5rem', marginBottom: '1rem' }}>Difficulty Levels & Solving Techniques</h3>

          <div style={{ background: '#f9f9f9', padding: '1.2rem', borderRadius: '8px', marginBottom: '1rem', borderLeft: '4px solid #aed581' }}>
            <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#1a1a1a', marginTop: 0, marginBottom: '0.5rem' }}>🟢 EASY</h4>
            <p style={{ color: '#555', marginBottom: '0.5rem' }}><strong>Cells Remaining:</strong> 36-41 of 81</p>
            <p style={{ color: '#555', fontWeight: 600, marginBottom: '0.5rem' }}>Primary Techniques:</p>
            <ul style={{ color: '#555', paddingLeft: '1.5rem', marginBottom: '0.5rem' }}>
              <li>Single Candidate: A cell has only one possible value</li>
              <li>Single Position: A digit appears in only one cell in a row/column/box</li>
              <li>Naked Pair: Two cells with identical candidate pairs</li>
              <li>Hidden Pair: Two digits restricted to two specific cells</li>
            </ul>
            <p style={{ color: '#555', fontStyle: 'italic', marginBottom: 0 }}>Perfect for beginners, solvable through basic logical deduction without advanced strategies.</p>
          </div>

          <div style={{ background: '#f9f9f9', padding: '1.2rem', borderRadius: '8px', marginBottom: '1rem', borderLeft: '4px solid #f0e68c' }}>
            <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#1a1a1a', marginTop: 0, marginBottom: '0.5rem' }}>🟡 MEDIUM</h4>
            <p style={{ color: '#555', marginBottom: '0.5rem' }}><strong>Cells Remaining:</strong> 31-35 of 81</p>
            <p style={{ color: '#555', fontWeight: 600, marginBottom: '0.5rem' }}>Primary Techniques:</p>
            <ul style={{ color: '#555', paddingLeft: '1.5rem', marginBottom: '0.5rem' }}>
              <li>Pointing Pair: Box to row/column exclusion patterns</li>
              <li>Box/Line Reduction: Line to box constraint analysis</li>
              <li>Naked Triple: Three cells with three combined candidates</li>
              <li>Hidden Triple: Three digits restricted to three cells</li>
            </ul>
            <p style={{ color: '#555', fontStyle: 'italic', marginBottom: 0 }}>Intermediate challenge requiring pattern recognition and multi-step logical reasoning.</p>
          </div>

          <div style={{ background: '#f9f9f9', padding: '1.2rem', borderRadius: '8px', marginBottom: '1rem', borderLeft: '4px solid #ffa500' }}>
            <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#1a1a1a', marginTop: 0, marginBottom: '0.5rem' }}>🟠 HARD</h4>
            <p style={{ color: '#555', marginBottom: '0.5rem' }}><strong>Cells Remaining:</strong> 26-30 of 81</p>
            <p style={{ color: '#555', fontWeight: 600, marginBottom: '0.5rem' }}>Primary Techniques:</p>
            <ul style={{ color: '#555', paddingLeft: '1.5rem', marginBottom: '0.5rem' }}>
              <li>X-Wing: 2×2 intersection patterns across rows/columns</li>
              <li>XY-Wing: Three cell chain deduction</li>
              <li>Simple Coloring: Binary logical alternation chains</li>
              <li>Swordfish: 3×3 extension of X-Wing patterns</li>
            </ul>
            <p style={{ color: '#555', fontStyle: 'italic', marginBottom: 0 }}>Advanced puzzles requiring sophisticated pattern analysis and strategic candidate tracking.</p>
          </div>

          <div style={{ background: '#f9f9f9', padding: '1.2rem', borderRadius: '8px', marginBottom: '1rem', borderLeft: '4px solid #ff3300' }}>
            <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#1a1a1a', marginTop: 0, marginBottom: '0.5rem' }}>🔥 INSANE</h4>
            <p style={{ color: '#555', marginBottom: '0.5rem' }}><strong>Cells Remaining:</strong> 21-25 of 81</p>
            <p style={{ color: '#555', fontWeight: 600, marginBottom: '0.5rem' }}>Primary Techniques:</p>
            <ul style={{ color: '#555', paddingLeft: '1.5rem', marginBottom: '0.5rem' }}>
              <li>Advanced Coloring: Complex logical chain patterns</li>
              <li>Forcing Chains: Recursive hypothesis testing with multiple branches</li>
              <li>Nishio: Nested contradiction analysis</li>
              <li>Guessing: Trial and error as last resort (indicates extreme complexity)</li>
            </ul>
            <p style={{ color: '#555', fontStyle: 'italic', marginBottom: 0 }}>Elite difficulty. Only for serious Sudoku practitioners. Tests mastery of all solving techniques.</p>
          </div>
        </section>

        <section style={{ marginBottom: '2.5rem' }}>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#1a1a1a', marginTop: 0, marginBottom: '1rem' }}>Scoring Engine</h2>
          <p style={{ color: '#555', marginBottom: '1.5rem' }}>
            Sudomaster<span style={{ color: dynamicColor, fontWeight: 'bold' }}>81</span>'s scoring system is precision-engineered to reward speed, accuracy, and skill while penalizing inefficiency. Every completed puzzle generates a score based on four independent multiplier factors:
          </p>

          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: '#1a1a1a', marginTop: '1.5rem', marginBottom: '1rem' }}>Base Score by Difficulty</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem 1rem', background: '#f5f5f5', borderRadius: '4px' }}>
              <span style={{ color: '#555', fontWeight: 600 }}>EASY</span>
              <span style={{ color: '#1a1a1a', fontWeight: 700 }}>1.000 pts</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem 1rem', background: '#f5f5f5', borderRadius: '4px' }}>
              <span style={{ color: '#555', fontWeight: 600 }}>MEDIUM</span>
              <span style={{ color: '#1a1a1a', fontWeight: 700 }}>3.000 pts</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem 1rem', background: '#f5f5f5', borderRadius: '4px' }}>
              <span style={{ color: '#555', fontWeight: 600 }}>HARD</span>
              <span style={{ color: '#1a1a1a', fontWeight: 700 }}>10.000 pts</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem 1rem', background: '#f5f5f5', borderRadius: '4px' }}>
              <span style={{ color: '#555', fontWeight: 600 }}>INSANE</span>
              <span style={{ color: '#1a1a1a', fontWeight: 700 }}>25.000 pts</span>
            </div>
          </div>

          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: '#1a1a1a', marginTop: '1.5rem', marginBottom: '1rem' }}>Speed Multiplier</h3>
          <p style={{ color: '#555', marginBottom: '0.5rem' }}>
            <strong>Formula:</strong> max(0,3, 1,8 - (TimeInMinutes / TimeFactor))
          </p>
          <p style={{ color: '#555', marginBottom: '1rem' }}>
            Speed is rewarded generously. Complete a puzzle near the expected time for your difficulty and maintain 100%+ bonus. Solving slowly reduces your multiplier—minimum floor is 30%, ensuring even patient solvers retain meaningful points.
          </p>
          <ul style={{ color: '#555', paddingLeft: '1.5rem', marginBottom: '1.5rem' }}>
            <li style={{ marginBottom: '0.5rem' }}><strong>Fast (under half expected time):</strong> 1,8x multiplier (180% base points)</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>On time (expected duration):</strong> 0,8x-1,0x multiplier (80-100%)</li>
            <li><strong>Slow (2× expected time):</strong> 0,3x multiplier (30% minimum floor)</li>
          </ul>

          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: '#1a1a1a', marginTop: '1.5rem', marginBottom: '1rem' }}>Accuracy Penalty</h3>
          <p style={{ color: '#555', marginBottom: '1rem' }}>
            Every mistake (when Error Indicator is enabled) reduces your score progressively. With the optional <strong>3 Mistake Limit</strong> enabled, reaching 3 mistakes ends the game immediately and no score is awarded.
          </p>
          <ul style={{ color: '#555', paddingLeft: '1.5rem', marginBottom: '1.5rem' }}>
            <li style={{ marginBottom: '0.5rem' }}><strong>0 mistakes:</strong> 1,00x (100% - no penalty)</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>1 mistake:</strong> 0,95x (95% - 5% reduction)</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>2 mistakes:</strong> 0,90x (90% - 10% reduction)</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>3 mistakes:</strong> 0,80x (80% - 20% reduction)</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>4 mistakes:</strong> 0,65x (65% - 35% reduction)</li>
            <li><strong>5+ mistakes:</strong> 0,50x (50% - 50% reduction)</li>
          </ul>

          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: '#1a1a1a', marginTop: '1.5rem', marginBottom: '1rem' }}>Assistance Penalty</h3>
          <p style={{ color: '#555', marginBottom: '1rem' }}>
            Using Auto-Candidate Mode (automatic candidate generation) incurs a strategic penalty:
          </p>
          <ul style={{ color: '#555', paddingLeft: '1.5rem', marginBottom: '1.5rem' }}>
            <li style={{ marginBottom: '0.5rem' }}><strong>Without Auto-Candidate:</strong> 1,0x (100% - pure strategy)</li>
            <li><strong>With Auto-Candidate:</strong> 0,4x (40% - 60% reduction)</li>
          </ul>

          <h3 style={{ fontSize: '1.2rem', fontWeight: 600, color: '#1a1a1a', marginTop: '1.5rem', marginBottom: '1rem' }}>Final Score Calculation</h3>
          <div style={{ background: '#fafafa', padding: '1.5rem', borderRadius: '8px', borderLeft: `4px solid ${dynamicColor}` }}>
            <p style={{ color: '#1a1a1a', marginBottom: '1rem' }}>
              <strong>Final Score = Base Score × Speed Multiplier × Accuracy Penalty × Assistance Penalty</strong>
            </p>
            <p style={{ color: '#555', fontStyle: 'italic', marginBottom: 0 }}>
              <em>Example: HARD puzzle (10.000 pts), solved in 8 minutes, 2 mistakes, no auto-candidate</em><br/>
              = 10.000 × 1,533 × 0,90 × 1,0 = <strong style={{ color: '#1a1a1a' }}>13.797 pts</strong>
            </p>
          </div>
        </section>

        <section style={{ marginBottom: '2.5rem' }}>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#1a1a1a', marginTop: 0, marginBottom: '1rem' }}>Technology Stack</h2>
          <ul style={{ color: '#555', paddingLeft: '1.5rem', marginBottom: '1.5rem' }}>
            <li style={{ marginBottom: '0.5rem' }}><strong>Spring Boot:</strong> Java enterprise framework</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>PostgreSQL:</strong> Relational database</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>Docker:</strong> Containerization for consistent deployment</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>Spring Security:</strong> Authentication & authorization</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>JWT:</strong> Stateless token-based authentication</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>React 18:</strong> Modern UI with hooks and state management</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>Axios:</strong> HTTP client for API communication</li>
            <li style={{ marginBottom: '0.5rem' }}><strong>Material-UI:</strong> Professional component library</li>
            <li><strong>Swagger / OpenAPI:</strong> Interactive API documentation</li>
          </ul>
        </section>

        <section style={{ borderTop: '1px solid #eee', paddingTop: '2rem', marginTop: '2rem' }}>
          <p style={{ color: '#555', marginBottom: '1rem', textAlign: 'center', fontStyle: 'italic' }}>
            Sudomaster<span style={{ color: dynamicColor, fontWeight: 'bold' }}>81</span> represents a commitment to excellence in puzzle design, game mechanics, and user experience. Every line of code, every algorithm, and every interface element has been crafted to deliver an uncompromising Sudoku solving experience.
          </p>
          <p style={{ color: '#555', textAlign: 'center', fontStyle: 'italic' }}>
            Built with passion for Sudoku enthusiasts. Enjoy the challenge.
          </p>
        </section>
      </DialogContent>

      <DialogActions sx={{ padding: '16px 24px', gap: '12px', justifyContent: 'center' }}>
        <Button
          onClick={onClose}
          variant="contained"
          sx={{
            backgroundColor: dynamicColor,
            color: 'white',
            fontWeight: 'bold',
            padding: '10px 30px',
            fontSize: '1rem',
            '&:hover': {
              backgroundColor: dynamicColor,
              opacity: 0.8,
            },
          }}
        >
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
}
