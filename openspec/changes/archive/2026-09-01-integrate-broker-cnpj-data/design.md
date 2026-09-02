## Context

Conforme `proposal.md`, o cadastro atual já valida CNPJ localmente, impede duplicidade e persiste todos os campos necessários, mas ainda confia em dados cadastrais enviados pelo cliente. A arquitetura e a DEC-007 exigem isolamento de APIs externas. A documentação oficial da BrasilAPI define `GET /api/cnpj/v1/{cnpj}`, respostas documentadas `200`, `400` e `404`, e os campos selecionados nesta mudança. Rate limit não possui quota oficial publicada, embora `429` possa ocorrer na infraestrutura.

O modelo persistente não muda e `validadaNaCvm` permanece falso. A nova integração atende apenas à origem cadastral por CNPJ; não valida autorização financeira e não substitui uma futura consulta específica de CEP.

## Goals / Non-Goals

**Goals:**

- Isolar o contrato da BrasilAPI atrás de um port interno substituível.
- Tornar o CNPJ a única entrada do cadastro e usar a fonte cadastral para os demais campos.
- Classificar falhas externas em respostas estáveis da aplicação.
- Manter testes normais determinísticos e sem rede.

**Non-Goals:**

- Implementar consulta de CEP, CVM, cache, retry, circuit breaker ou fallback com dados fictícios.
- Persistir o payload integral, quadro societário, CNAEs ou outros campos externos não usados por `Corretora`.
- Suportar nesta mudança o futuro CNPJ alfanumérico; o contrato local estável continua restrito aos 14 dígitos e ao algoritmo numérico já especificado.

## Decisions

### 1. Port e modelo interno independentes

O fluxo será `CorretoraService -> CnpjDataProvider -> BrasilApiCnpjAdapter -> BrasilAPI`. O port receberá o CNPJ normalizado e devolverá `CnpjRegistrationData`, contendo somente dados utilizados pelo domínio. `BrasilApiCnpjResponse` permanecerá no pacote do adapter e fará o binding dos nomes `snake_case` oficiais.

Isso permite trocar o provider e testar o service sem JSON ou HTTP. A alternativa de retornar o DTO BrasilAPI pelo port foi rejeitada por violar a DEC-007; criar uma camada de domínio mais ampla que a necessidade atual foi rejeitado como abstração especulativa.

### 2. Request contendo somente CNPJ

`CorretoraRequest` conterá apenas `cnpj`. Razão social, nome fantasia, e-mail, telefone, CEP e endereço, município, UF e situação cadastral virão da BrasilAPI. Nome fantasia, e-mail, primeiro telefone e complemento serão opcionais; valores vazios serão normalizados para `null`. Todos os demais campos serão obrigatórios no modelo interno porque a tabela já exige esses valores.

Se um campo obrigatório estiver ausente, exceder a capacidade do modelo ou o CNPJ retornado divergir do consultado, o adapter classificará a resposta como inválida. Dados oficiais não serão truncados e o usuário não poderá completar ou sobrescrever parcialmente a fonte nesta mudança.

### 3. Cliente HTTP síncrono e configuração

Será usado `RestClient`, disponível no Spring Web já presente, adequado ao fluxo síncrono MVC e sem nova dependência. Um bean configurado receberá base URL, connect timeout e read timeout por propriedades externas com defaults seguros para desenvolvimento. A URL padrão será `https://brasilapi.com.br`, sem credenciais, e o adapter chamará `/api/cnpj/v1/{cnpj}`.

A alternativa WebClient exigiria WebFlux sem benefício concreto; cliente JDK direto aumentaria código de serialização e tratamento. Retry automático não será usado para não multiplicar carga e rate limit.

### 4. Ordem de orquestração

Bean Validation rejeitará CNPJ inválido antes do service. O service normalizará o valor, consultará `existsByCnpj` e somente então chamará o port uma vez. Após o retorno válido, mapeará o modelo interno para `Corretora` e persistirá mantendo a constraint única como proteção concorrente e `validadaNaCvm=false`.

### 5. Classificação de erros

- BrasilAPI `404` será convertido em `CnpjNotFoundException` e `422 Unprocessable Entity`.
- Corpo ausente/ilegível, CNPJ divergente ou dados obrigatórios incompatíveis serão `InvalidCnpjResponseException` e `502 Bad Gateway`.
- Timeout, falha de conexão, `429`, demais `4xx` inesperados e `5xx` serão `CnpjProviderUnavailableException` e `503 Service Unavailable`.

As exceções poderão preservar a causa apenas para diagnóstico interno; o `ApiErrorResponse` terá mensagens da aplicação e nunca corpo, URL completa, headers, stack trace ou mensagem do provider.

### 6. Testes e validação real

Service e controller usarão mock do port. O adapter será testado com servidor HTTP simulado ligado ao `RestClient`, cobrindo binding/mapeamento e classes de erro. A suíte H2 continuará exercitando Flyway e persistência sem rede. Um teste `*IT` opt-in fará uma única consulta real controlada à BrasilAPI no final e ficará fora da execução normal.

Não será repetida validação PostgreSQL porque não há migration nem mudança de mapeamento; ela só será feita se surgir dúvida concreta ou falha incompatível com H2.

## Risks / Trade-offs

- [BrasilAPI pode estar indisponível ou limitar requisições sem quota pública] → timeouts curtos, uma chamada por cadastro, sem retry e erro 503 estável.
- [Dados oficiais podem estar vazios ou exceder limites existentes] → rejeitar como resposta inválida em vez de truncar ou completar com entrada não oficial.
- [Mudança quebra clientes que enviam o payload anterior] → documentar explicitamente o novo body somente com CNPJ e rejeitar propriedades desconhecidas.
- [CNPJ alfanumérico está previsto no contrato atual da BrasilAPI] → manter o escopo numérico exigido pela especificação vigente do projeto e tratar suporte alfanumérico em mudança própria quando o domínio local for revisado.

## Migration Plan

1. Introduzir port, modelo interno, configuração e adapter sem alterar o schema.
2. Alterar DTO, mapper, service e tratamento de erros.
3. Atualizar e ampliar testes sem chamadas reais na suíte normal.
4. Atualizar README e executar validações automatizadas.
5. Fazer uma chamada real controlada ao adapter e a validação OpenSpec strict.

Rollback de código restaura o request e o mapper anteriores. Nenhuma reversão de banco é necessária, pois não há migration ou alteração de dados existente prevista.
