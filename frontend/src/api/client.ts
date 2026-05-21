const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080/api';

export function setToken(token: string | null) {
  if (token) {
    localStorage.setItem('docforge-token', token);
  } else {
    localStorage.removeItem('docforge-token');
  }
}

export function getToken() {
  return localStorage.getItem('docforge-token');
}

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  if (!(init?.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers,
  });
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({ message: '请求失败' }));
    throw new Error(errorBody.message ?? '请求失败');
  }
  return response.json() as Promise<T>;
}

export async function apiDownload(path: string): Promise<Blob> {
  const headers = new Headers();
  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  const response = await fetch(`${API_BASE}${path}`, { headers });
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({ message: '下载失败' }));
    throw new Error(errorBody.message ?? '下载失败');
  }
  return response.blob();
}
