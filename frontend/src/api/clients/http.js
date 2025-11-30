import axios from 'axios';
import { API_BASE_URL } from '../config/baseUrl.js';
import { attachToken } from '../interceptors/token.js';

const http = axios.create({ baseURL: API_BASE_URL });
http.interceptors.request.use(attachToken);
export default http;
