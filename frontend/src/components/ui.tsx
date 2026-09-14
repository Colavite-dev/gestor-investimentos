import { useEffect, useId, useRef } from 'react';
import type { ReactNode } from 'react';
import { AlertCircle, CheckCircle2, LoaderCircle, X } from 'lucide-react';

export const LoadingState = () => <div className="loading"><LoaderCircle className="spin" size={22}/> Carregando dados…</div>;
export const EmptyState = ({ title, description, action }: { title: string; description: string; action?: ReactNode }) => <div className="empty"><div className="empty-icon">—</div><h3>{title}</h3><p>{description}</p>{action}</div>;
export const ErrorAlert = ({ message }: { message: string }) => <div className="error-alert" role="alert"><AlertCircle size={17} aria-hidden="true"/><span>{message}</span></div>;
export const StatusBadge = ({ label, tone = 'default' }: { label: string; tone?: string }) => <span className={`badge ${tone}`}>{label}</span>;

export function Modal({ open, onClose, title, children }: { open: boolean; onClose: () => void; title: string; children: ReactNode }) {
  const titleId = useId();
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const previousFocusRef = useRef<HTMLElement | null>(null);
  const onCloseRef = useRef(onClose);
  useEffect(() => { onCloseRef.current = onClose; }, [onClose]);

  useEffect(() => {
    if (!open) return;
    previousFocusRef.current = document.activeElement instanceof HTMLElement ? document.activeElement : null;
    closeButtonRef.current?.focus();
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onCloseRef.current();
      if (event.key !== 'Tab') return;
      const modal = closeButtonRef.current?.closest<HTMLElement>('[role="dialog"]');
      const focusable = modal ? [...modal.querySelectorAll<HTMLElement>('a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])')] : [];
      if (!focusable.length) return;
      const first = focusable[0];
      const last = focusable.at(-1)!;
      if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
      if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      previousFocusRef.current?.focus();
    };
  }, [open]);

  if (!open) return null;
  return <div className="modal-backdrop" onMouseDown={event => event.target === event.currentTarget && onClose()}><section className="modal" role="dialog" aria-modal="true" aria-labelledby={titleId}><div className="modal-heading"><h3 id={titleId}>{title}</h3><button ref={closeButtonRef} type="button" className="icon-button" onClick={onClose} aria-label="Fechar"><X size={18}/></button></div>{children}</section></div>;
}

export function Toast({ message, onClose }: { message: string; onClose: () => void }) {
  if (!message) return null;
  return <div className="toast" role="status" aria-live="polite"><CheckCircle2 size={17} aria-hidden="true"/>{message}<button type="button" onClick={onClose} aria-label="Fechar"><X size={15}/></button></div>;
}
