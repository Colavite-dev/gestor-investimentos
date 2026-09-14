import { useEffect, useState } from 'react';
import { ArrowLeft, Clock3, TrendingUp } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { acoesApi } from './api/modules';
import { EmptyState, ErrorAlert } from './components/ui';
import { StockHistoryChart } from './stock-history-chart';
import { sortHistoryChronologically } from './stock-history-model';
import type { Acao, CotacaoHistorica } from './types/api';
import { formatCurrency, formatDate } from './utils/format';
import './stock-history.css';

function HistorySkeleton() {
  return <div className="history-skeleton" aria-label="Carregando histórico de cotações">
    <span className="history-skeleton-heading" />
    <span className="history-skeleton-chart" />
    <span className="history-skeleton-row" />
    <span className="history-skeleton-row" />
  </div>;
}

export function StockHistoryPage() {
  const { id } = useParams();
  return <StockHistoryContent key={id} assetId={Number(id)} />;
}

function StockHistoryContent({ assetId }: { assetId: number }) {
  const invalidAssetId = !Number.isSafeInteger(assetId) || assetId <= 0;
  const [asset, setAsset] = useState<Acao>();
  const [history, setHistory] = useState<CotacaoHistorica[]>([]);
  const [loading, setLoading] = useState(!invalidAssetId);
  const [error, setError] = useState(invalidAssetId ? 'Identificador de ativo inválido.' : '');

  useEffect(() => {
    let active = true;
    if (invalidAssetId) return () => { active = false; };

    Promise.all([acoesApi.get(assetId), acoesApi.history(assetId)])
      .then(([loadedAsset, rows]) => {
        if (!active) return;
        setAsset(loadedAsset);
        setHistory(sortHistoryChronologically(rows));
      })
      .catch(reason => { if (active) setError(reason.message || 'Não foi possível carregar o histórico.'); })
      .finally(() => { if (active) setLoading(false); });

    return () => { active = false; };
  }, [assetId, invalidAssetId]);

  const latest = history.at(-1);

  return <div className="page stock-history-page">
    <Link to="/acoes" className="back-link history-back"><ArrowLeft size={16} /> Ações</Link>
    <header className="page-heading history-page-header">
      <div>
        <p className="page-eyebrow">Mercado</p>
        <h2>{asset ? `Histórico · ${asset.ticker}` : 'Histórico de cotações'}</h2>
        <p className="muted">{asset ? `${asset.nomeEmpresa} · ${asset.moeda}` : 'Preços registrados do ativo ao longo do tempo.'}</p>
      </div>
    </header>

    {error && <ErrorAlert message={error} />}
    {loading ? <HistorySkeleton /> : !history.length || !asset ? <EmptyState title="Nenhum histórico disponível" description="As cotações registradas deste ativo aparecerão aqui." /> : <>
      <section className="panel history-chart-card" aria-labelledby="history-chart-title">
        <div className="history-card-heading">
          <div><p className="section-kicker">Evolução registrada</p><h3 id="history-chart-title">Cotações</h3><span>Histórico de preços registrados</span></div>
          <div className="history-latest-value"><span>Último valor</span><strong>{formatCurrency(latest?.cotacao, asset.moeda)}</strong><small><Clock3 size={13} />{formatDate(latest?.dataHoraCotacao)}</small></div>
        </div>
        <StockHistoryChart history={history} currency={asset.moeda} />
      </section>

      <section className="panel history-table-card" aria-labelledby="history-table-title">
        <div className="panel-heading"><h3 id="history-table-title"><TrendingUp size={17} /> Registros</h3><span className="history-record-count">{history.length} {history.length === 1 ? 'cotação' : 'cotações'}</span></div>
        <div className="table-scroll"><table className="history-table"><thead><tr><th>Data/hora</th><th>Valor</th></tr></thead><tbody>{[...history].reverse().map(row => <tr key={row.id}><td>{formatDate(row.dataHoraCotacao)}</td><td><strong>{formatCurrency(row.cotacao, asset.moeda)}</strong></td></tr>)}</tbody></table></div>
      </section>
    </>}
  </div>;
}
