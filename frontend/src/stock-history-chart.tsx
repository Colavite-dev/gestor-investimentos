import { useId } from 'react';
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { CHART_COLORS } from './chart-theme';
import type { CotacaoHistorica, Moeda } from './types/api';
import { formatCurrency } from './utils/format';
import { calculatePriceDomain, formatHistoryAxisDate, formatHistoryTooltipDate, formatPriceAxis, hasSameMinuteCollision } from './stock-history-model';

type TooltipPayload = {
  payload?: CotacaoHistorica;
};

type HistoryTooltipProps = {
  active?: boolean;
  payload?: readonly TooltipPayload[];
  currency: Moeda;
};

type HistoryPointProps = {
  cx?: number;
  cy?: number;
  index?: number;
  total: number;
};

function prefersReducedMotion() {
  return typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

function HistoryTooltip({ active, payload, currency }: HistoryTooltipProps) {
  const item = payload?.[0]?.payload;
  if (!active || !item) return null;

  return <div className="history-tooltip" role="status">
    <span>{formatHistoryTooltipDate(item.dataHoraCotacao)}</span>
    <strong><i aria-hidden="true" />{formatCurrency(item.cotacao, currency)}</strong>
  </div>;
}

function HistoryPoint({ cx, cy, index, total }: HistoryPointProps) {
  if (cx == null || cy == null || index == null) return null;
  const latest = index === total - 1;
  return <circle
    cx={cx}
    cy={cy}
    r={latest ? 5.5 : 3}
    className={latest ? 'history-point history-point-latest' : 'history-point'}
    aria-hidden="true"
  />;
}

export function StockHistoryChart({ history, currency }: { history: readonly CotacaoHistorica[]; currency: Moeda }) {
  const gradientId = `history-gradient-${useId().replace(/:/g, '')}`;
  const domain = calculatePriceDomain(history);
  const firstDay = new Date(history[0].dataHoraCotacao).toDateString();
  const lastDay = new Date(history[history.length - 1].dataHoraCotacao).toDateString();
  const sameDay = firstDay === lastDay;
  const sameMinuteCollision = hasSameMinuteCollision(history);
  const animate = !prefersReducedMotion();

  return <div className="history-chart" role="img" aria-label={`Histórico de ${history.length} cotações em ${currency}, em ordem cronológica`}>
    <ResponsiveContainer width="100%" height="100%">
      <AreaChart data={history} margin={{ top: 18, right: 20, left: 4, bottom: 4 }}>
        <defs>
          <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={CHART_COLORS.historyLine} stopOpacity={0.28} />
            <stop offset="48%" stopColor={CHART_COLORS.historyLine} stopOpacity={0.1} />
            <stop offset="100%" stopColor={CHART_COLORS.historyLine} stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid vertical={false} stroke={CHART_COLORS.grid} strokeDasharray="3 7" />
        <XAxis
          dataKey="dataHoraCotacao"
          axisLine={false}
          tickLine={false}
          minTickGap={34}
          tickMargin={12}
          tickFormatter={value => {
            const timestamp = String(value);
            return formatHistoryAxisDate(timestamp, sameDay, sameDay && sameMinuteCollision);
          }}
          tick={{ fill: 'var(--color-text-muted)', fontSize: 11 }}
        />
        <YAxis
          domain={domain}
          axisLine={false}
          tickLine={false}
          width={76}
          tickMargin={8}
          tickFormatter={value => formatPriceAxis(Number(value), currency)}
          tick={{ fill: 'var(--color-text-muted)', fontSize: 11 }}
        />
        <Tooltip
          content={<HistoryTooltip currency={currency} />}
          cursor={{ stroke: CHART_COLORS.historyCursor, strokeWidth: 1, strokeDasharray: '4 5' }}
          wrapperStyle={{ outline: 'none' }}
        />
        <Area
          type="monotone"
          dataKey="cotacao"
          stroke={CHART_COLORS.historyLine}
          strokeWidth={2.5}
          fill={`url(#${gradientId})`}
          dot={<HistoryPoint total={history.length} />}
          activeDot={{ r: 5, fill: CHART_COLORS.historyLine, stroke: 'var(--color-text-primary)', strokeWidth: 2 }}
          isAnimationActive={animate}
          animationDuration={520}
        />
      </AreaChart>
    </ResponsiveContainer>
  </div>;
}
