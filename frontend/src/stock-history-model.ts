import type { CotacaoHistorica, Moeda } from './types/api';

export function sortHistoryChronologically(history: readonly CotacaoHistorica[]) {
  return [...history].sort((left, right) => (
    new Date(left.dataHoraCotacao).getTime() - new Date(right.dataHoraCotacao).getTime()
  ));
}

export function calculatePriceDomain(history: readonly CotacaoHistorica[]): [number, number] {
  if (!history.length) return [0, 1];

  const prices = history.map(item => Number(item.cotacao));
  const minPrice = Math.min(...prices);
  const maxPrice = Math.max(...prices);
  const range = maxPrice - minPrice;
  const reference = Math.max(Math.abs(minPrice), Math.abs(maxPrice), 1);
  const padding = Math.max(range * 0.1, reference * 0.015, 0.01);

  return [Math.max(0, minPrice - padding), maxPrice + padding];
}

export function formatHistoryAxisDate(value: string, sameDay: boolean, includeSeconds = false) {
  return new Intl.DateTimeFormat('pt-BR', sameDay
    ? { hour: '2-digit', minute: '2-digit', ...(includeSeconds ? { second: '2-digit' } : {}) }
    : { day: '2-digit', month: '2-digit' }).format(new Date(value));
}

export function hasSameMinuteCollision(history: readonly CotacaoHistorica[]) {
  const minutes = new Set<string>();
  return history.some(item => {
    const date = new Date(item.dataHoraCotacao);
    const key = `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}-${date.getHours()}-${date.getMinutes()}`;
    if (minutes.has(key)) return true;
    minutes.add(key);
    return false;
  });
}

export function formatHistoryTooltipDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  }).format(new Date(value));
}

export function formatPriceAxis(value: number, currency: Moeda) {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency,
    notation: Math.abs(value) >= 10_000 ? 'compact' : 'standard',
    maximumFractionDigits: Math.abs(value) < 100 ? 2 : 0,
  }).format(value);
}
