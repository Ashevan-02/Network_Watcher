import { useState, useEffect } from 'react';
import { Box, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, CircularProgress, Chip, Alert, Snackbar, TextField, Card, CardContent } from '@mui/material';
import { PlayArrow } from '@mui/icons-material';
import { scanService } from '../services/scanService';
import { formatDate } from '../utils/formatters';

export const Scans = () => {
  const [scans, setScans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [networkRange, setNetworkRange] = useState('192.168.1.0/24');

  const fetchScans = async () => {
    try {
      const response = await scanService.getAllScans();
      setScans(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchScans();
  }, []);

  const handleStartScan = async () => {
    if (!networkRange.trim()) {
      setError('Please enter a network range');
      return;
    }
    setScanning(true);
    setError('');
    try {
      await scanService.scanNetwork(networkRange);
      setSuccess('Scan started successfully!');
      setTimeout(() => fetchScans(), 1000);
    } catch (err) {
      console.error('Scan error:', err);
      setError(err.response?.data?.message || err.message || 'Failed to start scan');
    } finally {
      setScanning(false);
    }
  };

  if (loading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;

  return (
    <Box>
      <Snackbar open={!!error} autoHideDuration={6000} onClose={() => setError('')}>
        <Alert severity="error" onClose={() => setError('')}>{error}</Alert>
      </Snackbar>
      <Snackbar open={!!success} autoHideDuration={3000} onClose={() => setSuccess('')}>
        <Alert severity="success" onClose={() => setSuccess('')}>{success}</Alert>
      </Snackbar>
      
      <Typography variant="h4" mb={3}>Network Scans</Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>Start New Scan</Typography>
          <Box display="flex" gap={2} alignItems="center">
            <TextField
              label="Network Range"
              placeholder="e.g., 192.168.1.0/24"
              value={networkRange}
              onChange={(e) => setNetworkRange(e.target.value)}
              fullWidth
              helperText="Enter your network range (e.g., 192.168.43.0/24 for phone hotspot)"
            />
            <Button 
              variant="contained" 
              startIcon={scanning ? <CircularProgress size={20} color="inherit" /> : <PlayArrow />} 
              onClick={handleStartScan}
              disabled={scanning}
              sx={{ minWidth: 150 }}
            >
              {scanning ? 'Scanning...' : 'Start Scan'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Network Scope</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Devices Found</TableCell>
              <TableCell>Started</TableCell>
              <TableCell>Completed</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {scans.map((scan) => (
              <TableRow key={scan.id}>
                <TableCell>{scan.id}</TableCell>
                <TableCell>{scan.networkScope?.cidr || scan.networkScope?.name || '—'}</TableCell>
                <TableCell>
                  <Chip 
                    label={scan.status} 
                    color={scan.status === 'COMPLETED' ? 'success' : scan.status === 'RUNNING' ? 'primary' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>{scan.devicesFound || 0}</TableCell>
                <TableCell>{scan.startedAt ? formatDate(scan.startedAt) : '—'}</TableCell>
                <TableCell>{scan.completedAt ? formatDate(scan.completedAt) : 'In progress'}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};
