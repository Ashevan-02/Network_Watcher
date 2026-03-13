import api from './api';

export const bandwidthService = {
  getAllBandwidth: () => api.get('/bandwidth'),
  getDeviceBandwidth: (deviceId) => api.get(`/bandwidth/device/${deviceId}`),
  getBandwidthSummary: (deviceId) => api.get(`/bandwidth/device/${deviceId}/summary`)
};
