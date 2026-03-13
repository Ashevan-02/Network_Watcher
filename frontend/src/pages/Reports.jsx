import { useEffect, useState } from 'react';
import { Box, Grid, Card, CardContent, Typography, CircularProgress, Button, Stack } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import api from '../services/api';

export const Reports = () => {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);

  const loadReport = async () => {
    try {
      const res = await api.get('/reports/json');
      setReport(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReport();
  }, []);

  if (loading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;
  if (!report) return <Typography p={2}>Failed to load report</Typography>;

  const osData = Object.entries(report.devicesByOS || {}).map(([name, value]) => ({ name, value }));
  const vendorData = Object.entries(report.devicesByVendor || {}).map(([name, value]) => ({ name, value }));

  const handleDownloadText = async () => {
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
      console.error(e);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Reports</Typography>
        <Stack direction="row" spacing={2}>
          <Button variant="outlined" onClick={loadReport}>Refresh</Button>
          <Button variant="contained" onClick={handleDownloadText} disabled={downloading}>
            {downloading ? 'Downloading...' : 'Download Text Report'}
          </Button>
        </Stack>
      </Box>

      <Grid container spacing={3} mb={2}>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h5">{report.totalDevices}</Typography>
              <Typography color="text.secondary">Total Devices</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h5">{report.onlineDevices}</Typography>
              <Typography color="text.secondary">Online</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h5">{report.offlineDevices}</Typography>
              <Typography color="text.secondary">Offline</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h5">{report.vulnerableDevices}</Typography>
              <Typography color="text.secondary">Vulnerable</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card sx={{ height: 380 }}>
            <CardContent sx={{ height: '100%' }}>
              <Typography variant="h6" gutterBottom>Devices by OS</Typography>
              <ResponsiveContainer width="100%" height="90%">
                <BarChart data={osData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="value" fill="#1976d2" />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card sx={{ height: 380 }}>
            <CardContent sx={{ height: '100%' }}>
              <Typography variant="h6" gutterBottom>Devices by Vendor</Typography>
              <ResponsiveContainer width="100%" height="90%">
                <BarChart data={vendorData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" hide={vendorData.length > 8} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="value" fill="#2e7d32" />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
