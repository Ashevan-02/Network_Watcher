import api from './api';

export const alertService = {
  getAll: () => api.get('/alerts'),
  getById: (id) => api.get(`/alerts/${id}`),
  create: (data) => api.post('/alerts', data),
  acknowledge: (id) => api.post(`/alerts/${id}/acknowledge`),
  dismiss: (id) => api.post(`/alerts/${id}/dismiss`),
  escalate: (id) => api.post(`/alerts/${id}/escalate`)
};
