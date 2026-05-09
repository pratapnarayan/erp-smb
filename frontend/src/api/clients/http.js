import axios from 'axios';
import { API_BASE_URL } from '../config/baseUrl.js';
import { attachToken, handleAuthError } from '../interceptors/token.js';
import { logAction } from '../../utils/auditLog.js';

const ENTITY_MAP = {
  '/orders': 'Orders', '/sales': 'Sales', '/finance': 'Finance',
  '/hrms': 'HRMS', '/inventory': 'Inventory', '/products': 'Inventory',
  '/enquiry': 'Enquiry', '/reporting': 'Reporting', '/settings': 'Settings',
  '/auth': 'Auth',
};

function resolveEntity(url = '') {
  for (const [prefix, label] of Object.entries(ENTITY_MAP)) {
    if (url.includes(prefix)) return label;
  }
  return url.split('/').filter(Boolean)[1] || 'System';
}

const http = axios.create({ baseURL: API_BASE_URL });
http.interceptors.request.use(attachToken);
http.interceptors.response.use(
  (response) => {
    const method = (response.config.method || '').toUpperCase();
    const url = response.config.url || '';
    const action = method === 'POST' ? 'CREATE' : method === 'PUT' || method === 'PATCH' ? 'UPDATE' : method === 'DELETE' ? 'DELETE' : null;
    if (action) logAction(action, `${method} ${url}`, resolveEntity(url));
    return response;
  },
  handleAuthError
);

// Expose axios for token refresh fallback
if (typeof window !== 'undefined') window.axios = http;

export default http;
