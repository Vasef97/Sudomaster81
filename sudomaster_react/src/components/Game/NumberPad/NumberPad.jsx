import React, { useEffect } from 'react';
import './NumberPad.css';
import './NumberPadCandidate.css';

const NumberPad = ({
  onNumberClick,
  onClear,
  onUndo,
  isCandidateMode,
  onToggleMode,
  isAutoCandidateMode,
  onToggleAutoCandidateMode,
}) => {
  const numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9];

  useEffect(() => {
    const handleKeyDown = (event) => {
      if ((event.ctrlKey || event.metaKey) && event.key === 'z') {
        event.preventDefault();
        onUndo();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onUndo]);

  const handleNumberClick = (num) => {
    onNumberClick(num, isCandidateMode);
  };

  return (
    <div className="numpad">
      <div className="numpad__tabs">
        <button
          className={`numpad__tab ${!isCandidateMode ? 'numpad__tab--active' : ''}`}
          onClick={() => onToggleMode()}
        >
          Normal
        </button>
        <button
          className={`numpad__tab ${isCandidateMode ? 'numpad__tab--active' : ''}`}
          onClick={() => onToggleMode()}
        >
          Candidate
        </button>
      </div>

      <div className={`numpad__buttons ${isCandidateMode ? 'numpad__buttons--candidate' : ''}`}>
        {numbers.map((num) => (
          <button
            key={num}
            className={`numpad__button numpad__button--${isCandidateMode ? 'candidate' : 'normal'}`}
            onClick={() => handleNumberClick(num)}
          >
            {isCandidateMode ? (
              <div className="numpad__button-grid">
                {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((digit) => (
                  <span key={digit} className={`numpad__button-digit ${digit === num ? 'numpad__button-digit--active' : ''}`}>
                    {digit}
                  </span>
                ))}
              </div>
            ) : (
              num
            )}
          </button>
        ))}
      </div>

      <div className="numpad__controls">
        <button className="numpad__clear" onClick={onClear}>
          ✕
        </button>
        <button className="numpad__undo" onClick={onUndo}>
          Undo
        </button>
      </div>
    </div>
  );
};

export default NumberPad;
