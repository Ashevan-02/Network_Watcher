import api from './api';

export const scanService = {
  getAllScans: () => api.get('/scans'),
  getScanById: (id) => api.get(`/scans/${id}`),
  startScan: (scanData) => api.post('/scans/start', scanData),
  scanNetwork: (networkRange) => api.post(`/scan/network?range=${encodeURIComponent(networkRange)}`),
  scanDevice: (ip) => api.post(`/scan/device/${encodeURIComponent(ip)}`)
};
