import { useState, useEffect } from 'react';
import { Box, Typography, CircularProgress, Tabs, Tab } from '@mui/material';
import { vulnerabilityService } from '../services/vulnerabilityService';
import { VulnerabilityAlert } from '../components/VulnerabilityAlert';

export const Vulnerabilities = () => {
  const [vulnerabilities, setVulnerabilities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    const fetchVulnerabilities = async () => {
      try {
        const response = await vulnerabilityService.getAllVulnerabilities();
        setVulnerabilities(response.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchVulnerabilities();
  }, []);

  if (loading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;

  const filtered = filter === 'ALL' 
    ? vulnerabilities 
    : vulnerabilities.filter(v => v.severity === filter);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Vulnerabilities</Typography>
      
      <Tabs value={filter} onChange={(e, v) => setFilter(v)} sx={{ mb: 3 }}>
        <Tab label="All" value="ALL" />
        <Tab label="Critical" value="CRITICAL" />
        <Tab label="High" value="HIGH" />
        <Tab label="Medium" value="MEDIUM" />
        <Tab label="Low" value="LOW" />
      </Tabs>

      {filtered.length > 0 ? (
        filtered.map(vuln => (
          <VulnerabilityAlert key={vuln.id} vulnerability={vuln} />
        ))
      ) : (
        <Typography color="text.secondary">No vulnerabilities found</Typography>
      )}
    </Box>
  );
};
