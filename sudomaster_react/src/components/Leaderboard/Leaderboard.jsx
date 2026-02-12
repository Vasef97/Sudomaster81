import React, { useState, useEffect, useRef } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  CircularProgress,
  Alert,
  Box,
  TextField,
  Backdrop,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { gameService } from '../../services/authService';
import { formatTime } from '../../utils/formatters';
import './Leaderboard.css';

export default function Leaderboard({ open, onClose, currentUsername, accentColor = '#ffa500' }) {
  const [selectedTab, setSelectedTab] = useState('EASY');
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [isSearching, setIsSearching] = useState(false);

  const difficulties = ['EASY', 'MEDIUM', 'HARD', 'INSANE'];

  useEffect(() => {
    if (open) {
      let cancelled = false;
      const loadData = async () => {
        setLoading(true);
        setError('');
        try {
          const data = await gameService.getLeaderboard(selectedTab, 50);
          if (cancelled) return;
          if (data && data.data && Array.isArray(data.data)) {
            setLeaderboard(data.data);
          } else if (Array.isArray(data)) {
            setLeaderboard(data);
          } else {
            setLeaderboard([]);
          }
        } catch (err) {
          if (!cancelled) setError(`Failed to load leaderboard: ${err.message}`);
        } finally {
          if (!cancelled) setLoading(false);
        }
      };
      loadData();
      setSearchQuery('');
      setIsSearching(false);
      return () => { cancelled = true; };
    }
  }, [open, selectedTab]);

  const fetchLeaderboard = async (difficulty) => {
    setLoading(true);
    setError('');
    try {
      const data = await gameService.getLeaderboard(difficulty, 50);
      
      if (data && data.data && Array.isArray(data.data)) {
        setLeaderboard(data.data);
      } else if (Array.isArray(data)) {
        setLeaderboard(data);
      } else if (data === null || data === undefined) {
        setLeaderboard([]);
      } else {
        setLeaderboard([]);
      }
    } catch (err) {
      setError(`Failed to load leaderboard: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (event, newValue) => {
    setSelectedTab(newValue);
  };

  const fetchPlayerAcrossAllDifficulties = async (playerName) => {
    setLoading(true);
    setError('');
    try {
      const allResults = [];
      
      for (const difficulty of difficulties) {
        const data = await gameService.getLeaderboard(difficulty, 100);
        const leaderboardData = Array.isArray(data) ? data : (data?.data || []);
        allResults.push(...leaderboardData);
      }
      
      const filtered = allResults.filter(entry =>
        (entry.username || 'Anonymous').toLowerCase().includes(playerName.toLowerCase())
      );
      const sorted = filtered.sort((a, b) => b.score - a.score);
      setLeaderboard(sorted);
      setIsSearching(true);
    } catch (err) {
      setError(`Failed to search: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const searchDebounceRef = useRef(null);

  const handleSearchChange = (event) => {
    const query = event.target.value;
    setSearchQuery(query);
    
    if (searchDebounceRef.current) clearTimeout(searchDebounceRef.current);
    
    if (query.trim()) {
      searchDebounceRef.current = setTimeout(() => {
        fetchPlayerAcrossAllDifficulties(query);
      }, 400);
    } else {
      setIsSearching(false);
      fetchLeaderboard(selectedTab);
    }
  };

  const filteredLeaderboard = leaderboard.filter(entry =>
    (entry.username || 'Anonymous').toLowerCase().includes(searchQuery.toLowerCase())
  ).slice(0, 50);

  return (
    <>
      <Backdrop
        sx={{
          color: '#fff',
          zIndex: 1300,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 2,
        }}
        open={loading}
      >
        <CircularProgress color="inherit" size={60} />
        <p style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>Loading...</p>
      </Backdrop>
      <Dialog
        open={open}
        onClose={onClose}
        maxWidth="sm"
        fullWidth
        disableEnforceFocus
        disableAutoFocus
        disableRestoreFocus
        PaperProps={{
          sx: {
            width: { xs: '500px', sm: '540px' },
            maxWidth: { xs: '500px', sm: '540px' },
            height: { xs: '520px', sm: '580px' },
            borderRadius: '16px',
            boxShadow: '0 8px 32px rgba(0, 0, 0, 0.15)',
            display: 'flex',
            flexDirection: 'column',
            '@media (max-width: 810px)': {
              overflow: 'hidden',
            },
          },
        }}
      >
      <DialogTitle
        sx={{
          fontSize: { xs: '1.1rem', sm: '1.5rem' },
          fontWeight: 'bold',
          color: '#1a1a1a',
          borderBottom: `2px solid ${accentColor}`,
          paddingBottom: '16px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '12px',
          flexWrap: 'wrap',
        }}
      >
        <span>🏆 Leaderboard</span>
        <TextField
          placeholder="Search player..."
          size="small"
          value={searchQuery}
          onChange={handleSearchChange}
          InputProps={{
            startAdornment: <SearchIcon sx={{ mr: 1, color: '#666', fontSize: { xs: '1.2rem', sm: '1.5rem' } }} />,
          }}
          sx={{
            width: { xs: '140px', sm: '200px' },
            '& .MuiOutlinedInput-root': {
              fontSize: { xs: '0.75rem', sm: '0.9rem' },
              height: { xs: '32px', sm: '36px' },
              borderRadius: '8px',
            },
            '& .MuiOutlinedInput-input::placeholder': {
              fontSize: { xs: '0.75rem', sm: '0.9rem' },
            },
          }}
        />
      </DialogTitle>

      <DialogContent sx={{ padding: '24px', paddingTop: isSearching ? '32px' : '24px', display: 'flex', flexDirection: 'column', overflow: 'auto', '@media (max-width: 810px)': { overflowY: 'auto', overflowX: 'hidden' } }}>
        {!isSearching && (
          <Tabs
            value={selectedTab}
            onChange={handleTabChange}
            variant="fullWidth"
            sx={{
              marginBottom: '32px',
              '& .MuiTab-root': {
                fontSize: { xs: '0.6rem', sm: '0.9rem' },
                fontWeight: '600',
                color: '#666',
                padding: { xs: '8px 0px', sm: '12px 16px' },
                minWidth: { xs: '50px', sm: 'auto' },
                '&.Mui-selected': {
                  color: accentColor,
                },
              },
              '& .MuiTabs-indicator': {
                backgroundColor: accentColor,
                height: '3px',
              },
            }}
          >
            {difficulties.map(difficulty => (
              <Tab key={difficulty} label={difficulty} value={difficulty} />
            ))}
          </Tabs>
        )}

        {loading ? (
          <Box display="flex" justifyContent="center" alignItems="center" minHeight="300px" sx={{ marginTop: isSearching ? '32px' : '0' }}>
            <CircularProgress sx={{ color: accentColor }} />
          </Box>
        ) : error ? (
          <Alert severity="error" sx={{ mb: 2, marginTop: isSearching ? '32px' : '0' }}>
            {error}
          </Alert>
        ) : leaderboard.length === 0 ? (
          <Alert severity="info" sx={{ mb: 2, marginTop: isSearching ? '32px' : '0' }}>
            {isSearching ? 'No players found with that name.' : `No completions yet for ${selectedTab} difficulty. Be the first to complete a puzzle!`}
          </Alert>
        ) : filteredLeaderboard.length === 0 ? (
          <Alert severity="info" sx={{ mb: 2, marginTop: isSearching ? '32px' : '0' }}>
            No players found with that name.
          </Alert>
        ) : (
          <>
            <TableContainer component={Paper} sx={{ boxShadow: 'none', border: '1px solid #eee', mb: 0, marginTop: isSearching ? '32px' : '0' }}>
              <Table size="small" sx={{ tableLayout: 'fixed', width: '100%' }}>
                <TableHead sx={{ backgroundColor: '#f5f5f5' }}>
                  <TableRow>
                    <TableCell align="left" sx={{ fontWeight: 'bold', color: '#1a1a1a', width: isSearching ? '16.66%' : '20%', textAlign: 'left' }}>Rank</TableCell>
                    <TableCell align="left" sx={{ fontWeight: 'bold', color: '#1a1a1a', width: isSearching ? '16.66%' : '20%', textAlign: 'left' }}>Player</TableCell>
                    {isSearching && <TableCell align="left" sx={{ fontWeight: 'bold', color: '#1a1a1a', width: '16.66%', textAlign: 'left' }}>Difficulty</TableCell>}
                    <TableCell align="left" sx={{ fontWeight: 'bold', color: '#1a1a1a', width: isSearching ? '16.66%' : '20%', textAlign: 'left' }}>Assisted</TableCell>
                    <TableCell align="left" sx={{ fontWeight: 'bold', color: '#1a1a1a', width: isSearching ? '16.66%' : '20%', textAlign: 'left' }}>
                      Time
                    </TableCell>
                    <TableCell align="left" sx={{ fontWeight: 'bold', color: '#1a1a1a', width: isSearching ? '16.66%' : '20%', textAlign: 'left' }}>
                      Score
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                {filteredLeaderboard.map((entry, index) => {
                  const globalRank = leaderboard.indexOf(entry) + 1;
                  const isCurrentUser = currentUsername && entry.username === currentUsername;
                  return (
                    <TableRow
                      key={`${entry.username}-${entry.score}-${entry.difficulty || selectedTab}-${index}`}
                      sx={{
                        '&:hover': {
                          backgroundColor: '#fff9f0',
                        },
                        '&:nth-of-type(odd)': {
                          backgroundColor: '#fafafa',
                        },
                      }}
                    >
                      <TableCell
                        align="left"
                        sx={{
                          fontWeight: isCurrentUser ? '700' : '500',
                          color: globalRank === 1 ? accentColor : '#1a1a1a',
                          fontSize: { xs: 'clamp(0.65rem, 2.5vw, 0.9rem)', sm: '1rem' },
                          width: isSearching ? '16.66%' : '20%',
                          textAlign: 'left',
                        }}
                      >
                        {globalRank === 1 ? '🥇' : globalRank === 2 ? '🥈' : globalRank === 3 ? '🥉' : globalRank}
                      </TableCell>
                      <TableCell align="left" sx={{ fontWeight: isCurrentUser ? '700' : '500', color: '#1a1a1a', fontSize: { xs: 'clamp(0.65rem, 2.5vw, 0.9rem)', sm: '1rem' }, width: isSearching ? '16.66%' : '20%', textAlign: 'left' }}>
                        {(entry.username || 'Anonymous')}
                      </TableCell>
                      {isSearching && (
                        <TableCell align="left" sx={{ fontWeight: isCurrentUser ? '700' : '500', color: '#1a1a1a', fontSize: { xs: 'clamp(0.65rem, 2.5vw, 0.9rem)', sm: '1rem' }, width: '16.66%', textAlign: 'left' }}>
                          {entry.difficulty}
                        </TableCell>
                      )}
                      <TableCell align="center" sx={{ fontWeight: isCurrentUser ? '700' : '500', color: '#1a1a1a', fontSize: { xs: 'clamp(0.65rem, 2.5vw, 0.9rem)', sm: '1rem' }, width: isSearching ? '16.66%' : '20%' }}>
                        {(entry.mistakes > 0 || entry.autoCandidateMode || entry.assist) ? 'Yes' : 'No'}
                      </TableCell>
                      <TableCell align="left" sx={{ fontWeight: isCurrentUser ? '700' : '500', color: '#1a1a1a', fontSize: { xs: 'clamp(0.65rem, 2.5vw, 0.9rem)', sm: '1rem' }, width: isSearching ? '16.66%' : '20%', textAlign: 'left' }}>
                        {formatTime(entry.elapsedTimeSeconds)}
                      </TableCell>
                      <TableCell align="left" sx={{ fontWeight: isCurrentUser ? '700' : 'bold', color: accentColor, fontSize: { xs: 'clamp(0.65rem, 2.5vw, 0.9rem)', sm: '1rem' }, width: isSearching ? '16.66%' : '20%', textAlign: 'left' }}>
                        {entry.score || 0}
                      </TableCell>
                    </TableRow>
                  );
                })}
                </TableBody>
              </Table>
            </TableContainer>
          </>
        )}
      </DialogContent>

      <DialogActions sx={{ padding: '12px 16px', borderTop: '1px solid #eee', display: 'flex', flexDirection: 'column', gap: '16px', alignItems: 'center', justifyContent: 'center' }}>
        <Button
          onClick={onClose}
          variant="contained"
          size="small"
          sx={{
            backgroundColor: '#1a1a1a',
            color: '#fff',
            fontSize: { xs: '0.8rem', sm: '1rem' },
            padding: { xs: '6px 16px', sm: '8px 24px' },
            '&:hover': {
              backgroundColor: '#333',
            },
          }}
        >
          Close
        </Button>
      </DialogActions>
      </Dialog>
    </>
  );
}
