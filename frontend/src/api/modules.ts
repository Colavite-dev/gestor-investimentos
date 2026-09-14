import { apiClient } from './client';
import type { Acao, AcaoSuggestion, Carteira, CarteiraQuoteRefresh, CarteiraResumo, Corretora, CotacaoHistorica, MarketAssetPage, Mercado, Operacao, Posicao } from '../types/api';

export const corretorasApi = { list: () => apiClient.get<Corretora[]>('/corretoras'), create: (body: { cnpj: string }) => apiClient.post<Corretora>('/corretoras', body) };
export const acoesApi = {
  list: () => apiClient.get<Acao[]>('/acoes'), get: (id: number) => apiClient.get<Acao>(`/acoes/${id}`),
  create: (body: unknown) => apiClient.post<Acao>('/acoes', body), search: (q: string) => apiClient.get<AcaoSuggestion[]>(`/acoes/pesquisar?q=${encodeURIComponent(q)}`),
  catalog: ({ mercado, q = '', page = 0, size = 20 }: { mercado: Mercado; q?: string; page?: number; size?: number }) => {
    const params = new URLSearchParams({ mercado, q, page: String(page), size: String(size) });
    return apiClient.get<MarketAssetPage>(`/acoes/catalogo?${params.toString()}`);
  },
  resolve: (body: { ticker: string; mercado: string }) => apiClient.post<Acao>('/acoes/resolver', body),
  updateQuote: (id: number) => apiClient.put<Acao>(`/acoes/${id}/atualizar-cotacao`), history: (id: number) => apiClient.get<CotacaoHistorica[]>(`/acoes/${id}/historico-cotacoes`)
};
export const carteirasApi = { list: () => apiClient.get<Carteira[]>('/carteiras'), get: (id: number) => apiClient.get<Carteira>(`/carteiras/${id}`), create: (body: unknown) => apiClient.post<Carteira>('/carteiras', body), operations: (id: number) => apiClient.get<Operacao[]>(`/carteiras/${id}/operacoes`), positions: (id: number) => apiClient.get<Posicao[]>(`/carteiras/${id}/posicoes`), summary: (id: number) => apiClient.get<CarteiraResumo>(`/carteiras/${id}/resumo`), refreshQuotes: (id: number) => apiClient.put<CarteiraQuoteRefresh>(`/carteiras/${id}/atualizar-cotacoes`) };
export const operacoesApi = { get: (id: number) => apiClient.get<Operacao>(`/operacoes/${id}`), create: (body: unknown) => apiClient.post<Operacao>('/operacoes', body), listByCarteira: (id: number) => carteirasApi.operations(id) };
