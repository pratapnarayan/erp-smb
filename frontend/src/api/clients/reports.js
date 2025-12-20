import http from './http.js';

const tenantHeader = () => ({ 'X-Tenant-Id': localStorage.getItem('tenantId') || 'demo' });

export const reportingApi = {
  definitions: () => http.get('/reports/v1/reports/definitions', { headers: tenantHeader() }).then(r => r.data),
  metrics: (period='month') => http.get(`/reports/v1/reports/metrics?period=${encodeURIComponent(period)}`, { headers: tenantHeader() }).then(r=>r.data),
  run: (definitionCode, paramsJson='{}', format='CSV') => http.post('/reports/v1/reports/run', { definitionCode, paramsJson, format }, { headers: tenantHeader() }).then(r=>r.data),
  runs: (page=0,size=20) => http.get(`/reports/v1/reports/runs?page=${page}&size=${size}`, { headers: tenantHeader() }).then(r=>r.data),
  exportsByRun: (runId) => http.get(`/reports/v1/reports/runs/${runId}/exports`, { headers: tenantHeader() }).then(r => r.data),
  download: (exportId) => http.get(`/reports/v1/reports/exports/${exportId}/download`, { responseType: 'blob', headers: tenantHeader() }).then(r=>r.data),
};
