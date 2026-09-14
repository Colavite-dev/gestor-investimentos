import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { Plus, WalletCards } from 'lucide-react';
import { carteirasApi } from './api/modules';
import { EmptyState, ErrorAlert, LoadingState, Modal } from './components/ui';
import type { Carteira } from './types/api';
import { formatDate } from './utils/format';
import './portfolio-pages.css';

function WalletPageHeader({ onCreate, hasWallets }: { onCreate: () => void; hasWallets: boolean }) {
  return <header className="page-heading portfolio-page-heading">
    <div><p className="page-eyebrow">Gestão de carteiras</p><h2>Carteiras</h2><p className="muted">Acompanhe seus investimentos e desempenho por carteira.</p></div>
    {hasWallets && <button className="button primary" type="button" onClick={onCreate}><Plus size={16} /> Nova carteira</button>}
  </header>;
}

function WalletCard({ wallet }: { wallet: Carteira }) {
  return <Link className="wallet-card portfolio-wallet-card" to={`/carteiras/${wallet.id}`}>
    <div className="wallet-card-icon"><WalletCards size={19} aria-hidden="true" /></div>
    <div className="wallet-card-heading"><strong>{wallet.nome}</strong><span aria-hidden="true">→</span></div>
    <span>{wallet.descricao || 'Sem descrição'}</span>
    <small>Criada em {formatDate(wallet.dataCadastro)}</small>
  </Link>;
}

export function PortfolioListPage() {
  const [items, setItems] = useState<Carteira[]>([]);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ nome: '', descricao: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = () => {
    setLoading(true);
    return carteirasApi.list().then(setItems).catch(reason => setError(reason.message || 'Não foi possível carregar as carteiras.')).finally(() => setLoading(false));
  };

  useEffect(() => { void load(); }, []);

  const save = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await carteirasApi.create(form);
      setOpen(false);
      setForm({ nome: '', descricao: '' });
      setError('');
      void load();
    } catch (reason) {
      setError((reason as Error).message || 'Não foi possível criar a carteira.');
    }
  };

  return <div className="page portfolio-list-page">
    <WalletPageHeader onCreate={() => setOpen(true)} hasWallets={items.length > 0} />
    {error && <ErrorAlert message={error} />}
    {loading ? <LoadingState /> : items.length ? <>
      <section className="portfolio-overview" aria-label="Resumo de carteiras">
        <article className="portfolio-overview-card"><span>Carteiras cadastradas</span><strong>{items.length}</strong><small>Organize seus investimentos por estratégia</small></article>
      </section>
      <div className="portfolio-wallet-grid">{items.map(wallet => <WalletCard key={wallet.id} wallet={wallet} />)}</div>
    </> : <EmptyState title="Nenhuma carteira cadastrada" description="Crie sua primeira carteira para acompanhar posições e operações." action={<button className="button primary" type="button" onClick={() => setOpen(true)}><Plus size={16} /> Criar carteira</button>} />}
    <Modal open={open} onClose={() => setOpen(false)} title="Nova carteira">
      <form onSubmit={save}>
        <label>Nome<input required value={form.nome} onChange={event => setForm({ ...form, nome: event.target.value })} /></label>
        <label>Descrição<textarea className="wallet-description-field" value={form.descricao} onChange={event => setForm({ ...form, descricao: event.target.value })} /></label>
        <div className="form-actions"><button type="button" className="button" onClick={() => setOpen(false)}>Cancelar</button><button className="button primary">Criar carteira</button></div>
      </form>
    </Modal>
  </div>;
}
