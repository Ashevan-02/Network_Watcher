import { Box, Typography, Card, CardContent, Switch, FormControlLabel, Button, Stack } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useContext } from 'react';
import { ThemeContext } from '../context/ThemeContext';

export const Settings = () => {
  const { mode, toggleTheme } = useContext(ThemeContext);
  const navigate = useNavigate();

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Settings</Typography>
      
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>Appearance</Typography>
          <FormControlLabel
            control={<Switch checked={mode === 'dark'} onChange={toggleTheme} />}
            label="Dark Mode"
          />
          <Box mt={3}>
            <Typography variant="h6" gutterBottom>Integrations</Typography>
            <Stack direction="row" spacing={2}>
              <Button variant="outlined" onClick={() => navigate('/settings/snmp')}>SNMP Settings</Button>
            </Stack>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};
