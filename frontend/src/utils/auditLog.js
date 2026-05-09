const KEY = 'erp_audit_log';
const MAX_ENTRIES = 200;

export function logAction(action, detail = '', entity = '') {
  try {
    const entries = getEntries();
    const user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();
    entries.unshift({
      id: Date.now(),
      ts: new Date().toISOString(),
      user: user.username || 'system',
      role: user.role || '',
      action,
      entity,
      detail,
    });
    if (entries.length > MAX_ENTRIES) entries.length = MAX_ENTRIES;
    sessionStorage.setItem(KEY, JSON.stringify(entries));
  } catch { /* non-critical */ }
}

export function getEntries() {
  try {
    return JSON.parse(sessionStorage.getItem(KEY) || '[]');
  } catch {
    return [];
  }
}

export function clearEntries() {
  sessionStorage.removeItem(KEY);
}
