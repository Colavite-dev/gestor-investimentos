import { useId, useState } from 'react';
import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode } from 'react';
import { AlertCircle, Eye, EyeOff, LoaderCircle } from 'lucide-react';
import adaptInvestLogo from '../assets/adapt-invest-logo.png';
import adaptInvestSymbol from '../assets/adapt-invest-symbol.png';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & { loading?: boolean };

export function AuthButton({ children, className = '', loading = false, disabled, ...props }: ButtonProps) {
  return <button className={`auth-cta ${className}`.trim()} disabled={disabled || loading} aria-busy={loading} {...props}>
    <span className="auth-cta-content">{loading && <LoaderCircle className="auth-spinner" aria-hidden="true"/>}{children}</span>
  </button>;
}

type InputFieldProps = InputHTMLAttributes<HTMLInputElement> & { label: string; icon?: ReactNode };

export function InputField({ label, icon, className = '', id: suppliedId, ...props }: InputFieldProps) {
  const generatedId = useId();
  const id = suppliedId ?? generatedId;
  return <label className="auth-field" htmlFor={id}>
    <span className="auth-field-label">{label}</span>
    <span className="auth-input-wrap">
      {icon && <span className="auth-input-icon" aria-hidden="true">{icon}</span>}
      <input id={id} className={`${icon ? 'has-leading-icon' : ''} ${className}`.trim()} {...props}/>
    </span>
  </label>;
}

type PasswordFieldProps = Omit<InputFieldProps, 'type'>;

export function PasswordField(props: PasswordFieldProps) {
  const [visible, setVisible] = useState(false);
  return <div className="auth-password-field">
    <InputField {...props} type={visible ? 'text' : 'password'}/>
    <button type="button" className="auth-password-toggle" onClick={() => setVisible(current => !current)} aria-label={visible ? 'Ocultar senha' : 'Mostrar senha'} aria-pressed={visible}>
      {visible ? <EyeOff aria-hidden="true"/> : <Eye aria-hidden="true"/>}
    </button>
  </div>;
}

export function AuthMessage({ children }: { children: ReactNode }) {
  return <div className="auth-message" role="alert"><AlertCircle aria-hidden="true"/><span>{children}</span></div>;
}

export function AuthLayout({ children }: { children: ReactNode }) {
  return <main className="auth-shell">
    <section className="auth-brand-panel" aria-label="Adapt Invest">
      <div className="auth-brand-glow" aria-hidden="true"/>
      <div className="auth-brand-content">
        <picture className="auth-logo-picture">
          <source media="(max-width: 767px)" srcSet={adaptInvestSymbol}/>
          <img className="auth-logo" src={adaptInvestLogo} alt="Adapt Invest"/>
        </picture>
        <div className="auth-brand-copy">
          <p className="auth-kicker">Seu futuro em movimento</p>
          <h1>Invista com visão. Gerencie com controle.</h1>
          <p>Centralize seus investimentos, acompanhe sua carteira e tome decisões com base em dados reais.</p>
        </div>
      </div>
      <div className="auth-market-art" aria-hidden="true">
        <svg viewBox="0 0 620 250" role="presentation" focusable="false">
          <defs><linearGradient id="auth-line-gradient" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stopColor="#2dd4dc" stopOpacity="0.32"/><stop offset="1" stopColor="#2b65ff"/></linearGradient></defs>
          <g className="auth-chart-grid"><path d="M20 55H600M20 115H600M20 175H600M100 25V220M220 25V220M340 25V220M460 25V220M580 25V220"/></g>
          <g className="auth-chart-bars"><rect x="74" y="165" width="34" height="55" rx="5"/><rect x="153" y="138" width="34" height="82" rx="5"/><rect x="232" y="151" width="34" height="69" rx="5"/><rect x="311" y="104" width="34" height="116" rx="5"/><rect x="390" y="119" width="34" height="101" rx="5"/><rect x="469" y="72" width="34" height="148" rx="5"/></g>
          <path className="auth-chart-line" d="M42 181C100 177 120 143 172 151S242 132 282 141 352 88 405 103 477 66 566 43"/>
          <circle className="auth-chart-point" cx="566" cy="43" r="6"/>
        </svg>
      </div>
    </section>
    <section className="auth-form-panel"><div className="auth-form-container">{children}</div></section>
  </main>;
}
