import { useEffect, useState } from 'react';
import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Link, useParams } from 'react-router-dom';
import { PieChart as PieIcon, Plus, RefreshCw } from 'lucide-react';
import { carteirasApi } from './api/modules';
import type { Carteira, CarteiraResumo, Operacao, Posicao } from './types/api';
import { EmptyState, ErrorAlert, LoadingState, StatusBadge } from './components/ui';
import { CHART_COLORS, categoricalChartColor } from './chart-theme';
import { formatCurrency, formatDate, formatNumber, formatOptionalCurrency, formatPercent } from './utils/format';
import './dashboard-charts.css';
import { useAuth } from './auth';

type ChartTooltipItem = {
  color?: string;
  dataKey?: string | number;
  name?: string;
  value?: string | number;
  payload?: { percent?: number };
};

function prefersReducedMotion() {
  return typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

function formatAxisValue(value: number) {
  return new Intl.NumberFormat('pt-BR', { notation: 'compact', maximumFractionDigits: 1 }).format(value);
}

function ValuationTooltip({ active, payload, label }: { active?: boolean; payload?: readonly ChartTooltipItem[]; label?: string }) {
  if (!active || !payload?.length || (label !== 'BRL' && label !== 'USD')) return null;
  return <div className="chart-tooltip" role="status">
    <strong className="chart-tooltip-title">{label}</strong>
    {payload.map(item => <div className="chart-tooltip-row" key={String(item.dataKey)}>
      <span><i style={{ background: item.color }}/>{item.name}</span>
      <strong>{formatCurrency(Number(item.value), label)}</strong>
    </div>)}
  </div>;
}

function DistributionTooltip({ active, payload, currency }: { active?: boolean; payload?: readonly ChartTooltipItem[]; currency: 'BRL' | 'USD' }) {
  const item = payload?.[0];
  if (!active || !item) return null;
  return <div className="chart-tooltip chart-tooltip-compact" role="status">
    <div className="chart-tooltip-row"><span><i style={{ background: item.color }}/>{item.name}</span><strong>{formatCurrency(Number(item.value), currency)}</strong></div>
    {item.payload?.percent != null && <div className="chart-tooltip-row chart-tooltip-detail"><span>Participação</span><strong>{item.payload.percent.toFixed(1)}%</strong></div>}
  </div>;
}

function ValuationLegend() {
  return <div className="chart-series-legend" aria-label="Séries do gráfico">
    <span><i style={{ background: CHART_COLORS.invested }}/>Valor investido</span>
    <span><i style={{ background: CHART_COLORS.current }}/>Valor atual</span>
  </div>;
}

function Metric({ label, value, tone }: { label: string; value: string; tone?: string }) {
  return <div className="metric"><span>{label}</span><strong className={tone}>{value}</strong></div>;
}

function SummaryByCurrency({ summary }: { summary?: CarteiraResumo }) {
  return <>{(['BRL', 'USD'] as const).map(currency => {
    const item = summary?.porMoeda?.[currency];
    if (!item) return null;
    const tone = item.lucroPrejuizo == null ? undefined : Number(item.lucroPrejuizo) >= 0 ? 'positive' : 'negative';
    return <section className="currency-block" key={currency}>
      <div className="section-title"><span>{currency}</span><span className="muted">{item.quantidadePosicoes} posições</span></div>
      <div className="metric-grid valuation-metrics">
        <Metric label="Valor investido (custo)" value={formatCurrency(item.valorInvestido, currency)}/>
        <Metric label="Valor atual" value={formatOptionalCurrency(item.patrimonioAtual, currency)}/>
        <Metric label="Resultado" value={formatOptionalCurrency(item.lucroPrejuizo, currency)} tone={tone}/>
        <Metric label="Rentabilidade" value={formatPercent(item.rentabilidadePercentual)} tone={tone}/>
      </div>
    </section>;
  })}</>;
}

function ValuationComparison({ summary }: { summary?: CarteiraResumo }) {
  const data = (['BRL', 'USD'] as const).flatMap(currency => {
    const item = summary?.porMoeda?.[currency];
    return item ? [{ moeda: currency, investido: Number(item.valorInvestido), atual: item.patrimonioAtual == null ? 0 : Number(item.patrimonioAtual) }] : [];
  });
  if (!data.length) return null;
  return <div className="panel valuation-chart"><div className="panel-heading"><h3>Valor investido × valor atual</h3></div><ResponsiveContainer width="100%" height={245}><BarChart data={data} margin={{ top: 22, right: 24, left: 8, bottom: 5 }}><CartesianGrid strokeDasharray="3 3" stroke="#30363d"/><XAxis dataKey="moeda" stroke="#8b949e"/><YAxis stroke="#8b949e"/><Tooltip contentStyle={{ background: '#1c2128', border: '1px solid #30363d' }}/><Legend/><Bar dataKey="investido" name="Valor investido" fill="#79b8ff" radius={[4, 4, 0, 0]}/><Bar dataKey="atual" name="Valor atual" fill="#3fb950" radius={[4, 4, 0, 0]}/></BarChart></ResponsiveContainer></div>;
}

void ValuationComparison;

function PremiumValuationComparison({ summary }: { summary?: CarteiraResumo }) {
  const data = (['BRL', 'USD'] as const).flatMap(currency => {
    const item = summary?.porMoeda?.[currency];
    return item ? [{ moeda: currency, investido: Number(item.valorInvestido), atual: item.patrimonioAtual == null ? 0 : Number(item.patrimonioAtual) }] : [];
  });
  if (!data.length) return null;
  const animate = !prefersReducedMotion();
  return <div className="panel valuation-chart dashboard-valuation-chart portfolio-valuation-chart">
    <div className="panel-heading chart-card-heading"><div><span className="chart-card-eyebrow">Comparativo por moeda</span><h3>Valor investido × valor atual</h3></div><ValuationLegend /></div>
    <div className="valuation-chart-body" role="img" aria-label="Comparação entre valor investido e valor atual, com BRL e USD mantidos separadamente">
      <ResponsiveContainer width="100%" height={218}><BarChart data={data} margin={{ top: 12, right: 18, left: 0, bottom: 2 }} barCategoryGap="58%" barGap={8}>
        <CartesianGrid vertical={false} stroke="var(--color-chart-grid)" strokeDasharray="3 7" />
        <XAxis dataKey="moeda" axisLine={false} tickLine={false} tickMargin={12} tick={{ fill: 'var(--color-text-muted)', fontSize: 12 }} />
        <YAxis axisLine={false} tickLine={false} width={54} tickFormatter={formatAxisValue} tick={{ fill: 'var(--color-text-muted)', fontSize: 11 }} />
        <Tooltip content={<ValuationTooltip />} cursor={{ fill: CHART_COLORS.hover }} wrapperStyle={{ outline: 'none' }} />
        <Bar dataKey="investido" name="Valor investido" fill={CHART_COLORS.invested} barSize={16} maxBarSize={18} radius={[6, 6, 2, 2]} isAnimationActive={animate} animationDuration={520} />
        <Bar dataKey="atual" name="Valor atual" fill={CHART_COLORS.current} barSize={16} maxBarSize={18} radius={[6, 6, 2, 2]} isAnimationActive={animate} animationDuration={580} />
      </BarChart></ResponsiveContainer>
    </div>
  </div>;
}

function DashboardValuationComparison({ summary }: { summary?: CarteiraResumo }) {
  const data = (['BRL', 'USD'] as const).flatMap(currency => {
    const item = summary?.porMoeda?.[currency];
    return item ? [{ moeda: currency, investido: Number(item.valorInvestido), atual: item.patrimonioAtual == null ? 0 : Number(item.patrimonioAtual) }] : [];
  });
  if (!data.length) return null;
  const animate = !prefersReducedMotion();
  return <div className="panel valuation-chart dashboard-valuation-chart">
    <div className="panel-heading chart-card-heading">
      <div><span className="chart-card-eyebrow">Comparativo por moeda</span><h3>Valor investido × valor atual</h3></div>
      <ValuationLegend/>
    </div>
    <div className="valuation-chart-body" role="img" aria-label="Comparação entre valor investido e valor atual, com BRL e USD mantidos separadamente">
      <ResponsiveContainer width="100%" height={218}>
        <BarChart data={data} margin={{ top: 12, right: 18, left: 0, bottom: 2 }} barCategoryGap="58%" barGap={8}>
          <CartesianGrid vertical={false} stroke="var(--color-chart-grid)" strokeDasharray="3 7"/>
          <XAxis dataKey="moeda" axisLine={false} tickLine={false} tickMargin={12} tick={{ fill: 'var(--color-text-muted)', fontSize: 12 }}/>
          <YAxis axisLine={false} tickLine={false} width={54} tickFormatter={formatAxisValue} tick={{ fill: 'var(--color-text-muted)', fontSize: 11 }}/>
          <Tooltip content={<ValuationTooltip/>} cursor={{ fill: CHART_COLORS.hover }} wrapperStyle={{ outline: 'none' }}/>
          <Bar dataKey="investido" name="Valor investido" fill={CHART_COLORS.invested} barSize={18} maxBarSize={20} radius={[7, 7, 2, 2]} isAnimationActive={animate} animationDuration={520}/>
          <Bar dataKey="atual" name="Valor atual" fill={CHART_COLORS.current} barSize={18} maxBarSize={20} radius={[7, 7, 2, 2]} isAnimationActive={animate} animationDuration={580}/>
        </BarChart>
      </ResponsiveContainer>
    </div>
  </div>;
}

function Distribution({ positions }: { positions: Posicao[] }) {
  return <div className="distribution-grid">{(['BRL', 'USD'] as const).map(currency => {
    const rows = positions.filter(position => position.moeda === currency && position.patrimonioAtual != null);
    const total = rows.reduce((sum, position) => sum + Number(position.patrimonioAtual), 0);
    if (!rows.length || total <= 0) return null;
    const data = rows.map(position => ({ name: position.ticker, value: Number(position.patrimonioAtual), percent: Number(position.patrimonioAtual) / total * 100 }));
    const animate = !prefersReducedMotion();
    const paddingAngle = data.length > 1 ? 5 : 0;
    return <div className="distribution panel" key={currency}><div className="panel-heading"><h3><PieIcon size={16}/> Distribuição pelo valor atual — {currency}</h3></div><div className="distribution-body" role="img" aria-label={`Distribuição pelo valor atual em ${currency} com ${data.length} ${data.length === 1 ? 'ativo' : 'ativos'}`}><ResponsiveContainer width="100%" height={200}><PieChart><Pie data={data} dataKey="value" nameKey="name" innerRadius={48} outerRadius={80} paddingAngle={paddingAngle} cornerRadius={9} isAnimationActive={animate}>{data.map((_, index) => <Cell key={index} fill={categoricalChartColor(index)}/>)}</Pie><Tooltip content={<DistributionTooltip currency={currency}/>} cursor={false} wrapperStyle={{ outline: 'none' }}/></PieChart></ResponsiveContainer><div className="legend">{data.map((item, index) => <div className="legend-row" key={item.name}><span><i style={{ background: categoricalChartColor(index) }}/> <strong>{item.name}</strong></span><span>{item.percent.toFixed(1)}% · {formatCurrency(item.value, currency)}</span></div>)}</div></div></div>;
  })}</div>;
}

function PositionsTable({ rows }: { rows: Posicao[] }) {
  return <div className="table-scroll"><table><thead><tr><th>Ativo</th><th>Quantidade</th><th>Preço médio</th><th>Cotação atual</th><th>Custo</th><th>Valor atual</th><th>Resultado</th><th>Rentabilidade</th></tr></thead><tbody>{rows.map(position => {
    const tone = position.lucroPrejuizo == null ? undefined : Number(position.lucroPrejuizo) >= 0 ? 'positive' : 'negative';
    return <tr key={position.acaoId}><td><strong className="ticker">{position.ticker}</strong><br/><StatusBadge label={position.mercado === 'BRASIL' ? 'Brasil' : 'EUA'}/></td><td>{formatNumber(position.quantidade)}</td><td>{formatCurrency(position.precoMedio, position.moeda)}</td><td>{formatOptionalCurrency(position.cotacaoAtual, position.moeda)}</td><td>{formatCurrency(position.valorInvestido, position.moeda)}</td><td>{formatOptionalCurrency(position.patrimonioAtual, position.moeda)}</td><td className={tone}>{formatOptionalCurrency(position.lucroPrejuizo, position.moeda)}</td><td className={tone}>{formatPercent(position.rentabilidadePercentual)}</td></tr>;
  })}</tbody></table></div>;
}

export function DashboardPage() {
  const { user } = useAuth();
  const [wallets, setWallets] = useState<Carteira[]>([]);
  const [selected, setSelected] = useState<number>();
  const [summary, setSummary] = useState<CarteiraResumo>();
  const [positions, setPositions] = useState<Posicao[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setWallets([]); setSelected(undefined); setSummary(undefined); setPositions([]); setError(''); setLoading(true);
    carteirasApi.list().then(items => { if (active) { setWallets(items); setSelected(items[0]?.id); } })
      .catch(apiError => { if (active) setError(apiError.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [user?.id]);
  useEffect(() => {
    let active = true;
    setSummary(undefined); setPositions([]); setError('');
    if (selected) Promise.all([carteirasApi.summary(selected), carteirasApi.positions(selected)])
      .then(([loadedSummary, loadedPositions]) => { if (active) { setSummary(loadedSummary); setPositions(loadedPositions); } })
      .catch(apiError => { if (active) setError(apiError.message); });
    return () => { active = false; };
  }, [selected, user?.id]);

  if (loading) return <LoadingState/>;
  if (error) return <ErrorAlert message={error}/>;
  if (!wallets.length) return <EmptyState title="Nenhuma carteira cadastrada" description="Crie sua primeira carteira para acompanhar seus investimentos." action={<Link className="button primary" to="/carteiras"><Plus size={16}/> Criar carteira</Link>}/>;
  return <div className="page"><div className="page-heading"><div><p className="muted">Visão geral</p><h2>Seus investimentos</h2><p className="muted">Custo vem das operações; valor atual vem das cotações de mercado.</p></div><label className="select-wrap">Carteira<select value={selected} onChange={event => setSelected(Number(event.target.value))}>{wallets.map(wallet => <option value={wallet.id} key={wallet.id}>{wallet.nome}</option>)}</select></label></div><SummaryByCurrency summary={summary}/><DashboardValuationComparison summary={summary}/><Distribution positions={positions}/><div className="panel"><div className="panel-heading"><h3>Posições atuais</h3><Link to={`/carteiras/${selected}`}>Ver carteira →</Link></div>{positions.length ? <PositionsTable rows={positions}/> : <EmptyState title="Nenhuma posição nesta carteira" description="Registre uma compra para começar."/>}</div></div>;
}

function PortfolioDetailPremium({ wallet, summary, positions, operations }: { wallet?: Carteira; summary?: CarteiraResumo; positions: Posicao[]; operations: Operacao[] }) {
  return <div className="page portfolio-detail-page"><Link to="/carteiras" className="back-link">← Carteiras</Link><header className="page-heading portfolio-page-heading"><div><p className="page-eyebrow">Carteira</p><h2>{wallet?.nome}</h2><p className="muted">{wallet?.descricao || 'Resumo, posições e operações'}</p></div><Link className="button" to="/operacoes">Nova operação</Link></header><SummaryByCurrency summary={summary}/><PremiumValuationComparison summary={summary}/><Distribution positions={positions}/><div className="panel portfolio-positions-panel"><div className="panel-heading"><h3>Posições</h3></div>{positions.length ? <PositionsTable rows={positions}/> : <EmptyState title="Nenhuma posição" description="Registre uma compra para visualizar a posição." action={<Link className="button primary" to="/operacoes">Registrar operação</Link>}/>}</div><div className="panel"><div className="panel-heading"><h3>Operações recentes</h3><Link to="/operacoes">Nova operação →</Link></div>{operations.length ? <div className="table-scroll"><table><thead><tr><th>Data</th><th>Ativo</th><th>Tipo</th><th>Quantidade</th><th>Preço executado</th></tr></thead><tbody>{operations.map(operation => <tr key={operation.id}><td>{formatDate(operation.dataOperacao)}</td><td className="ticker">{operation.ticker}</td><td><StatusBadge label={operation.tipo} tone={operation.tipo === 'COMPRA' ? 'success' : 'danger'}/></td><td>{formatNumber(operation.quantidade)}</td><td>{formatCurrency(operation.precoUnitario, operation.moeda)}</td></tr>)}</tbody></table></div> : <EmptyState title="Carteira sem operações" description="As compras e vendas registradas aparecerão aqui."/>}</div></div>;
}

function PortfolioQuoteRefresh({ refreshing, message, error, onRefresh }: { refreshing: boolean; message: string; error: boolean; onRefresh: () => void }) {
  return <div className="page portfolio-detail-page"><div className="stock-row-actions"><button className="button" type="button" disabled={refreshing} onClick={onRefresh}><RefreshCw className={refreshing ? 'spin' : ''} size={16}/>{refreshing ? 'Atualizando cotações…' : 'Atualizar cotações'}</button></div>{message && (error ? <ErrorAlert message={message}/> : <p className="muted" role="status">{message}</p>)}</div>;
}

export function PortfolioDetailPage() {
  const { user } = useAuth();
  const { id } = useParams();
  const walletId = Number(id);
  const [wallet, setWallet] = useState<Carteira>();
  const [summary, setSummary] = useState<CarteiraResumo>();
  const [positions, setPositions] = useState<Posicao[]>([]);
  const [operations, setOperations] = useState<Operacao[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);
  const [refreshMessage, setRefreshMessage] = useState('');
  const [refreshError, setRefreshError] = useState(false);

  useEffect(() => {
    let active = true;
    setWallet(undefined); setSummary(undefined); setPositions([]); setOperations([]); setError(''); setLoading(true);
    if (!Number.isSafeInteger(walletId) || walletId <= 0) { setError('Identificador de carteira inválido.'); setLoading(false); return () => { active = false; }; }
    Promise.all([carteirasApi.get(walletId), carteirasApi.summary(walletId), carteirasApi.positions(walletId), carteirasApi.operations(walletId)])
      .then(([loadedWallet, loadedSummary, loadedPositions, loadedOperations]) => { if (active) { setWallet(loadedWallet); setSummary(loadedSummary); setPositions(loadedPositions); setOperations(loadedOperations); } })
      .catch(apiError => { if (active) setError(apiError.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [walletId, user?.id]);

  const refreshQuotes = async () => {
    if (refreshing) return;
    setRefreshing(true); setRefreshMessage(''); setRefreshError(false);
    try {
      const result = await carteirasApi.refreshQuotes(walletId);
      if (result.quantidadeAtualizada > 0) {
        const [loadedSummary, loadedPositions] = await Promise.all([carteirasApi.summary(walletId), carteirasApi.positions(walletId)]);
        setSummary(loadedSummary); setPositions(loadedPositions);
        setRefreshMessage(result.quantidadeComFalha > 0 ? `Cotações atualizadas parcialmente. Falharam: ${result.tickersComFalha.join(', ')}.` : 'Cotações atualizadas com sucesso.');
      } else if (result.quantidadeComFalha > 0) {
        setRefreshError(true); setRefreshMessage('Não foi possível atualizar as cotações agora. Tente novamente mais tarde.');
      } else setRefreshMessage('Não há posições abertas para atualizar.');
    } catch (apiError) {
      setRefreshError(true); setRefreshMessage(apiError instanceof Error ? apiError.message : 'Não foi possível atualizar as cotações.');
    } finally { setRefreshing(false); }
  };

  if (loading) return <LoadingState/>;
  if (error) return <div className="page"><Link to="/carteiras" className="back-link">← Carteiras</Link><ErrorAlert message={error}/></div>;
  return <><PortfolioQuoteRefresh refreshing={refreshing} message={refreshMessage} error={refreshError} onRefresh={() => void refreshQuotes()}/><PortfolioDetailPremium wallet={wallet} summary={summary} positions={positions} operations={operations}/></>;
}
