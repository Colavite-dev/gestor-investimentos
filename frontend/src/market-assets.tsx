import { useEffect, useMemo, useRef, useState } from 'react';
import {
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Clock3,
  History,
  LoaderCircle,
  RefreshCw,
  Search,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { acoesApi } from './api/modules';
import type { Acao, MarketAsset, Mercado } from './types/api';
import { formatCurrency, formatDate } from './utils/format';
import { EmptyState, ErrorAlert, StatusBadge, Toast } from './components/ui';
import './stock-page.css';

function assetKey(asset: Pick<MarketAsset, 'ticker' | 'mercado'>) {
  return `${asset.ticker}-${asset.mercado}`;
}

function AssetLogo({ asset }: { asset: Pick<MarketAsset, 'ticker' | 'mercado'> & { logoUrl?: string } }) {
  const [failed, setFailed] = useState(false);
  const safeLogo = useMemo(() => {
    if (asset.mercado !== 'BRASIL' || !asset.logoUrl) return undefined;
    try {
      const url = new URL(asset.logoUrl);
      return url.protocol === 'https:' ? url.toString() : undefined;
    } catch {
      return undefined;
    }
  }, [asset.logoUrl, asset.mercado]);

  if (!safeLogo || failed) {
    return <span className="stock-logo stock-logo-fallback" aria-label={`Ícone padrão de ${asset.ticker}`}>{asset.ticker.slice(0, 1)}</span>;
  }

  return <span className="stock-logo"><img src={safeLogo} alt="" loading="lazy" referrerPolicy="no-referrer" onError={() => setFailed(true)} /></span>;
}

function MarketBadge({ mercado }: { mercado: Mercado }) {
  return <StatusBadge label={mercado === 'BRASIL' ? 'BR' : 'US'} />;
}

function StockQuote({ value, currency, catalog }: { value?: number | null; currency: string; catalog?: boolean }) {
  return <strong className="stock-quote">{value == null ? (catalog ? 'Cotação ao acompanhar' : '—') : formatCurrency(value, currency)}</strong>;
}

function CatalogSkeleton() {
  return <div className="stock-catalog-skeleton" aria-label="Carregando catálogo de ativos">
    {[0, 1, 2, 3].map(item => <span className="stock-skeleton-line" key={item} />)}
  </div>;
}

export function MarketAssetsPage() {
  const [localAssets, setLocalAssets] = useState<Acao[]>([]);
  const [catalog, setCatalog] = useState<MarketAsset[]>([]);
  const [market, setMarket] = useState<Mercado>('BRASIL');
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [total, setTotal] = useState<number>();
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [localLoading, setLocalLoading] = useState(true);
  const [catalogError, setCatalogError] = useState('');
  const [resolutionError, setResolutionError] = useState('');
  const [localError, setLocalError] = useState('');
  const [resolvingKey, setResolvingKey] = useState('');
  const [updatingId, setUpdatingId] = useState<number>();
  const [toast, setToast] = useState('');
  const requestId = useRef(0);

  useEffect(() => {
    let active = true;
    acoesApi.list()
      .then(items => { if (active) setLocalAssets(items); })
      .catch(error => { if (active) setLocalError(error.message || 'Não foi possível carregar os ativos acompanhados.'); })
      .finally(() => { if (active) setLocalLoading(false); });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    const id = ++requestId.current;
    const timer = window.setTimeout(() => {
      setCatalogLoading(true);
      setCatalogError('');
      acoesApi.catalog({ mercado: market, q: query.trim(), page, size: 20 })
        .then(response => {
          if (requestId.current !== id) return;
          setCatalog(response.items);
          setHasNext(response.hasNext);
          setTotal(response.totalElements);
        })
        .catch(error => {
          if (requestId.current !== id) return;
          setCatalog([]);
          setHasNext(false);
          setTotal(undefined);
          setCatalogError(error.message || 'Não foi possível pesquisar ativos neste momento.');
        })
        .finally(() => { if (requestId.current === id) setCatalogLoading(false); });
    }, query.trim() ? 350 : 0);
    return () => window.clearTimeout(timer);
  }, [market, page, query]);

  const trackedKeys = useMemo(() => new Set(localAssets.map(assetKey)), [localAssets]);
  const summary = useMemo(() => ({
    total: localAssets.length,
    br: localAssets.filter(asset => asset.mercado === 'BRASIL').length,
    us: localAssets.filter(asset => asset.mercado === 'ESTADOS_UNIDOS').length,
    quoted: localAssets.filter(asset => asset.cotacaoAtual != null).length,
  }), [localAssets]);

  async function resolveAsset(asset: MarketAsset) {
    const key = assetKey(asset);
    setResolutionError('');
    setResolvingKey(key);
    try {
      const resolved = await acoesApi.resolve({ ticker: asset.ticker, mercado: asset.mercado });
      setLocalAssets(current => current.some(item => item.id === resolved.id) ? current : [...current, resolved]);
      setToast(`${resolved.ticker} está disponível nos ativos acompanhados.`);
    } catch (error) {
      setResolutionError((error as Error).message || 'Não foi possível carregar esse ativo.');
    } finally {
      setResolvingKey('');
    }
  }

  async function refreshQuote(asset: Acao) {
    setLocalError('');
    setUpdatingId(asset.id);
    try {
      const updated = await acoesApi.updateQuote(asset.id);
      setLocalAssets(current => current.map(item => item.id === updated.id ? updated : item));
      setToast('Cotação atualizada.');
    } catch (error) {
      setLocalError((error as Error).message || 'Não foi possível atualizar a cotação.');
    } finally {
      setUpdatingId(undefined);
    }
  }

  const catalogTitle = market === 'BRASIL' ? 'Mercado brasileiro' : 'Mercado americano';

  return <div className="page stock-page">
    <header className="page-heading stock-page-header">
      <div>
        <p className="page-eyebrow">Mercado</p>
        <h2>Ações</h2>
        <p className="muted">Pesquise ativos dos mercados brasileiro e americano e acompanhe suas cotações.</p>
      </div>
    </header>

    <section className="stock-explorer panel" aria-labelledby="stock-explorer-title">
      <div className="stock-section-heading">
        <div><p className="section-kicker">Descoberta de mercado</p><h3 id="stock-explorer-title">Explorar ativos</h3><p>Pesquise por ticker ou nome da empresa.</p></div>
        <div className="stock-market-switch" role="group" aria-label="Selecionar mercado">
          <button className={market === 'BRASIL' ? 'active' : ''} onClick={() => { setMarket('BRASIL'); setPage(0); }} type="button">Brasil</button>
          <button className={market === 'ESTADOS_UNIDOS' ? 'active' : ''} onClick={() => { setMarket('ESTADOS_UNIDOS'); setPage(0); }} type="button">Estados Unidos</button>
        </div>
      </div>
      <label className="stock-search" htmlFor="stock-search">
        <Search size={19} aria-hidden="true" />
        <input id="stock-search" value={query} onChange={event => { setQuery(event.target.value); setPage(0); }} placeholder="Busque por ticker ou empresa..." autoComplete="off" />
        {catalogLoading && <LoaderCircle className="spin" size={18} aria-label="Pesquisando ativos" />}
      </label>
      <p className="stock-search-examples">Exemplos: PETR4, VALE3, AAPL, MSFT</p>

      {catalogError && <ErrorAlert message={catalogError} />}
      {resolutionError && <ErrorAlert message={resolutionError} />}
      {catalogLoading ? <CatalogSkeleton /> : catalog.length ? <>
        <div className="stock-catalog-grid" aria-live="polite">
          {catalog.map(asset => {
            const key = assetKey(asset);
            const tracked = trackedKeys.has(key);
            const resolving = resolvingKey === key;
            return <article className="stock-catalog-card" key={key}>
              <AssetLogo asset={asset} />
              <div className="stock-catalog-main"><strong className="ticker">{asset.ticker}</strong><span>{asset.nomeEmpresa || 'Empresa não informada'}</span><small>{asset.exchange || (asset.mercado === 'BRASIL' ? 'B3' : 'Exchange não informada')}</small></div>
              <div className="stock-catalog-meta"><MarketBadge mercado={asset.mercado} /><span>{asset.moeda}</span><StockQuote value={asset.cotacaoAtual} currency={asset.moeda} catalog /></div>
              <button className="stock-track-button" disabled={tracked || resolving} type="button" onClick={() => void resolveAsset(asset)}>
                {resolving ? <><LoaderCircle className="spin" size={15} /> Carregando ativo...</> : tracked ? <><CheckCircle2 size={15} /> Acompanhado</> : 'Acompanhar'}
              </button>
            </article>;
          })}
        </div>
        <nav className="stock-pagination" aria-label="Paginação do catálogo">
          <button className="button button-secondary" disabled={page === 0} onClick={() => setPage(current => Math.max(0, current - 1))} type="button"><ChevronLeft size={16} /> Anterior</button>
          <span>{catalogTitle} · Página {page + 1}{total == null ? '' : ` · ${total} ativos`}</span>
          <button className="button button-secondary" disabled={!hasNext} onClick={() => setPage(current => current + 1)} type="button">Próxima <ChevronRight size={16} /></button>
        </nav>
      </> : !catalogError ? <EmptyState title="Nenhum ativo encontrado" description="Tente outro mercado ou termo de busca." /> : null}
    </section>

    <section className="stock-tracked-section" aria-labelledby="tracked-stocks-title">
      <div className="stock-section-heading stock-tracked-heading">
        <div><p className="section-kicker">Acompanhamento</p><h3 id="tracked-stocks-title">Ativos acompanhados</h3><p>Ativos disponíveis no sistema com suas últimas cotações.</p></div>
      </div>
      <div className="stock-summary" aria-label="Resumo dos ativos acompanhados">
        <span><strong>{summary.total}</strong> ativos</span><span><strong>{summary.br}</strong> BR</span><span><strong>{summary.us}</strong> US</span><span><strong>{summary.quoted}</strong> com cotação</span>
      </div>
      {localError && <ErrorAlert message={localError} />}
      {localLoading ? <CatalogSkeleton /> : !localAssets.length ? <EmptyState title="Nenhum ativo acompanhado" description="Pesquise um ativo acima para começar." /> : <>
        <div className="stock-table-wrap"><table className="stock-table"><thead><tr><th>Ativo</th><th>Empresa</th><th>Mercado</th><th>Moeda</th><th>Cotação</th><th>Atualizado</th><th><span className="sr-only">Ações</span></th></tr></thead><tbody>{localAssets.map(asset => <tr key={asset.id}><td><div className="stock-table-ticker"><AssetLogo asset={asset} /><strong className="ticker">{asset.ticker}</strong></div></td><td>{asset.nomeEmpresa || '—'}</td><td><MarketBadge mercado={asset.mercado} /></td><td>{asset.moeda}</td><td><StockQuote value={asset.cotacaoAtual} currency={asset.moeda} /></td><td>{asset.dataHoraCotacao ? <span className="stock-updated"><Clock3 size={14} />{formatDate(asset.dataHoraCotacao)}</span> : '—'}</td><td><div className="stock-row-actions"><Link to={`/acoes/${asset.id}/historico`} className="icon-button" title="Ver histórico" aria-label={`Ver histórico de ${asset.ticker}`}><History size={16} /></Link><button className="icon-button" title="Atualizar cotação" aria-label={`Atualizar cotação de ${asset.ticker}`} disabled={updatingId === asset.id} onClick={() => void refreshQuote(asset)} type="button"><RefreshCw className={updatingId === asset.id ? 'spin' : ''} size={16} /></button></div></td></tr>)}</tbody></table></div>
        <div className="stock-mobile-cards">{localAssets.map(asset => <article className="stock-mobile-card" key={asset.id}><div className="stock-mobile-top"><div className="stock-table-ticker"><AssetLogo asset={asset} /><div><strong className="ticker">{asset.ticker}</strong><small>{asset.nomeEmpresa || 'Empresa não informada'}</small></div></div><MarketBadge mercado={asset.mercado} /></div><div className="stock-mobile-quote"><span>Cotação atual</span><StockQuote value={asset.cotacaoAtual} currency={asset.moeda} /></div><div className="stock-mobile-footer"><span>{asset.dataHoraCotacao ? formatDate(asset.dataHoraCotacao) : 'Cotação não disponível'}</span><div className="stock-row-actions"><Link to={`/acoes/${asset.id}/historico`} className="icon-button" aria-label={`Ver histórico de ${asset.ticker}`}><History size={16} /></Link><button className="icon-button" aria-label={`Atualizar cotação de ${asset.ticker}`} disabled={updatingId === asset.id} onClick={() => void refreshQuote(asset)} type="button"><RefreshCw className={updatingId === asset.id ? 'spin' : ''} size={16} /></button></div></div></article>)}</div>
      </>}
    </section>
    <Toast message={toast} onClose={() => setToast('')} />
  </div>;
}
