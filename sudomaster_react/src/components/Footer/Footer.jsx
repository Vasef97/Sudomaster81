import React from 'react';
import { Button } from '@mui/material';
import './Footer.css';

export default function Footer({ onAboutClick }) {
  return (
    <footer className="footer">
      <div className="footer__content">
        <Button
          onClick={() => window.open('https://github.com/Vasef97', '_blank')}
          sx={{
            color: '#1a1a1a',
            textTransform: 'none',
            fontSize: '0.8rem',
            fontWeight: '600',
            padding: 0,
            minWidth: 'auto',
            height: 'auto',
            lineHeight: 'inherit',
            display: 'inline-flex',
            alignItems: 'center',
            verticalAlign: 'baseline',
            transition: 'all 0.3s ease',
            '&:hover': {
              backgroundColor: 'transparent',
              transform: 'scale(1.05)',
            },
            '&:active': {
              transform: 'scale(0.95)',
            },
          }}
        >
          © 2026 <span style={{ fontWeight: 'bold', marginLeft: '0.25rem' }}>Vasef97</span>
        </Button>
        <span className="footer__separator">|</span>
        <Button
          onClick={onAboutClick}
          sx={{
            color: '#999',
            textTransform: 'none',
            fontSize: '0.8rem',
            fontWeight: '600',
            padding: 0,
            minWidth: 'auto',
            height: 'auto',
            lineHeight: 'inherit',
            display: 'inline-flex',
            alignItems: 'center',
            verticalAlign: 'baseline',
            transition: 'all 0.3s ease',
            '&:hover': {
              backgroundColor: 'transparent',
              transform: 'scale(1.05)',
            },
            '&:active': {
              transform: 'scale(0.95)',
            },
          }}
        >
          About
        </Button>
      </div>
    </footer>
  );
}
