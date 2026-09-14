export type Mercado='BRASIL'|'ESTADOS_UNIDOS'; export type Moeda='BRL'|'USD'; export type TipoOperacao='COMPRA'|'VENDA';
export interface Corretora{id:number;cnpj:string;razaoSocial:string;nomeFantasia:string;email?:string;telefone?:string;cep?:string;logradouro?:string;numero?:string;complemento?:string;bairro?:string;cidade?:string;uf?:string;situacaoCadastral:string;validadaNaCvm:boolean;dataCadastro:string}
export interface Acao{id:number;ticker:string;nomeEmpresa:string;mercado:Mercado;moeda:Moeda;cotacaoAtual:number;dataHoraCotacao:string}
export interface AcaoSuggestion{ticker:string;nomeEmpresa?:string;mercado:Mercado;moeda:Moeda}
export interface MarketAsset{ticker:string;nomeEmpresa:string;mercado:Mercado;moeda:Moeda;exchange?:string;micCode?:string;cotacaoAtual?:number;logoUrl?:string}
export interface MarketAssetPage{items:MarketAsset[];page:number;size:number;hasNext:boolean;totalElements?:number}
export interface Carteira{id:number;nome:string;descricao?:string;dataCadastro:string}
export interface Operacao{id:number;carteiraId:number;acaoId:number;ticker:string;moeda:Moeda;tipo:TipoOperacao;quantidade:number;precoUnitario:number;dataOperacao:string}
export interface Posicao{acaoId:number;ticker:string;mercado:Mercado;moeda:Moeda;quantidade:number;precoMedio:number;valorInvestido:number;cotacaoAtual?:number;patrimonioAtual?:number;lucroPrejuizo?:number;rentabilidadePercentual?:number}
export interface ResumoMoeda{quantidadePosicoes:number;valorInvestido:number;patrimonioAtual?:number;lucroPrejuizo?:number;rentabilidadePercentual?:number}; export interface CarteiraResumo{carteiraId:number;porMoeda:Partial<Record<Moeda,ResumoMoeda>>}; export interface CotacaoHistorica{id:number;cotacao:number;dataHoraCotacao:string;dataRegistro:string}; export interface ApiErrorPayload{status?:number;message?:string;fieldErrors?:Record<string,string>}
export interface CarteiraQuoteRefresh{quantidadeAtualizada:number;quantidadeComFalha:number;tickersComFalha:string[]}
