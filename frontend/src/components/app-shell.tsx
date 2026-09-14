import { useEffect, useRef, useState } from 'react';
import type { ComponentType, ReactNode, SVGProps } from 'react';
import { Activity, Building2, ChevronLeft, LayoutDashboard, LogOut, Menu, ShieldCheck, TrendingUp, WalletCards, X } from 'lucide-react';
import { NavLink, useLocation } from 'react-router-dom';
import { subscribeApiStatus } from '../api/client';
import adaptInvestLogo from '../assets/adapt-invest-logo.png';
import adaptInvestSymbol from '../assets/adapt-invest-symbol.png';
import { useAuth } from '../auth';
import './app-shell.css';

type NavigationItem = {
  to: string;
  label: string;
  icon: ComponentType<SVGProps<SVGSVGElement> & { size?: string | number }>;
  adminOnly?: boolean;
};

const APP_NAVIGATION: readonly NavigationItem[] = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/corretoras', label: 'Corretoras', icon: Building2 },
  { to: '/acoes', label: 'Ações', icon: TrendingUp },
  { to: '/carteiras', label: 'Carteiras', icon: WalletCards },
  { to: '/operacoes', label: 'Operações', icon: Activity },
  { to: '/admin', label: 'Admin', icon: ShieldCheck, adminOnly: true }
] as const;

const PAGE_TITLES: readonly { matches: (path: string) => boolean; title: string }[] = [
  { matches: path => path === '/', title: 'Dashboard' },
  { matches: path => path.includes('/historico'), title: 'Histórico de cotações' },
  { matches: path => path.startsWith('/corretoras'), title: 'Corretoras' },
  { matches: path => path.startsWith('/acoes'), title: 'Ações' },
  { matches: path => path.startsWith('/carteiras/'), title: 'Detalhe da carteira' },
  { matches: path => path.startsWith('/carteiras'), title: 'Carteiras' },
  { matches: path => path.startsWith('/operacoes'), title: 'Operações' },
  { matches: path => path.startsWith('/admin'), title: 'Administração' }
] as const;

function getPageTitle(path: string) {
  return PAGE_TITLES.find(item => item.matches(path))?.title ?? 'Adapt Invest';
}

function visibleNavigation(role?: 'USER' | 'ADMIN') {
  return APP_NAVIGATION.filter(item => !item.adminOnly || role === 'ADMIN');
}

export function AuthenticatedShell({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [online, setOnline] = useState<boolean | null>(null);
  const [compact, setCompact] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const menuButtonRef = useRef<HTMLButtonElement>(null);
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const drawerRef = useRef<HTMLElement>(null);

  useEffect(() => subscribeApiStatus(setOnline), []);

  useEffect(() => {
    if (!drawerOpen) return;
    closeButtonRef.current?.focus();
    const handleDrawerKeyboard = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setDrawerOpen(false);
        menuButtonRef.current?.focus();
        return;
      }

      if (event.key !== 'Tab') return;
      const drawer = drawerRef.current;
      if (!drawer) return;

      const focusableElements = Array.from(drawer.querySelectorAll<HTMLElement>(
        'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
      ));
      if (focusableElements.length === 0) return;

      const firstFocusable = focusableElements[0];
      const lastFocusable = focusableElements[focusableElements.length - 1];
      const activeElement = document.activeElement;
      if (event.shiftKey && (activeElement === firstFocusable || !drawer.contains(activeElement))) {
        event.preventDefault();
        lastFocusable.focus();
      } else if (!event.shiftKey && (activeElement === lastFocusable || !drawer.contains(activeElement))) {
        event.preventDefault();
        firstFocusable.focus();
      }
    };
    document.addEventListener('keydown', handleDrawerKeyboard);
    return () => document.removeEventListener('keydown', handleDrawerKeyboard);
  }, [drawerOpen]);

  const closeDrawer = () => {
    setDrawerOpen(false);
    menuButtonRef.current?.focus();
  };

  const navigation = visibleNavigation(user?.role);
  const title = getPageTitle(location.pathname);

  return <div className={`adapt-shell ${compact ? 'is-compact' : ''}`}>
    <a className="skip-link" href="#main-content">Ir para o conteúdo</a>
    <button
      ref={menuButtonRef}
      type="button"
      className="mobile-menu-button"
      onClick={() => setDrawerOpen(true)}
      aria-label="Abrir menu principal"
      aria-controls="primary-sidebar"
      aria-expanded={drawerOpen}
    ><Menu aria-hidden="true"/></button>

    <div className={`sidebar-backdrop ${drawerOpen ? 'is-visible' : ''}`} onMouseDown={event => event.target === event.currentTarget && closeDrawer()} aria-hidden="true"/>
    <aside ref={drawerRef} id="primary-sidebar" className={`adapt-sidebar ${drawerOpen ? 'is-open' : ''}`} aria-label="Navegação principal" aria-modal={drawerOpen || undefined} role={drawerOpen ? 'dialog' : undefined}>
      <div className="sidebar-brand-row">
        <NavLink className="sidebar-brand" to="/" aria-label="Adapt Invest — Dashboard" onClick={closeDrawer}>
          <img className={compact && !drawerOpen ? 'sidebar-logo-symbol' : 'sidebar-logo-full'} src={compact && !drawerOpen ? adaptInvestSymbol : adaptInvestLogo} alt=""/>
        </NavLink>
        <button ref={closeButtonRef} type="button" className="sidebar-mobile-close" onClick={closeDrawer} aria-label="Fechar menu principal"><X aria-hidden="true"/></button>
      </div>

      <nav className="sidebar-navigation" aria-label="Seções do Adapt Invest">
        <span className="sidebar-section-label">Navegação</span>
        {navigation.map(({ to, label, icon: Icon }) => <NavLink
          key={to}
          to={to}
          end={to === '/'}
          className={({ isActive }) => `sidebar-nav-item ${isActive ? 'is-active' : ''}`}
          onClick={closeDrawer}
          title={compact ? label : undefined}
        >
          <Icon size={19} aria-hidden="true"/>
          <span>{label}</span>
        </NavLink>)}
      </nav>

      <div className="sidebar-session">
        <div className="sidebar-user">
          <span className="sidebar-avatar" aria-hidden="true">{user?.username?.slice(0, 1).toUpperCase() || 'U'}</span>
          <span className="sidebar-user-copy"><strong>{user?.username}</strong><small>{user?.role}</small></span>
        </div>
        <button type="button" className="sidebar-logout" onClick={logout} aria-label="Sair da conta"><LogOut size={18} aria-hidden="true"/><span>Sair</span></button>
        <div className="sidebar-api-state" aria-label={online === null ? 'Status da API não verificado' : online ? 'API conectada' : 'API indisponível'}>
          <span className={`status-dot ${online === false ? 'is-offline' : ''}`} aria-hidden="true"/>
          <span>{online === null ? 'Verificando API' : online ? 'API conectada' : 'API indisponível'}</span>
        </div>
      </div>

      <button type="button" className="sidebar-compact-toggle" onClick={() => setCompact(current => !current)} aria-label={compact ? 'Expandir menu lateral' : 'Recolher menu lateral'} aria-pressed={compact}>
        <ChevronLeft aria-hidden="true"/><span>{compact ? 'Expandir' : 'Recolher menu'}</span>
      </button>
    </aside>

    <div className="adapt-workspace">
      <header className="app-topbar">
        <div className="app-topbar-title"><span>Adapt Invest</span><h1>{title}</h1></div>
        <div className="app-topbar-user" aria-label={`Usuário ${user?.username}, perfil ${user?.role}`}>
          <strong>{user?.username}</strong><span>{user?.role}</span>
        </div>
      </header>
      <main id="main-content" className="app-content" tabIndex={-1}>{children}</main>
    </div>
  </div>;
}
