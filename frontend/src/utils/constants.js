// In development this falls back to same-origin paths.
// For deployed environments you can optionally set:
// - VITE_API_BASE_URL (e.g. https://network-watcher.onrender.com/api)
// - VITE_WS_BASE_URL (e.g. https://network-watcher.onrender.com/ws)
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

// Derive WS base from API base when possible:
// - if API_BASE_URL is https://host/api -> ws becomes https://host/ws
// - if API_BASE_URL is /api -> ws becomes /ws (same-origin)
const derivedWsBase = API_BASE_URL.replace(/\/api\/?$/, '/ws').replace(/\/api$/, '/ws');
export const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || derivedWsBase;

export const DEVICE_STATUS = {
  ONLINE: 'ONLINE',
  OFFLINE: 'OFFLINE'
};

export const VULNERABILITY_SEVERITY = {
  CRITICAL: 'CRITICAL',
  HIGH: 'HIGH',
  MEDIUM: 'MEDIUM',
  LOW: 'LOW',
  INFO: 'INFO'
};

export const SCAN_STATUS = {
  PENDING: 'PENDING',
  RUNNING: 'RUNNING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED'
};
