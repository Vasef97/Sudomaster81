import React from 'react';
import './SudokuIcon.css';

const SudokuIcon = ({ size = 'medium', colorProfile }) => {
  return (
    <div className={`sudoku-icon sudoku-icon--${size}`}>
      <div className="sudoku-icon__grid">
        <div className="sudoku-icon__cell">1</div>
        <div className="sudoku-icon__cell">2</div>
        <div className="sudoku-icon__cell">3</div>
        <div className="sudoku-icon__cell sudoku-icon__cell--empty"></div>
        <div className="sudoku-icon__cell sudoku-icon__cell--highlight" style={{ backgroundColor: colorProfile?.intensive || '#ffa500' }}>?</div>
        <div className="sudoku-icon__cell sudoku-icon__cell--empty"></div>
        <div className="sudoku-icon__cell sudoku-icon__cell--empty"></div>
        <div className="sudoku-icon__cell sudoku-icon__cell--empty"></div>
        <div className="sudoku-icon__cell sudoku-icon__cell--empty"></div>
      </div>
    </div>
  );
};

export default SudokuIcon;
