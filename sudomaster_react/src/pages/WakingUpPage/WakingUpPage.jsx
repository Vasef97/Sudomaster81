import React from 'react';
import { CircularProgress } from '@mui/material';
import networkServerImg from '../../assets/network-server.png';
import './WakingUpPage.css';

const WakingUpPage = () => {
  return (
    <div className="waking-up-page">
      <div className="waking-up-page__container">
        <div className="waking-up-page__image-wrapper">
          <img
            src={networkServerImg}
            alt="Connecting to server"
            className="waking-up-page__image"
          />
        </div>
        <CircularProgress
          size={44}
          thickness={4}
          sx={{ color: '#999' }}
        />
        <h1 className="waking-up-page__title">Loading</h1>
        <p className="waking-up-page__message">
          The server is starting up. Please wait a few seconds...
        </p>
      </div>
    </div>
  );
};

export default WakingUpPage;
