import { format, formatDistanceToNow } from 'date-fns';

export const formatDate = (date) => {
  if (!date) return 'N/A';
  const d = new Date(date);
  if (isNaN(d.getTime())) return String(date);
  return format(d, 'MMM dd, yyyy HH:mm');
};

export const formatTimeAgo = (date) => {
  if (!date) return 'N/A';
  return formatDistanceToNow(new Date(date), { addSuffix: true });
};

export const formatBytes = (bytes) => {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`;
};

export const formatIP = (ip) => ip || 'Unknown';

export const formatMacAddress = (mac) => {
  if (!mac) return 'Unknown';
  return mac.toUpperCase().match(/.{1,2}/g)?.join(':') || mac;
};
