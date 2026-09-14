import { useCallback, useEffect, useId, useRef, useState } from 'react';
import { Plus } from 'lucide-react';
import { acoesApi, carteirasApi, operacoesApi } from './api/modules';
import type { Acao, AcaoSuggestion, Carteira, Operacao } from './types/api';
import { EmptyState, ErrorAlert, Modal, StatusBadge } from './components/ui';
import { formatCurrency, formatDate, formatNumber } from './utils/format';
import {
  applyOperationQuote,
  beginOperationPricing,
  calculateOperationTotal,
  editOperationPrice,
  emptyOperationPricingState,
  failOperationQuote
} from './operation-pricing';
import { useAuth } from './auth';

type OperationForm = {
  tipo: 'COMPRA' | 'VENDA';
  quantidade: string;
  dataOperacao: string;
  acaoId?: number;
};

function AssetCombobox({ assets, selectedId, onResolved, onResolutionError }: {
  assets: Acao[];
  selectedId?: number;
  onResolved: (asset: Acao, newlyResolved: boolean) => void;
  onResolutionError: (message: string) => void;
}) {
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const [results, setResults] = useState<AcaoSuggestion[]>([]);
  const [searching, setSearching] = useState(false);
  const [resolving, setResolving] = useState(false);
  const [searchError, setSearchError] = useState('');
  const requestId = useRef(0);
  const [activeIndex, setActiveIndex] = useState(-1);
  const listboxId = useId();
  const selected = assets.find(asset => asset.id === selectedId);

  useEffect(() => {
    const term = query.trim();
    if (!open || term.length < 2) {
      setResults([]);
      setSearching(false);
      setSearchError('');
      setActiveIndex(-1);
      return;
    }
    const id = ++requestId.current;
    const timer = window.setTimeout(() => {
      setSearching(true);
      setSearchError('');
      acoesApi.search(term)
        .then(items => { if (requestId.current === id) { setResults(items); setActiveIndex(items.length ? 0 : -1); } })
        .catch(() => { if (requestId.current === id) { setResults([]); setActiveIndex(-1); setSearchError('Não foi possível pesquisar ativos agora.'); } })
        .finally(() => { if (requestId.current === id) setSearching(false); });
    }, 350);
    return () => window.clearTimeout(timer);
  }, [open, query]);

  const choose = async (suggestion: AcaoSuggestion) => {
    if (resolving) return;
    ++requestId.current;
    setSearching(false);
    setSearchError('');
    const existing = assets.find(asset => asset.ticker === suggestion.ticker && asset.mercado === suggestion.mercado);
    if (existing) {
      onResolved(existing, false);
      setOpen(false);
      setQuery('');
      setActiveIndex(-1);
      return;
    }
    setResolving(true);
    try {
      const resolved = await acoesApi.resolve({ ticker: suggestion.ticker, mercado: suggestion.mercado });
      onResolved(resolved, true);
      setOpen(false);
      setQuery('');
      setActiveIndex(-1);
    } catch (error) {
      onResolutionError((error as Error).message || 'Não foi possível validar o ativo.');
    } finally {
      setResolving(false);
    }
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Escape') {
      setOpen(false);
      setActiveIndex(-1);
      return;
    }
    if (!results.length) return;
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setOpen(true);
      setActiveIndex(current => (current + 1) % results.length);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      setOpen(true);
      setActiveIndex(current => current <= 0 ? results.length - 1 : current - 1);
    } else if (event.key === 'Enter' && activeIndex >= 0) {
      event.preventDefault();
      void choose(results[activeIndex]);
    }
  };

  return <label className="action-combobox">Ativo
    <div className="combobox-root">
      <input
        value={open ? query : selected ? `${selected.ticker} · ${selected.mercado === 'BRASIL' ? 'BR' : 'US'}` : ''}
        onFocus={() => setOpen(true)}
        onChange={event => { setQuery(event.target.value); setOpen(true); setActiveIndex(-1); }}
        onKeyDown={handleKeyDown}
        placeholder="Busque por ticker ou empresa"
        autoComplete="off"
        role="combobox"
        aria-autocomplete="list"
        aria-controls={listboxId}
        aria-activedescendant={open && activeIndex >= 0 ? `${listboxId}-option-${activeIndex}` : undefined}
        aria-expanded={open}
        aria-haspopup="listbox"
      />
      {open && <div id={listboxId} className="combobox-list" role="listbox" aria-label="Resultados da busca de ativos">
        {resolving ? <div className="combobox-empty">Validando ativo...</div>
          : searching ? <div className="combobox-empty">Pesquisando...</div>
            : searchError ? <div className="combobox-empty combobox-error">{searchError}<small>A ação selecionada e sua cotação não foram alteradas.</small></div>
            : results.length ? results.map((item, index) => <button id={`${listboxId}-option-${index}`} type="button" role="option" aria-selected={activeIndex === index} className={activeIndex === index ? 'active' : undefined} key={`${item.ticker}-${item.mercado}`} onClick={() => void choose(item)}>
              <strong>{item.ticker} · {item.mercado === 'BRASIL' ? 'BR' : 'US'} · {item.moeda}</strong>
              <span>{item.nomeEmpresa || 'Empresa não informada'}</span>
            </button>)
              : <div className="combobox-empty">{query.trim().length < 2 ? 'Digite ao menos 2 caracteres.' : 'Nenhum ativo encontrado.'}</div>}
      </div>}
    </div>
  </label>;
}

export function OperationsPage() {
  const { user } = useAuth();
  const [wallets, setWallets] = useState<Carteira[]>([]);
  const [assets, setAssets] = useState<Acao[]>([]);
  const [selectedWallet, setSelectedWallet] = useState<number>();
  const [operations, setOperations] = useState<Operacao[]>([]);
  const [form, setForm] = useState<OperationForm>({ tipo: 'COMPRA', quantidade: '', dataOperacao: new Date().toISOString().slice(0, 16) });
  const [open, setOpen] = useState(false);
  const [error, setError] = useState('');
  const [selectionError, setSelectionError] = useState('');
  const [pricing, setPricing] = useState(emptyOperationPricingState);
  const quoteRequestId = useRef(0);
  const operationsRequestId = useRef(0);

  const reportSelectionError = useCallback((message: string) => setSelectionError(message), []);
  const loadOperations = useCallback((walletId: number) => {
    const requestId = ++operationsRequestId.current;
    setOperations([]);
    return operacoesApi.listByCarteira(walletId)
      .then(items => { if (requestId === operationsRequestId.current) setOperations(items); })
      .catch(apiError => { if (requestId === operationsRequestId.current) setError(apiError.message); });
  }, []);

  useEffect(() => {
    let active = true;
    ++operationsRequestId.current;
    setWallets([]); setAssets([]); setSelectedWallet(undefined); setOperations([]); setError('');
    Promise.all([carteirasApi.list(), acoesApi.list()])
      .then(([loadedWallets, loadedAssets]) => {
        if (!active) return;
        setWallets(loadedWallets);
        setAssets(loadedAssets);
        const firstWallet = loadedWallets[0]?.id;
        setSelectedWallet(firstWallet);
        if (firstWallet) void loadOperations(firstWallet);
      })
      .catch(apiError => { if (active) setError(apiError.message); });
    return () => { active = false; ++operationsRequestId.current; };
  }, [loadOperations, user?.id]);

  const selectAsset = (asset: Acao, newlyResolved: boolean) => {
    const id = ++quoteRequestId.current;
    setAssets(current => current.some(item => item.id === asset.id) ? current : [...current, asset]);
    setForm(current => ({ ...current, acaoId: asset.id }));
    setSelectionError('');
    setPricing(beginOperationPricing(asset.id, id, asset.cotacaoAtual, !newlyResolved));

    const applyQuote = (quotedAsset: Acao) => {
      setAssets(current => current.map(item => item.id === quotedAsset.id ? quotedAsset : item));
      setPricing(current => applyOperationQuote(current, quotedAsset.id, id, quotedAsset.cotacaoAtual));
    };

    if (newlyResolved) {
      applyQuote(asset);
      return;
    }

    acoesApi.updateQuote(asset.id)
      .then(applyQuote)
      .catch(apiError => {
        setPricing(current => failOperationQuote(current, asset.id, id, apiError.message));
      });
  };

  const selectedAsset = assets.find(asset => asset.id === form.acaoId);
  const currency = selectedAsset?.moeda || 'BRL';
  const total = calculateOperationTotal(form.quantidade, pricing.unitPrice);
  const canSubmit = Boolean(selectedWallet && form.acaoId && Number(form.quantidade) > 0 && Number(pricing.unitPrice) > 0 && form.dataOperacao);

  const close = () => {
    ++quoteRequestId.current;
    setOpen(false);
    setError('');
    setSelectionError('');
    setPricing(emptyOperationPricingState);
    setForm({ tipo: 'COMPRA', quantidade: '', dataOperacao: new Date().toISOString().slice(0, 16) });
  };

  const save = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canSubmit) return;
    try {
      await operacoesApi.create({
        carteiraId: selectedWallet,
        acaoId: form.acaoId,
        tipo: form.tipo,
        quantidade: Number(form.quantidade),
        precoUnitario: Number(pricing.unitPrice),
        dataOperacao: new Date(form.dataOperacao).toISOString()
      });
      const walletId = selectedWallet;
      close();
      if (walletId) await loadOperations(walletId);
    } catch (apiError) {
      setError((apiError as Error).message);
    }
  };

  return <div className="page">
    <div className="page-heading"><div><p className="muted">Gestão</p><h2>Operações</h2><p className="muted">Registre o preço realmente executado em cada compra ou venda.</p></div><button className="button primary" onClick={() => setOpen(true)}><Plus size={16}/> Nova operação</button></div>
    <label className="select-wrap inline">Carteira<select value={selectedWallet} onChange={event => { const id = Number(event.target.value); setSelectedWallet(id); void loadOperations(id); }}>{wallets.map(wallet => <option value={wallet.id} key={wallet.id}>{wallet.nome}</option>)}</select></label>
    {error && !open && <ErrorAlert message={error}/>}
    <div className="panel table-scroll"><table><thead><tr><th>Data</th><th>Ativo</th><th>Tipo</th><th>Quantidade</th><th>Preço unitário</th></tr></thead><tbody>{operations.map(operation => <tr key={operation.id}><td>{formatDate(operation.dataOperacao)}</td><td className="ticker">{operation.ticker}</td><td><StatusBadge label={operation.tipo} tone={operation.tipo === 'COMPRA' ? 'success' : 'danger'}/></td><td>{formatNumber(operation.quantidade)}</td><td>{formatCurrency(operation.precoUnitario, operation.moeda)}</td></tr>)}</tbody></table>{!operations.length && <EmptyState title="Nenhuma operação" description="Registre uma compra ou venda."/>}</div>

    <Modal open={open} onClose={close} title="Nova operação"><form className="operation-form" onSubmit={save}>
      <AssetCombobox assets={assets} selectedId={form.acaoId} onResolved={selectAsset} onResolutionError={reportSelectionError}/>
      {selectionError && <ErrorAlert message={selectionError}/>}
      <div className={`market-quote ${pricing.error ? 'quote-error' : ''}`} aria-live="polite">
        <span>Cotação atual do mercado</span>
        <strong>{pricing.marketQuote == null ? pricing.loading ? 'Consultando...' : 'Não disponível' : formatCurrency(pricing.marketQuote, currency)}</strong>
        {pricing.loading && pricing.marketQuote != null && <small>Atualizando cotação...</small>}
      </div>
      {pricing.notice && <p className="quote-notice">{pricing.notice}</p>}
      {pricing.error && <ErrorAlert message={pricing.error}/>}
      <div className="operation-type" role="group" aria-label="Tipo da operação"><button type="button" className={form.tipo === 'COMPRA' ? 'selected compra' : ''} aria-pressed={form.tipo === 'COMPRA'} onClick={() => setForm(current => ({ ...current, tipo: 'COMPRA' }))}>Compra</button><button type="button" className={form.tipo === 'VENDA' ? 'selected venda' : ''} aria-pressed={form.tipo === 'VENDA'} onClick={() => setForm(current => ({ ...current, tipo: 'VENDA' }))}>Venda</button></div>
      <div className="operation-grid">
        <label>Data da transação<input required type="datetime-local" value={form.dataOperacao} onChange={event => setForm(current => ({ ...current, dataOperacao: event.target.value }))}/></label>
        <label>Quantidade<input required min="0.00000001" step="any" type="number" inputMode="decimal" value={form.quantidade} onChange={event => setForm(current => ({ ...current, quantidade: event.target.value }))}/></label>
        <label>Preço unitário <span className="currency-hint">{currency === 'USD' ? 'US$' : 'R$'}</span><input required min="0.00000001" step="any" type="number" inputMode="decimal" value={pricing.unitPrice} onChange={event => setPricing(current => editOperationPrice(current, event.target.value))}/></label>
      </div>
      <div className="operation-total"><span>Total da operação</span><strong>{formatCurrency(total, currency)}</strong></div>
      {error && <ErrorAlert message={error}/>}
      <div className="form-actions"><button type="button" className="button" onClick={close}>Cancelar</button><button className="button primary" disabled={!canSubmit}>Registrar operação</button></div>
    </form></Modal>
  </div>;
}
