import http from './http.js';

export const authApi = {
  login: (username, password) => http.post('/auth/login', { username, password }).then(r => r.data),
  signup: (username, password, role) => http.post('/auth/signup', { username, password, role }).then(r => r.data),
  refresh: (refreshToken) => http.post('/auth/refresh', { refreshToken }).then(r => r.data),
};

export const usersApi = { list: (page=0,size=20) => http.get(`/users?page=${page}&size=${size}`).then(r=>r.data) };
export const productsApi = { list: (page=0,size=20) => http.get(`/products?page=${page}&size=${size}`).then(r=>r.data), create: (item)=> http.post('/products', item).then(r=>r.data) };
export const ordersApi = { list: (page=0,size=20) => http.get(`/orders?page=${page}&size=${size}`).then(r=>r.data), create: (order)=> http.post('/orders', order).then(r=>r.data) };
export const salesApi = { list: (page=0,size=20) => http.get(`/sales?page=${page}&size=${size}`).then(r=>r.data) };
export const financeApi = { list: (page=0,size=20) => http.get(`/finance?page=${page}&size=${size}`).then(r=>r.data), create: (t)=> http.post('/finance', t).then(r=>r.data), kpis: ()=> http.get('/finance/kpis').then(r=>r.data) };
export const hrmsApi = { list: (page=0,size=20) => http.get(`/hrms?page=${page}&size=${size}`).then(r=>r.data), create: (e)=> http.post('/hrms', e).then(r=>r.data) };
export const enquiryApi = { list: (page=0,size=20) => http.get(`/enquiry?page=${page}&size=${size}`).then(r=>r.data), create: (e)=> http.post('/enquiry', e).then(r=>r.data) };
export { reportingApi } from './reports.js';
