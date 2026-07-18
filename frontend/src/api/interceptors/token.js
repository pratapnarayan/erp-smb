import { API_BASE_URL } from '../config/baseUrl.js';

/**
 * Axios request/response interceptors for authentication.
 *
 * Auth model (HttpOnly cookies):
 *   - Access and refresh tokens are stored in HttpOnly cookies set by the server.
 *   - JavaScript cannot read these cookies, preventing XSS-based token theft.
 *   - Axios sends them automatically when `withCredentials: true` is set on the
 *     instance (configured in http.js).
 *   - No Authorization header is attached manually — the cookie is the credential.
 *
 * X-Tenant-Id header is still attached here (it is not sensitive).
 */

/**
 * Request interceptor: attach X-Tenant-Id header only.
 * Tokens travel via HttpOnly cookies and are sent by the browser automatically.
 */
export function attachToken(config) {
  const tenantId = localStorage.getItem('tenantId') || 'demo';
  config.headers = config.headers || {};
  if (!config.headers['X-Tenant-Id'] && tenantId) {
    config.headers['X-Tenant-Id'] = tenantId;
  }
  return config;
}

/** Single in-flight refresh promise, shared across concurrent 401 failures. */
let refreshPromise = null;

function clearUserStorage() {
  // Only clear non-sensitive user metadata (tokens live in HttpOnly cookies).
  localStorage.removeItem('user');
}

function notifyLoggedOut() {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('auth:logout'));
  }
}

/**
 * Response error interceptor.
 *
 * On 401/403:
 *   1. POST /api/auth/refresh — the refreshToken cookie is sent automatically.
 *   2. Server sets a new accessToken cookie and returns 200.
 *   3. Retry the original request (new cookie is sent automatically).
 *   4. If refresh fails, clear user data and fire auth:logout.
 */
export async function handleAuthError(error) {
  const status   = error?.response?.status;
  const original = error.config || {};

  if ((status === 401 || status === 403) && !original._retry) {
    original._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = fetch(`${API_BASE_URL}/auth/refresh`, {
          method:      'POST',
          credentials: 'include', // send refreshToken cookie
          headers:     { 'Content-Type': 'application/json' },
        })
          .then(async (resp) => {
            if (!resp.ok) throw new Error('Token refresh failed');
            // New accessToken cookie has been set by the server.
            // Nothing to store — the browser handles the cookie.
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      await refreshPromise;

      // Retry the original request — the new accessToken cookie will be sent.
      if (!window?.axios) return Promise.reject(error);
      return window.axios(original);
    } catch {
      clearUserStorage();
      notifyLoggedOut();
      return Promise.reject(error);
    }
  }

  return Promise.reject(error);
}
