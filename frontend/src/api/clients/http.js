import axios from 'axios';
import { API_BASE_URL } from '../config/baseUrl.js';
import { attachToken, handleAuthError } from '../interceptors/token.js';

const http = axios.create({ baseURL: API_BASE_URL });
http.interceptors.request.use(attachToken);
http.interceptors.response.use(r => r, handleAuthError);

// Expose axios for token refresh fallback
if (typeof window !== 'undefined') window.axios = http;

export default http;
