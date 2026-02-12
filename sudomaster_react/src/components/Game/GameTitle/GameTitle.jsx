import React from 'react';
import SudokuIcon from '../../Common/SudokuIcon/SudokuIcon';
import './GameTitle.css';

export default function GameTitle({ colorProfile }) {
  return (
    <div className="game-title__container">
      <SudokuIcon size="tiny" colorProfile={colorProfile} />
      <h1 className="game-title__title">
        Sudomaster<span className="game-title__number" style={{ color: colorProfile?.intensive || '#ffa500' }}>81</span>
      </h1>
    </div>
  );
}
