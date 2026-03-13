import api from './api';

export const packetService = {
  getPackets: () => api.get('/packets'),
  getHttpPackets: () => api.get('/packets/http'),
  getDnsPackets: () => api.get('/packets/dns'),
  getApps: () => api.get('/packets/apps'),
  startCapture: (interfaceIp) => api.post(`/packets/start?interfaceIp=${encodeURIComponent(interfaceIp)}`),
  stopCapture: () => api.post('/packets/stop'),
  clearPackets: () => api.delete('/packets')
};
