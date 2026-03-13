import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Card, CardContent, Typography } from '@mui/material';
import { formatBytes } from '../utils/formatters';

export const BandwidthChart = ({ data, title = 'Bandwidth Usage' }) => {
  const formatYAxis = (value) => formatBytes(value);

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>{title}</Typography>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="time" />
            <YAxis tickFormatter={formatYAxis} />
            <Tooltip formatter={(value) => formatBytes(value)} />
            <Legend />
            <Line type="monotone" dataKey="bytesSent" stroke="#8884d8" name="Upload" />
            <Line type="monotone" dataKey="bytesReceived" stroke="#82ca9d" name="Download" />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
};
