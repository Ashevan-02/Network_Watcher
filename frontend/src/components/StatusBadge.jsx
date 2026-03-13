import { Chip } from '@mui/material';
import { Circle } from '@mui/icons-material';

export const StatusBadge = ({ status }) => {
  const isOnline = status === 'ONLINE';
  
  return (
    <Chip
      icon={<Circle sx={{ fontSize: 12 }} />}
      label={status}
      color={isOnline ? 'success' : 'error'}
      size="small"
    />
  );
};
