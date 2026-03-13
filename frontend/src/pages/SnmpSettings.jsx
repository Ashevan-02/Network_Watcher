import { useEffect, useState } from 'react';
import { Box, Typography, TextField, Button, Chip, Stack, CircularProgress, Paper } from '@mui/material';
import { snmpSettingsService } from '../services/snmpSettingsService';

export const SnmpSettings = () => {
  const [communities, setCommunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [value, setValue] = useState('');

  const load = async () => {
    try {
      const res = await snmpSettingsService.getAll();
      setCommunities(res.data);
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
      <Typography variant="h5" gutterBottom>SNMP Settings</Typography>
      <Paper sx={{ p: 2, mb: 2 }}>
        <Stack direction="row" spacing={2}>
          <TextField size="small" label="Community" value={value} onChange={e => setValue(e.target.value)} />
          <Button variant="contained" disabled={!value.trim()} onClick={async () => {
            await snmpSettingsService.add(value.trim());
            setValue('');
            load();
          }}>Add</Button>
        </Stack>
      </Paper>
      <Stack direction="row" spacing={1} flexWrap="wrap">
        {communities.map(c => (
          <Chip key={c} label={c} onDelete={async () => {
            await snmpSettingsService.remove(c);
            load();
          }} />
        ))}
      </Stack>
    </Box>
  );
};
