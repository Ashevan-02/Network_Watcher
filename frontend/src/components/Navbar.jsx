import { AppBar, Toolbar, Typography, IconButton, Box, Badge } from '@mui/material';
import { Brightness4, Brightness7, Notifications, AccountCircle } from '@mui/icons-material';
import { useContext } from 'react';
import { ThemeContext } from '../context/ThemeContext';
import { useAuth } from '../hooks/useAuth';

export const Navbar = () => {
  const { mode, toggleTheme } = useContext(ThemeContext);
  const { user, logout } = useAuth();

  return (
    <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar>
        <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
          🌐 Network Watcher
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <IconButton color="inherit">
            <Badge badgeContent={3} color="error">
              <Notifications />
            </Badge>
          </IconButton>
          <IconButton onClick={toggleTheme} color="inherit">
            {mode === 'dark' ? <Brightness7 /> : <Brightness4 />}
          </IconButton>
          <IconButton color="inherit" onClick={logout}>
            <AccountCircle />
          </IconButton>
        </Box>
      </Toolbar>
    </AppBar>
  );
};
