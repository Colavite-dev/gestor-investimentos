export type OperationPricingState = {
  assetId?: number;
  requestId: number;
  marketQuote?: number;
  unitPrice: string;
  manuallyEdited: boolean;
  loading: boolean;
  error: string;
  notice: string;
};

export const emptyOperationPricingState: OperationPricingState = {
  requestId: 0,
  unitPrice: '',
  manuallyEdited: false,
  loading: false,
  error: '',
  notice: ''
};

function validQuote(value: number | undefined): value is number {
  return value != null && Number.isFinite(value) && value > 0;
}

export function beginOperationPricing(assetId: number, requestId: number, knownQuote: number | undefined, loading: boolean): OperationPricingState {
  const quote = validQuote(knownQuote) ? knownQuote : undefined;
  return {
    assetId,
    requestId,
    marketQuote: quote,
    unitPrice: quote == null ? '' : String(quote),
    manuallyEdited: false,
    loading,
    error: '',
    notice: ''
  };
}

export function editOperationPrice(state: OperationPricingState, unitPrice: string): OperationPricingState {
  return { ...state, unitPrice, manuallyEdited: true };
}

export function applyOperationQuote(state: OperationPricingState, assetId: number, requestId: number, quote: number): OperationPricingState {
  if (state.assetId !== assetId || state.requestId !== requestId || !validQuote(quote)) return state;
  return {
    ...state,
    marketQuote: quote,
    unitPrice: state.manuallyEdited ? state.unitPrice : String(quote),
    loading: false,
    error: '',
    notice: ''
  };
}

export function failOperationQuote(state: OperationPricingState, assetId: number, requestId: number, providerMessage?: string): OperationPricingState {
  if (state.assetId !== assetId || state.requestId !== requestId) return state;
  if (validQuote(state.marketQuote)) {
    return {
      ...state,
      loading: false,
      error: '',
      notice: 'Não foi possível atualizar a cotação agora. Usando a última cotação disponível.'
    };
  }
  return {
    ...state,
    loading: false,
    error: `${providerMessage || 'Não foi possível obter a cotação.'} Informe um preço manualmente.`,
    notice: ''
  };
}

export function calculateOperationTotal(quantity: string, unitPrice: string): number {
  return Math.max(0, Number(quantity) || 0) * Math.max(0, Number(unitPrice) || 0);
}
