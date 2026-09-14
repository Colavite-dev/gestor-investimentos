import { useEffect, useState } from 'react';
import { apiClient } from './api/client';
import { ErrorAlert, LoadingState } from './components/ui';

type AdminUser = { id: number; nome: string; username: string; email: string; role: string; createdAt: string };
type Metrics = { totalUsuarios: number; totalCarteiras: number; totalAcoes: number; totalOperacoes: number };
export function AdminPage() {
  const [users, setUsers] = useState<AdminUser[]>([]); const [metrics, setMetrics] = useState<Metrics>(); const [error, setError] = useState('');
  useEffect(() => { Promise.all([apiClient.get<AdminUser[]>('/admin/users'), apiClient.get<Metrics>('/admin/metrics')]).then(([userList, values]) => { setUsers(userList); setMetrics(values); }).catch(cause => setError(cause instanceof Error ? cause.message : 'Não foi possível carregar a área administrativa.')); }, []);
  if (error) return <div className="page"><ErrorAlert message={error} /></div>;
  if (!metrics) return <LoadingState />;
  return <div className="page"><div className="page-heading"><div><p className="muted">Administração</p><h2>Visão do sistema</h2></div></div><div className="metric-grid">{Object.entries(metrics).map(([label, value]) => <div className="metric" key={label}><span>{label.replace(/([A-Z])/g, ' $1')}</span><strong>{value}</strong></div>)}</div><section className="panel table-scroll"><div className="panel-heading"><h3>Usuários</h3></div><table><thead><tr><th>Nome</th><th>Usuário</th><th>E-mail</th><th>Perfil</th><th>Criado em</th></tr></thead><tbody>{users.map(user => <tr key={user.id}><td>{user.nome}</td><td>{user.username}</td><td>{user.email}</td><td>{user.role}</td><td>{new Date(user.createdAt).toLocaleString('pt-BR')}</td></tr>)}</tbody></table></section></div>;
}
