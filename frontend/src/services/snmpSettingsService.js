import api from './api';

export const snmpSettingsService = {
  getAll: () => api.get('/snmp/communities'),
  add: (community) => api.post('/snmp/communities', { community }),
  remove: (community) => api.delete(`/snmp/communities/${encodeURIComponent(community)}`)
};
