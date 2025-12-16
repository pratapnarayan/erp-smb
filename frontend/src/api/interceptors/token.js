export function attachToken(config){
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers = config.headers || {};
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
}

let refreshPromise = null;

function clearAuthStorage(){
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
}

function notifyLoggedOut(){
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('auth:logout'));
  }
}

export async function handleAuthError(error){
  const status = error?.response?.status;
  const original = error.config || {};
  if ((status === 401 || status === 403) && !original._retry) {
    original._retry = true;

    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken || refreshToken === 'undefined' || refreshToken === 'null') {
      clearAuthStorage();
      notifyLoggedOut();
      return Promise.reject(error);
    }

    try {
      if (!refreshPromise) {
        refreshPromise = fetch('/api/auth/refresh', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken }),
        })
          .then(async (resp) => {
            if (!resp.ok) throw new Error('Refresh failed');
            const data = await resp.json();
            if (!data?.accessToken) throw new Error('Missing accessToken');
            localStorage.setItem('accessToken', data.accessToken);
            return data.accessToken;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      const newToken = await refreshPromise;
      original.headers = original.headers || {};
      original.headers['Authorization'] = `Bearer ${newToken}`;

      if (!window?.axios) return Promise.reject(error);
      return window.axios(original);
    } catch (e) {
      clearAuthStorage();
      notifyLoggedOut();
      return Promise.reject(error);
    }
  }

  return Promise.reject(error);
}
