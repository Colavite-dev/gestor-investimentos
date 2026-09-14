import { useState } from 'react';
import type { FormEvent } from 'react';
import { AtSign, Mail, UserRound } from 'lucide-react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ApiError } from './api/client';
import { AuthButton, AuthLayout, AuthMessage, InputField, PasswordField } from './components/auth-ui';
import { useAuth } from './auth';
import './auth.css';

export function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);

  if (user) return <Navigate to="/" replace/>;

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setSending(true);
    try {
      await login(username, password);
      navigate(location.state?.from || '/', { replace: true });
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Não foi possível entrar.');
    } finally {
      setSending(false);
    }
  };

  return <AuthLayout>
    <header className="auth-form-header">
      <span className="auth-form-kicker">Acesse sua conta</span>
      <h2>Bem-vindo de volta</h2>
      <p>Acompanhe suas carteiras e decisões de investimento em um só lugar.</p>
    </header>
    <form className="auth-form" onSubmit={submit} aria-busy={sending}>
      <InputField label="Usuário" required autoComplete="username" value={username} onChange={event => setUsername(event.target.value)} icon={<AtSign/>}/>
      <PasswordField label="Senha" required autoComplete="current-password" value={password} onChange={event => setPassword(event.target.value)}/>
      {error && <AuthMessage>{error}</AuthMessage>}
      <AuthButton type="submit" loading={sending}>{sending ? 'Entrando...' : 'Entrar'}</AuthButton>
    </form>
    <p className="auth-switch">Ainda não tem uma conta? <Link to="/cadastro">Criar conta</Link></p>
  </AuthLayout>;
}

export function RegisterPage() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ nome: '', username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);

  if (user) return <Navigate to="/" replace/>;

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setSending(true);
    try {
      await register(form.nome, form.username, form.email, form.password);
      navigate('/login', { replace: true });
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Não foi possível concluir o cadastro.');
    } finally {
      setSending(false);
    }
  };

  return <AuthLayout>
    <header className="auth-form-header">
      <span className="auth-form-kicker">Comece agora</span>
      <h2>Crie sua conta</h2>
      <p>Organize seu patrimônio e acompanhe a evolução dos seus investimentos.</p>
    </header>
    <form className="auth-form" onSubmit={submit} aria-busy={sending}>
      <div className="auth-form-grid">
        <InputField label="Nome" required autoComplete="name" value={form.nome} onChange={event => setForm({ ...form, nome: event.target.value })} icon={<UserRound/>}/>
        <InputField label="Usuário" required autoComplete="username" value={form.username} onChange={event => setForm({ ...form, username: event.target.value })} icon={<AtSign/>}/>
      </div>
      <InputField label="E-mail" required type="email" autoComplete="email" value={form.email} onChange={event => setForm({ ...form, email: event.target.value })} icon={<Mail/>}/>
      <PasswordField label="Senha" required autoComplete="new-password" value={form.password} onChange={event => setForm({ ...form, password: event.target.value })}/>
      {error && <AuthMessage>{error}</AuthMessage>}
      <AuthButton type="submit" loading={sending}>{sending ? 'Criando conta...' : 'Criar conta'}</AuthButton>
    </form>
    <p className="auth-switch">Já possui uma conta? <Link to="/login">Fazer login</Link></p>
  </AuthLayout>;
}
