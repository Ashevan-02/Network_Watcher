import { useState } from 'react';
import { Box, Card, CardContent, TextField, Button, Typography, Alert } from '@mui/material';
import axios from 'axios';

export const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    try {
      // Direct API call
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        username,
        password
      });
      
      // Save token and user
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify({ username: response.data.username }));
      
      // Force page reload to dashboard
      window.location.href = '/';
    } catch (err) {
      setLoading(false);
      setError('Invalid username or password');
    }
  };

  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh" bgcolor="background.default">
      <Card sx={{ maxWidth: 400, width: '100%', m: 2 }}>
        <CardContent>
          <Typography variant="h4" align="center" gutterBottom>
            🌐 Network Watcher
          </Typography>
          <Typography variant="body2" align="center" color="text.secondary" mb={3}>
            Sign in to monitor your network
          </Typography>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              margin="normal"
              required
            />
            <Button fullWidth variant="contained" type="submit" sx={{ mt: 3 }} disabled={loading}>
              {loading ? 'Logging in...' : 'Login'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
};
