import React from 'react';
import { Button } from '@mui/material';
import serverErrorImg from '../../assets/server-error.png';
import './ServerErrorPage.css';

const ServerErrorPage = ({ onRetry }) => {
  return (
    <div className="server-error-page">
      <div className="server-error-page__container">
        <div className="server-error-page__image-wrapper">
          <img
            src={serverErrorImg}
            alt="Server Error"
            className="server-error-page__image"
          />
        </div>
        <h1 className="server-error-page__title">Server Error</h1>
        <p className="server-error-page__message">
          We're sorry, but the server is currently unavailable. Please try again later.
        </p>
        <Button
          variant="contained"
          onClick={onRetry}
          sx={{
            backgroundColor: '#ffa500',
            color: 'white',
            padding: '0.8rem 2rem',
            fontSize: '1rem',
            fontWeight: 'bold',
            '&:hover': {
              backgroundColor: '#ff8c00',
            },
            marginTop: '2rem',
          }}
        >
          Try Again
        </Button>
      </div>
    </div>
  );
};

export default ServerErrorPage;
