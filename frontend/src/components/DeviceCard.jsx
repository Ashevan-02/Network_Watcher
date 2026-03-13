import { Card, CardContent, Typography, Box } from '@mui/material';
import { Computer } from '@mui/icons-material';
import { StatusBadge } from './StatusBadge';
import { formatTimeAgo } from '../utils/formatters';
import { useNavigate } from 'react-router-dom';

export const DeviceCard = ({ device }) => {
  const navigate = useNavigate();

  return (
    <Card sx={{ cursor: 'pointer', '&:hover': { boxShadow: 6 } }} onClick={() => navigate(`/devices/${device.id}`)}>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Computer color="primary" />
          <StatusBadge status={device.status} />
        </Box>
        <Typography variant="h6" gutterBottom>{device.hostname || 'Unknown'}</Typography>
        <Typography variant="body2" color="text.secondary">{device.ipAddress}</Typography>
        <Typography variant="caption" color="text.secondary">
          Last seen: {formatTimeAgo(device.lastSeen)}
        </Typography>
      </CardContent>
    </Card>
  );
};
