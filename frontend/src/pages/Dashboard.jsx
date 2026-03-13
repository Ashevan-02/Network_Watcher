import { useState, useEffect, useCallback } from 'react';
import { Box, Grid, Card, CardContent, Typography, CircularProgress, Button, Stack } from '@mui/material';
import { Devices, CheckCircle, Warning } from '@mui/icons-material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { deviceService } from '../services/deviceService';
import { vulnerabilityService } from '../services/vulnerabilityService';
import { DeviceCard } from '../components/DeviceCard';
import { useWebSocket } from '../hooks/useWebSocket';
import api from '../services/api';

export const Dashboard = () => {
  const [stats, setStats] = useState({ total: 0, online: 0, offline: 0 });
  const [vulnerabilities, setVulnerabilities] = useState([]);
  const [recentDevices, setRecentDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);
  const [jsonReport, setJsonReport] = useState(null);

  const loadDashboardData = useCallback(async () => {
    try {
      const [devicesRes, vulnRes] = await Promise.all([
        deviceService.getAllDevices(),
        vulnerabilityService.getAllVulnerabilities()
      ]);
      
      const devices = devicesRes.data;
      setStats({
        total: devices.length,
        online: devices.filter(d => d.status === 'ONLINE').length,
        offline: devices.filter(d => d.status === 'OFFLINE').length
      });
      setVulnerabilities(vulnRes.data);
      setRecentDevices(devices.slice(0, 6));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useWebSocket('/topic/devices', () => {
    loadDashboardData();
  });

  useEffect(() => {
    loadDashboardData();
  }, [loadDashboardData]);

  if (loading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;

  const criticalVulns = vulnerabilities.filter(v => v.severity === 'CRITICAL').length;

  const handleDownloadTextReport = async () => {
    setDownloading(true);
    try {
      const res = await api.get('/reports/text', { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'network-report.txt');
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (e) {
      console.error('Report download failed', e);
    } finally {
      setDownloading(false);
    }
  };

  const handleLoadJsonReport = async () => {
    try {
      const res = await api.get('/reports/json');
      setJsonReport(res.data);
    } catch (e) {
      console.error('Load JSON report failed', e);
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Dashboard</Typography>
      
      <Box mb={3}>
        <Stack direction="row" spacing={2}>
          <Button variant="contained" onClick={handleDownloadTextReport} disabled={downloading}>
            {downloading ? 'Downloading...' : 'Download Text Report'}
          </Button>
          <Button variant="outlined" onClick={handleLoadJsonReport}>
            View JSON Report (Console)
          </Button>
        </Stack>
      </Box>

      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <Devices color="primary" fontSize="large" />
                <Box>
                  <Typography variant="h4">{stats.total}</Typography>
                  <Typography color="text.secondary">Total Devices</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <CheckCircle color="success" fontSize="large" />
                <Box>
                  <Typography variant="h4">{stats.online}</Typography>
                  <Typography color="text.secondary">Online Devices</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <Warning color="error" fontSize="large" />
                <Box>
                  <Typography variant="h4">{criticalVulns}</Typography>
                  <Typography color="text.secondary">Critical Vulnerabilities</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Typography variant="h5" gutterBottom>Recent Devices</Typography>
      <Grid container spacing={2}>
        {recentDevices.map(device => (
          <Grid item xs={12} sm={6} md={4} key={device.id}>
            <DeviceCard device={device} />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
