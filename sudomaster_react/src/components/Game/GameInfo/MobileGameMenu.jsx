import React, { useState } from 'react';
import { IconButton, Menu, MenuItem } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import './MobileGameMenu.css';

const MobileGameMenu = ({
  onRestart,
  onExit,
  onInfo,
  onSettings,
  onLogout,
  colorProfile = {},
}) => {
  const [mobileMenuAnchor, setMobileMenuAnchor] = useState(null);

  const handleMobileMenuOpen = (event) => {
    setMobileMenuAnchor(event.currentTarget);
  };

  const handleMobileMenuClose = () => {
    setMobileMenuAnchor(null);
  };

  const handleMobileRestart = () => {
    onRestart?.();
    handleMobileMenuClose();
  };

  const handleMobileExit = () => {
    onExit?.();
    handleMobileMenuClose();
  };

  const handleMobileSettings = () => {
    onSettings?.();
    handleMobileMenuClose();
  };

  const handleMobileLogout = () => {
    onLogout?.();
    handleMobileMenuClose();
  };

  const handleMobileInfo = () => {
    onInfo?.();
    handleMobileMenuClose();
  };

  return (
    <>
      <IconButton
        className="mobile-game-menu__btn"
        onClick={handleMobileMenuOpen}
        size="medium"
        sx={{
          color: '#1a1a1a',
          borderRadius: '8px',
          border: '2px solid #d3d3d3',
          backgroundColor: '#ffffff',
          padding: '0.65rem',
          display: 'none',
          '&:hover': {
            backgroundColor: '#f5f5f5',
          },
        }}
      >
        <MenuIcon sx={{ fontSize: '1.8rem' }} />
      </IconButton>

      <Menu
        anchorEl={mobileMenuAnchor}
        open={Boolean(mobileMenuAnchor)}
        onClose={handleMobileMenuClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
        PaperProps={{
          sx: {
            borderRadius: '12px',
            boxShadow: '0 8px 32px rgba(0, 0, 0, 0.15)',
            marginRight: '-8px',
            '& .MuiMenuItem-root': {
              fontSize: '1.3rem !important',
              fontWeight: '700 !important',
              padding: '0.85rem 1.5rem',
              color: '#1a1a1a',
              transition: 'all 0.2s ease',
              '&:hover': {
                backgroundColor: colorProfile?.light || '#f5f5dc',
              },
            },
          },
        }}
      >
        <MenuItem onClick={handleMobileRestart}>Restart</MenuItem>
        <MenuItem onClick={handleMobileExit}>Exit</MenuItem>
        {onInfo && <MenuItem onClick={handleMobileInfo}>Instructions</MenuItem>}
        {onSettings && <MenuItem onClick={handleMobileSettings}>Settings</MenuItem>}
        {onLogout && <MenuItem onClick={handleMobileLogout}>Logout</MenuItem>}
      </Menu>
    </>
  );
};

export default MobileGameMenu;
