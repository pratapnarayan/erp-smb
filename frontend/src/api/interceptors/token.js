export function attachToken(config){
  const token = localStorage.getItem('accessToken');
  if (token) config.headers['Authorization'] = `Bearer ${token}`;
  return config;
}

let isRefreshing = false;
let pending = [];

function subscribeTokenRefresh(cb){ pending.push(cb); }
function onRefreshed(newToken){ pending.forEach(cb => cb(newToken)); pending = []; }

export async function handleAuthError(error){
  const status = error?.response?.status;
  const original = error.config || {};
  if ((status === 401 || status === 403) && !original._retry) {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      return Promise.reject(error);
    }
    if (!isRefreshing) {
      isRefreshing = true;
      try {
        const resp = await fetch('/api/auth/refresh', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken })
        });
        if (resp.ok) {
          const data = await resp.json();
          localStorage.setItem('accessToken', data.accessToken);
          onRefreshed(data.accessToken);
        } else {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          throw new Error('Refresh failed');
        }
      } catch (e) {
        isRefreshing = false;
        return Promise.reject(error);
      }
      isRefreshing = false;
    }
    return new Promise((resolve, reject) => {
      subscribeTokenRefresh((newToken) => {
        try {
          original._retry = true;
          original.headers = original.headers || {};
          original.headers['Authorization'] = `Bearer ${newToken}`;
          resolve(window.axios ? window.axios(original) : fetch(original.url || original.baseURL+original.url, original));
        } catch (e){ reject(e); }
      });
    });
  }
  return Promise.reject(error);
}
