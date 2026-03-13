import { useEffect, useState } from 'react';
import { Box, Typography, CircularProgress, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button, Chip } from '@mui/material';
import { alertService } from '../services/alertService';
import { formatDate } from '../utils/formatters';

export const Alerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const res = await alertService.getAll();
      setAlerts(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  if (loading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Alerts</Typography>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Title</TableCell>
              <TableCell>Message</TableCell>
              <TableCell>Severity</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Device</TableCell>
              <TableCell>Created</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {alerts.map(a => (
              <TableRow key={a.id}>
                <TableCell>{a.title}</TableCell>
                <TableCell>{a.message}</TableCell>
                <TableCell><Chip size="small" color={a.severity === 'HIGH' ? 'error' : a.severity === 'MEDIUM' ? 'warning' : 'default'} label={a.severity} /></TableCell>
                <TableCell><Chip size="small" label={a.status} /></TableCell>
                <TableCell>{a.device?.ipAddress || '—'}</TableCell>
                <TableCell>{formatDate(a.createdAt)}</TableCell>
                <TableCell align="right">
                  <Button size="small" onClick={async () => { await alertService.acknowledge(a.id); load(); }}>Acknowledge</Button>
                  <Button size="small" onClick={async () => { await alertService.dismiss(a.id); load(); }}>Dismiss</Button>
                  <Button size="small" onClick={async () => { await alertService.escalate(a.id); load(); }}>Escalate</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};
