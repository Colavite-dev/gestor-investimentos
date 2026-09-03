## Context

O `CorretoraService` já orquestra validação local, duplicidade e `CnpjDataProvider`, recebendo `CnpjRegistrationData` independente da BrasilAPI. A fonte de CNPJ é a origem cadastral atual do endereço; a nova integração somente valida o CEP e completa lacunas permitidas. Consulte `proposal.md` e as delta specs para o comportamento observável.

## Goals / Non-Goals

**Goals:**

- Manter a consulta ViaCEP isolada por port e modelo interno, compatível com o padrão da integração CNPJ.
- Reconciliar endereço de forma pequena, determinística e sem alterar request, entidade ou schema.
- Converter categorias de falha externa em exceções internas e respostas HTTP estáveis.

**Non-Goals:**

- Não implementar pesquisa ViaCEP por endereço, cache, retry, circuit breaker, credenciais, CVM ou alteração de banco.
- Não substituir a BrasilAPI como fonte de dados empresariais nem corrigir automaticamente campos cadastrais não vazios.

## Decisions

### 1. Port, modelo interno e adapter ViaCEP

O fluxo será `CorretoraService -> CepDataProvider -> ViaCepAdapter -> ViaCEP`, em paralelo e independente de `CnpjDataProvider`. O port receberá CEP normalizado e devolverá um modelo interno somente com CEP, logradouro, bairro, município e UF. O DTO JSON ficará restrito ao adapter.

Isso preserva a substituibilidade e permite testar service sem HTTP. Acoplar o service ao DTO ViaCEP ou fazer o adapter BrasilAPI chamar ViaCEP violaria o isolamento atual.

### 2. Contrato e configuração HTTP

Será reutilizado `RestClient` com bean dedicado, URL base padrão `https://viacep.com.br` e timeouts configuráveis por propriedades. O adapter chamará `GET /ws/{cep}/json/`; a documentação oficial informa `400` para formato inválido e `erro: true` em JSON para CEP válido inexistente. Como o service valida o formato antes da chamada, uma resposta HTTP 400 externa será tratada como falha técnica do provider.

ViaCEP não declara necessidade de credenciais para esse endpoint; por isso não haverá variáveis de segredo. WebClient e dependências adicionais foram rejeitados por não trazerem benefício ao MVC síncrono existente.

### 3. Reconciliação conservadora

O CEP normalizado permanece o retornado pela fonte de CNPJ. Logradouro, bairro, município e UF são preenchidos pelo ViaCEP somente se o correspondente campo cadastral estiver ausente. Número e complemento nunca vêm de ViaCEP. Campos não vazios de CNPJ são preservados, exceto que divergência não vazia de município ou UF é bloqueante: o cadastro falha como dados externos incompatíveis (`502`) para evitar persistir uma combinação geográfica contraditória.

Rejeitar toda diferença de logradouro ou bairro seria excessivo, pois há diferenças legítimas de formatação. Sobrescrever município/UF contraditórios ocultaria uma inconsistência da fonte cadastral.

### 4. Ordem de orquestração e erros

Após validação/normalização do CNPJ e `existsByCnpj`, o service consulta somente o port CNPJ. Depois de validar `CnpjRegistrationData`, normaliza e valida seu CEP, consulta uma vez o port CEP, reconcilia, mapeia e persiste. Logo, duplicidade não chama nenhum provider.

CEP inexistente será exceção própria e retornará `422`; resposta ViaCEP inválida, CEP cadastral inválido ou conflito geográfico retornarão `502`; timeout, conexão, rate limit, indisponibilidade e HTTP externo inesperado retornarão `503`. As mensagens públicas serão definidas pela aplicação, sem dados internos do provider.

## Risks / Trade-offs

- [ViaCEP pode responder com campos de endereço vazios] → usar esses campos apenas para preenchimento quando não vazios; exigir CEP, município e UF válidos no modelo interno.
- [Diferenças de bases podem gerar município/UF divergentes] → falhar de maneira estável e não persistir endereço inconsistente.
- [Consulta adicional aumenta latência do cadastro] → timeouts configuráveis, uma consulta por cadastro não duplicado e nenhum retry.
- [ViaCEP alerta contra uso massivo] → não haverá validação de bases, cache ou chamadas em massa nesta capability.

## Migration Plan

1. Adicionar port, modelo, propriedades, adapter e exceções sem tocar no schema.
2. Integrar reconciliação no service e no handler HTTP.
3. Atualizar testes e README.
4. Executar testes normais, `mvn verify`, uma consulta opt-in real e strict validation.

O rollback é somente de código e configuração. Não haverá migration nem dados persistidos pela validação real controlada.
