import { useState, useEffect } from 'react';
import { Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, CircularProgress, TextField, Button, Stack, Chip } from '@mui/material';
import { packetService } from '../services/packetService';
import { formatDate } from '../utils/formatters';

export const NetworkActivity = () => {
  const [packets, setPackets] = useState([]);
  const [httpPackets, setHttpPackets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [interfaceIp, setInterfaceIp] = useState('');
  const [capturing, setCapturing] = useState(false);
  const [actionMessage, setActionMessage] = useState('');
  const [polling, setPolling] = useState(false);
  const [appSummary, setAppSummary] = useState({});

  useEffect(() => {
    const fetchPackets = async () => {
      try {
        const [respAll, respHttp, respApps] = await Promise.all([
          packetService.getPackets(),
          packetService.getHttpPackets(),
          packetService.getApps()
        ]);
        setPackets(respAll.data);
        setHttpPackets(respHttp.data);
        setAppSummary(respApps.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchPackets();
  }, []);

  useEffect(() => {
    if (!polling) return;
    const id = setInterval(async () => {
      try {
        const [respAll, respHttp, respApps] = await Promise.all([
          packetService.getPackets(),
          packetService.getHttpPackets(),
          packetService.getApps()
        ]);
        setPackets(respAll.data);
        setHttpPackets(respHttp.data);
        setAppSummary(respApps.data);
      } catch (e) {
        console.error(e);
      }
    }, 2000);
    return () => clearInterval(id);
  }, [polling]);

  if (loading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Network Activity</Typography>

      <Box mb={2}>
        <Stack direction="row" spacing={2} alignItems="center">
          <TextField
            label="Interface IP"
            placeholder="e.g., 192.168.1.10"
            value={interfaceIp}
            onChange={(e) => setInterfaceIp(e.target.value)}
            size="small"
          />
          <Button
            variant="contained"
            onClick={async () => {
              if (!interfaceIp.trim()) return;
              setCapturing(true);
              setActionMessage('');
              try {
                const res = await packetService.startCapture(interfaceIp);
                setActionMessage(res.data || 'Capture started');
                setPolling(true);
              } catch (e) {
                setActionMessage(e.response?.data || e.message || 'Failed to start capture');
              } finally {
                setCapturing(false);
              }
            }}
            disabled={capturing}
          >
            {capturing ? 'Starting...' : 'Start Capture'}
          </Button>
          <Button
            variant="outlined"
            onClick={async () => {
              setCapturing(true);
              setActionMessage('');
              try {
                const res = await packetService.stopCapture();
                setActionMessage(res.data || 'Capture stopped');
                setPolling(false);
              } catch (e) {
                setActionMessage(e.response?.data || e.message || 'Failed to stop capture');
              } finally {
                setCapturing(false);
              }
            }}
            disabled={capturing}
          >
            Stop Capture
          </Button>
          <Button
            onClick={async () => {
              try {
                await packetService.clearPackets();
                const [respAll, respHttp, respApps] = await Promise.all([
                  packetService.getPackets(),
                  packetService.getHttpPackets(),
                  packetService.getApps()
                ]);
                setPackets(respAll.data);
                setHttpPackets(respHttp.data);
                setAppSummary(respApps.data);
              } catch (e) {
                console.error(e);
              }
            }}
          >
            Clear History
          </Button>
        </Stack>
        {!!actionMessage && (
          <Typography variant="body2" color="text.secondary" mt={1}>{actionMessage}</Typography>
        )}
      </Box>
      
      {Object.keys(appSummary).length > 0 && (
        <Box mb={3}>
          <Typography variant="h5" gutterBottom>App Usage</Typography>
          <Stack direction="row" flexWrap="wrap" gap={1}>
            {Object.entries(appSummary).sort((a, b) => b[1] - a[1]).map(([app, count]) => (
              <Chip key={app} label={`${app}: ${count}`} color="primary" variant="outlined" />
            ))}
          </Stack>
        </Box>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Timestamp</TableCell>
              <TableCell>Source IP</TableCell>
              <TableCell>Destination IP</TableCell>
              <TableCell>Protocol</TableCell>
              <TableCell>Length</TableCell>
              <TableCell>Info</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {packets.map((packet) => (
              <TableRow key={`${packet.timestamp}-${packet.sourceIp}-${packet.destinationIp}-${packet.length}`}>
                <TableCell>{formatDate(packet.timestamp)}</TableCell>
                <TableCell>{packet.sourceIp}</TableCell>
                <TableCell>{packet.destinationIp}</TableCell>
                <TableCell>{packet.protocol}</TableCell>
                <TableCell>{packet.length} bytes</TableCell>
                <TableCell>{packet.info}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Box mt={4}>
        <Typography variant="h5" gutterBottom>HTTP Requests</Typography>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Time</TableCell>
                <TableCell>Method</TableCell>
                <TableCell>Host</TableCell>
                <TableCell>URL</TableCell>
                <TableCell>Source IP</TableCell>
                <TableCell>Destination IP</TableCell>
                <TableCell>User-Agent</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {httpPackets.map((h) => (
                <TableRow key={`${h.timestamp}-${h.sourceIp}-${h.url}`}>
                  <TableCell>{formatDate(h.timestamp)}</TableCell>
                  <TableCell>{h.method}</TableCell>
                  <TableCell>{h.host}</TableCell>
                  <TableCell>{h.url}</TableCell>
                  <TableCell>{h.sourceIp}</TableCell>
                  <TableCell>{h.destinationIp}</TableCell>
                  <TableCell>{h.userAgent}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>
    </Box>
  );
};
