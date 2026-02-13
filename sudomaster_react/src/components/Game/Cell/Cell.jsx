import React from 'react';
import './Cell.css';

const Cell = ({
  value,
  candidates,
  isSelected,
  isHighlightedByLocation,
  isHighlightedByValue,
  isPrefilled,
  isCandidateMode,
  isDuplicate,
  isErrorCell,
  onClick,
  colorProfile,
  fontSize,
}) => {
  const getHighlightClass = () => {
    if (isSelected) return 'cell--selected';
    if (isHighlightedByValue) return 'cell--highlighted-value';
    if (isHighlightedByLocation && isPrefilled) return 'cell--prefilled-highlighted';
    if (isHighlightedByLocation) return 'cell--highlighted';
    if (isDuplicate) return 'cell--duplicate';
    if (isPrefilled) return 'cell--prefilled';
    return 'cell--empty';
  };

  const candidatesArray = Array.isArray(candidates) ? candidates : [];

  return (
    <div
      className={`cell ${getHighlightClass()} ${isErrorCell ? 'cell--error-shake' : ''} ${fontSize === 'large' ? 'cell--large-font' : fontSize === 'medium' ? 'cell--medium-font' : ''}`}
      onClick={onClick}
      style={{
        '--color-intensive': colorProfile?.intensive || '#ffa500',
        '--color-light': colorProfile?.light || '#ffe4cc',
        '--color-prefilled-light': colorProfile?.prefilledLight || '#efefe2',
      }}
    >
      {isCandidateMode && candidatesArray.length > 0 && !value ? (
        <div className="cell__candidates">
          {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
            <div
              key={num}
              className={`cell__candidate ${
                candidatesArray.includes(num) ? 'cell__candidate--active' : ''
              }`}
            >
              {candidatesArray.includes(num) ? num : ''}
            </div>
          ))}
        </div>
      ) : (
        <>
          <span className="cell__value">{value}</span>
          {candidatesArray.length > 0 && !value && (
            <div className="cell__candidates-background">
              {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                <div
                  key={num}
                  className={`cell__candidate-dot ${
                    candidatesArray.includes(num) ? 'cell__candidate-dot--active' : ''
                  }`}
                >
                  {candidatesArray.includes(num) ? num : ''}
                </div>
              ))}
            </div>
          )}
          {isDuplicate && <div className="cell__duplicate-dot"></div>}
        </>
      )}
    </div>
  );
};

export default Cell;
