import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { apiClient, setUnauthorizedListener } from './api/client';

export type AuthUser = { id: number; nome: string; username: string; role: 'USER' | 'ADMIN' };
type LoginResponse = { accessToken: string; tokenType: 'Bearer'; expiresIn: number; user: AuthUser };
type AuthContextValue = { user?: AuthUser; ready: boolean; login: (username: string, password: string) => Promise<void>; register: (nome: string, username: string, email: string, password: string) => Promise<void>; logout: () => void };
const AuthContext = createContext<AuthContextValue | undefined>(undefined);
const TOKEN_KEY = 'adapt-invest.token';

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate();
  const [user, setUser] = useState<AuthUser>();
  const [ready, setReady] = useState(false);
  const logout = useCallback(() => { sessionStorage.removeItem(TOKEN_KEY); setUser(undefined); navigate('/login', { replace: true }); }, [navigate]);
  useEffect(() => {
    setUnauthorizedListener(logout);
    if (!sessionStorage.getItem(TOKEN_KEY)) { setReady(true); return () => setUnauthorizedListener(); }
    apiClient.get<AuthUser>('/auth/me').then(setUser).catch(() => undefined).finally(() => setReady(true));
    return () => setUnauthorizedListener();
  }, [logout]);
  const value = useMemo<AuthContextValue>(() => ({
    user, ready, logout,
    async login(username, password) { const response = await apiClient.post<LoginResponse>('/auth/login', { username, password }); sessionStorage.setItem(TOKEN_KEY, response.accessToken); setUser(response.user); },
    async register(nome, username, email, password) { await apiClient.post('/auth/register', { nome, username, email, password }); },
  }), [user, ready, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export function useAuth() { const context = useContext(AuthContext); if (!context) throw new Error('AuthProvider ausente'); return context; }
export function ProtectedRoute({ children, adminOnly = false }: { children: React.ReactNode; adminOnly?: boolean }) {
  const { user, ready } = useAuth(); const location = useLocation();
  if (!ready) return <div className="page"><p className="muted">Carregando sessão...</p></div>;
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/acesso-negado" replace />;
  return <>{children}</>;
}
