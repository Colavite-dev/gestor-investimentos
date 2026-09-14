import { BrowserRouter, Link, Route, Routes } from 'react-router-dom';
import { AuthProvider, ProtectedRoute } from './auth';
import { LoginPage, RegisterPage } from './auth-pages';
import { AdminPage } from './admin';
import { BrokerPage } from './broker-page';
import { AuthenticatedShell } from './components/app-shell';
import { EmptyState } from './components/ui';
import { MarketAssetsPage } from './market-assets';
import { StockHistoryPage } from './stock-history-page';
import { OperationsPage } from './operations-page';
import { DashboardPage, PortfolioDetailPage } from './portfolio-pages';
import { PortfolioListPage } from './portfolio-list-page';
import './styles.css';
import './distribution.css';
import './market-enhancements.css';

function AccessDenied() { return <div className="auth-page"><section className="panel auth-card"><h1>Acesso negado</h1><p className="muted">Sua conta não possui permissão para esta área.</p><Link className="button primary" to="/">Voltar ao dashboard</Link></section></div>; }

function ProtectedApp() {
  return <ProtectedRoute><AuthenticatedShell><Routes><Route path="/" element={<DashboardPage/>}/><Route path="/corretoras" element={<BrokerPage/>}/><Route path="/acoes" element={<MarketAssetsPage/>}/><Route path="/acoes/:id/historico" element={<StockHistoryPage/>}/><Route path="/carteiras" element={<PortfolioListPage/>}/><Route path="/carteiras/:id" element={<PortfolioDetailPage/>}/><Route path="/operacoes" element={<OperationsPage/>}/><Route path="/admin" element={<ProtectedRoute adminOnly><AdminPage/></ProtectedRoute>}/><Route path="*" element={<EmptyState title="Página não encontrada" description="Use o menu para continuar." action={<Link to="/" className="button primary">Ir para dashboard</Link>}/>} /></Routes></AuthenticatedShell></ProtectedRoute>;
}

export default function App() {
  return <BrowserRouter><AuthProvider><Routes><Route path="/login" element={<LoginPage/>}/><Route path="/cadastro" element={<RegisterPage/>}/><Route path="/acesso-negado" element={<ProtectedRoute><AccessDenied/></ProtectedRoute>}/><Route path="/*" element={<ProtectedApp/>}/></Routes></AuthProvider></BrowserRouter>;
}
