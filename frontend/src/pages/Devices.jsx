import { useState, useEffect } from 'react';
import { Box, Typography, TextField, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, CircularProgress, InputAdornment, Button, Stack } from '@mui/material';
import { Search } from '@mui/icons-material';
import { deviceService } from '../services/deviceService';
import { StatusBadge } from '../components/StatusBadge';
import { formatTimeAgo } from '../utils/formatters';
import { useNavigate } from 'react-router-dom';
import { scanService } from '../services/scanService';

export const Devices = () => {
  const [devices, setDevices] = useState([]);
  const [filteredDevices, setFilteredDevices] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [cidr, setCidr] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDevices = async () => {
      try {
        const response = await deviceService.getAllDevices();
        setDevices(response.data);
        setFilteredDevices(response.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchDevices();
  }, []);

  useEffect(() => {
    const filtered = devices.filter(d =>
      d.ipAddress?.toLowerCase().includes(search.toLowerCase()) ||
      d.hostname?.toLowerCase().includes(search.toLowerCase()) ||
      d.macAddress?.toLowerCase().includes(search.toLowerCase())
    );
    setFilteredDevices(filtered);
  }, [search, devices]);

  if (loading) return <Box display="flex" justifyContent="center" p={4}><CircularProgress /></Box>;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h4">Devices</Typography>
        <Stack direction="row" spacing={2}>
          <TextField
            size="small"
            placeholder="CIDR (e.g., 192.168.75.0/24)"
            value={cidr}
            onChange={(e) => setCidr(e.target.value)}
            sx={{ width: 260 }}
          />
          <Button
            variant="outlined"
            onClick={async () => {
              try {
                await deviceService.deleteByRange(cidr);
                const response = await deviceService.getAllDevices();
                setDevices(response.data);
                setFilteredDevices(response.data);
              } catch (e) { console.error(e); }
            }}
            disabled={!cidr.trim()}
          >
            Delete Range
          </Button>
          <Button
            color="error"
            variant="contained"
            onClick={async () => {
              try {
                await deviceService.deleteAll();
                setDevices([]);
                setFilteredDevices([]);
              } catch (e) { console.error(e); }
            }}
          >
            Delete All
          </Button>
        </Stack>
      </Box>
      
      <TextField
        fullWidth
        placeholder="Search by IP, hostname, or MAC address..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        sx={{ mb: 3 }}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <Search />
            </InputAdornment>
          )
        }}
      />

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>IP Address</TableCell>
              <TableCell>Hostname</TableCell>
              <TableCell>MAC Address</TableCell>
              <TableCell>OS</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Last Seen</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredDevices.map((device) => (
              <TableRow
                key={device.id}
                hover
                sx={{ cursor: 'pointer' }}
                onClick={() => navigate(`/devices/${device.id}`)}
              >
                <TableCell>{device.ipAddress}</TableCell>
                <TableCell>
                  {device.hostname || 'Unknown'}
                  {device.hostnameSource ? ` (${device.hostnameSource})` : ''}
                </TableCell>
                <TableCell>{device.macAddress || 'N/A'}</TableCell>
                <TableCell>{device.operatingSystem || 'Unknown'}</TableCell>
                <TableCell><StatusBadge status={device.status} /></TableCell>
                <TableCell>{formatTimeAgo(device.lastSeen)}</TableCell>
                <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                  <Button 
                    size="small" 
                    variant="outlined" 
                    onClick={async () => {
                      try {
                        setBusyId(device.id);
                        await scanService.scanDevice(device.ipAddress);
                        const response = await deviceService.getAllDevices();
                        setDevices(response.data);
                        setFilteredDevices(response.data);
                      } catch (err) {
                        console.error(err);
                      } finally {
                        setBusyId(null);
                      }
                    }}
                  >
                    {busyId === device.id ? 'Scanning…' : 'Detailed Scan'}
                  </Button>
                  <Button 
                    size="small" 
                    sx={{ ml: 1 }}
                    onClick={async () => {
                      try {
                        setBusyId(device.id + '-snmp');
                        await deviceService.snmpEnrich(device.ipAddress, 'public');
                        const response = await deviceService.getAllDevices();
                        setDevices(response.data);
                        setFilteredDevices(response.data);
                      } catch (err) {
                        console.error(err);
                      } finally {
                        setBusyId(null);
                      }
                    }}
                  >
                    {busyId === device.id + '-snmp' ? 'SNMP…' : 'SNMP Enrich'}
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};
