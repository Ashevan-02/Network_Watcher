import api from './api';

export const deviceService = {
  getAllDevices: () => api.get('/devices'),
  getDeviceById: (id) => api.get(`/devices/${id}`),
  getOnlineDevices: () => api.get('/devices/status/online'),
  getOfflineDevices: () => api.get('/devices/status/offline'),
  updateDevice: (id, data) => api.put(`/devices/${id}`, data),
  deleteDevice: (id) => api.delete(`/devices/${id}`),
  getDeviceStats: () => api.get('/devices/stats'),
  getByIp: (ip) => api.get(`/devices/ip/${encodeURIComponent(ip)}`),
  disconnect: (ip) => api.post(`/devices/disconnect/${encodeURIComponent(ip)}`),
  reconnect: (ip) => api.post(`/devices/reconnect/${encodeURIComponent(ip)}`),
  getBlocked: () => api.get('/devices/blocked'),
  snmpEnrich: (ip, community = 'public') => api.post(`/devices/snmp/${encodeURIComponent(ip)}?community=${encodeURIComponent(community)}`),
  deleteAll: () => api.delete('/devices'),
  deleteByRange: (cidr) => api.delete(`/devices/range?cidr=${encodeURIComponent(cidr)}`)
};
