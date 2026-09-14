import type { ApiErrorPayload } from '../types/api';

const base = (import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
let statusListeners: ((online: boolean) => void)[] = [];
let unauthorizedListener: (() => void) | undefined;

export class ApiError extends Error {
  status: number;
  payload?: ApiErrorPayload;
  constructor(status: number, message: string, payload?: ApiErrorPayload) { super(message); this.name = 'ApiError'; this.status = status; this.payload = payload; }
}
export function subscribeApiStatus(listener: (online: boolean) => void) { statusListeners.push(listener); return () => { statusListeners = statusListeners.filter(item => item !== listener); }; }
export function setUnauthorizedListener(listener?: () => void) { unauthorizedListener = listener; }
function statusMessage(status: number) {
  if (status === 400) return 'Revise os dados informados.';
  if (status === 401) return 'Sua sessão expirou ou é inválida.';
  if (status === 403) return 'Você não tem permissão para esta ação.';
  if (status === 404) return 'Recurso não encontrado.';
  if (status === 409) return 'Registro duplicado ou conflito.';
  if (status === 422) return 'A operação não atende a uma regra de negócio.';
  return 'Não foi possível concluir a solicitação.';
}
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = sessionStorage.getItem('adapt-invest.token'); let response: Response;
  try { response = await fetch(`${base}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(options.headers || {}) } }); }
  catch { statusListeners.forEach(listener => listener(false)); throw new ApiError(503, 'Não foi possível conectar ao backend.'); }
  statusListeners.forEach(listener => listener(true));
  const text = await response.text(); let payload: ApiErrorPayload = {};
  try { payload = text ? JSON.parse(text) : {}; } catch { /* resposta não JSON */ }
  if (!response.ok) { if (response.status === 401 && token) unauthorizedListener?.(); throw new ApiError(response.status, payload.message || statusMessage(response.status), payload); }
  return text ? JSON.parse(text) : undefined as T;
}
export const apiClient = { get: <T>(path: string) => request<T>(path), post: <T>(path: string, body: unknown) => request<T>(path, { method: 'POST', body: JSON.stringify(body) }), put: <T>(path: string) => request<T>(path, { method: 'PUT' }) };
