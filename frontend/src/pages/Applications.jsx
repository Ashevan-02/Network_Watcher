import { useEffect, useState } from 'react';
import { Box, Typography, Grid, Card, CardContent, CircularProgress, Table, TableHead, TableRow, TableCell, TableBody } from '@mui/material';
import { packetService } from '../services/packetService';

export const Applications = () => {
  const [apps, setApps] = useState(null);
  const [dns, setDns] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const [appsRes, dnsRes] = await Promise.all([
        packetService.getApps(),
        packetService.getDnsPackets()
      ]);
      setApps(appsRes.data);
      setDns(dnsRes.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  if (loading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;

  const appRows = Object.entries(apps || {}).sort((a,b) => b[1]-a[1]);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Applications</Typography>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Top Apps</Typography>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>App</TableCell>
                    <TableCell align="right">Count</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {appRows.map(([app, count]) => (
                    <TableRow key={app}>
                      <TableCell>{app}</TableCell>
                      <TableCell align="right">{count}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Recent DNS Queries</Typography>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Domain</TableCell>
                    <TableCell>Source</TableCell>
                    <TableCell>Time</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {dns.slice(-20).reverse().map((q, idx) => (
                    <TableRow key={idx}>
                      <TableCell>{q.domain}</TableCell>
                      <TableCell>{q.sourceIp}</TableCell>
                      <TableCell>{new Date(q.timestamp).toLocaleTimeString()}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
