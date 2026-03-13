import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Box, Typography, Card, CardContent, Grid, CircularProgress, Chip, Button, Stack } from '@mui/material';
import { deviceService } from '../services/deviceService';
import { vulnerabilityService } from '../services/vulnerabilityService';
import { bandwidthService } from '../services/bandwidthService';
import { StatusBadge } from '../components/StatusBadge';
import { VulnerabilityAlert } from '../components/VulnerabilityAlert';
import { BandwidthChart } from '../components/BandwidthChart';
import { formatDate } from '../utils/formatters';
import { format } from 'date-fns';

export const DeviceDetails = () => {
  const { id } = useParams();
  const [device, setDevice] = useState(null);
  const [vulnerabilities, setVulnerabilities] = useState([]);
  const [bandwidthData, setBandwidthData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [actionMessage, setActionMessage] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [deviceRes, vulnRes, bandwidthRes] = await Promise.all([
          deviceService.getDeviceById(id),
          vulnerabilityService.getVulnerabilitiesByDevice(id),
          bandwidthService.getDeviceBandwidth(id)
        ]);
        setDevice(deviceRes.data);
        setVulnerabilities(vulnRes.data);
        setBandwidthData(bandwidthRes.data.map(b => ({
          time: format(new Date(b.recordedAt), 'HH:mm'),
          bytesSent: b.bytesSent,
          bytesReceived: b.bytesReceived
        })));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  if (loading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;
  if (!device) return <Typography>Device not found</Typography>;

  const handleDisconnect = async () => {
    if (!device?.ipAddress) return;
    setActionLoading(true);
    try {
      const res = await deviceService.disconnect(device.ipAddress);
      setActionMessage(res.data || 'Device disconnected');
    } catch (e) {
      setActionMessage(e.response?.data || e.message || 'Failed to disconnect');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReconnect = async () => {
    if (!device?.ipAddress) return;
    setActionLoading(true);
    try {
      const res = await deviceService.reconnect(device.ipAddress);
      setActionMessage(res.data || 'Device reconnected');
    } catch (e) {
      setActionMessage(e.response?.data || e.message || 'Failed to reconnect');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">{device.hostname || 'Unknown Device'}</Typography>
        <StatusBadge status={device.status} />
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Device Information</Typography>
              <Box display="flex" flexDirection="column" gap={1}>
                <Typography><strong>IP Address:</strong> {device.ipAddress}</Typography>
                <Typography><strong>MAC Address:</strong> {device.macAddress || 'N/A'}</Typography>
                <Typography><strong>OS:</strong> {device.operatingSystem || 'Unknown'}</Typography>
                <Typography><strong>Manufacturer:</strong> {device.macVendor || 'Unknown'}</Typography>
                <Typography><strong>First Seen:</strong> {formatDate(device.firstSeen)}</Typography>
                <Typography><strong>Last Seen:</strong> {formatDate(device.lastSeen)}</Typography>
              </Box>
              <Stack direction="row" spacing={2} mt={2}>
                <Button variant="contained" color="error" onClick={handleDisconnect} disabled={actionLoading}>
                  {actionLoading ? 'Processing...' : 'Disconnect Device'}
                </Button>
                <Button variant="outlined" onClick={handleReconnect} disabled={actionLoading}>
                  {actionLoading ? 'Processing...' : 'Reconnect Device'}
                </Button>
              </Stack>
              {actionMessage && (
                <Typography variant="body2" color="text.secondary" mt={1}>{actionMessage}</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Open Ports</Typography>
              <Box display="flex" flexWrap="wrap" gap={1}>
                <Typography color="text.secondary">No open ports list available</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <BandwidthChart data={bandwidthData} title="Device Bandwidth Usage" />
        </Grid>

        <Grid item xs={12}>
          <Typography variant="h5" gutterBottom>Vulnerabilities</Typography>
          {vulnerabilities.length > 0 ? (
            vulnerabilities.map(vuln => (
              <VulnerabilityAlert key={vuln.id} vulnerability={vuln} />
            ))
          ) : (
            <Typography color="text.secondary">No vulnerabilities detected</Typography>
          )}
        </Grid>
      </Grid>
    </Box>
  );
};
