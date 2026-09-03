## 1. Lote 1 — Contratos e adapter ViaCEP

- [x] 1.1 Criar `CepDataProvider` e o modelo interno de dados de CEP, independentes do DTO ViaCEP, e verificar por compilação e teste unitário que a camada de negócio não depende do contrato externo.
- [x] 1.2 Criar propriedades e bean `RestClient` dedicados ao ViaCEP, com URL e timeouts configuráveis e sem credenciais, e verificar o binding em teste de contexto.
- [x] 1.3 Criar o DTO externo ViaCEP somente com os campos usados e verificar por teste de desserialização o binding de `cep`, `logradouro`, `bairro`, `localidade`, `uf` e `erro`.
- [x] 1.4 Implementar `ViaCepAdapter` para consultar `GET /ws/{cep}/json/`, normalizar o resultado para o modelo interno e verificar por servidor HTTP simulado o caminho, o método e o mapeamento.
- [x] 1.5 Implementar validação local de CEP com normalização para oito dígitos e verificar por testes formatos numérico, mascarado e inválidos.

## 2. Lote 2 — Falhas externas e reconciliação

- [x] 2.1 Criar exceções específicas de CEP não encontrado, resposta inválida e provider indisponível, e verificar pelo handler HTTP os status públicos 422, 502 e 503 sem detalhes externos.
- [x] 2.2 Classificar no adapter `erro: true`, corpo ilegível, CEP divergente, timeout, falha de conexão, rate limit e HTTP inesperado, e verificar cada classe em testes isolados sem acesso à internet.
- [x] 2.3 Implementar a reconciliação conservadora do endereço e verificar por testes o preenchimento de logradouro, bairro, município e UF ausentes.
- [x] 2.4 Preservar campos não vazios de CNPJ, número e complemento, e verificar por testes que diferenças de logradouro/bairro não sobrescrevem a origem cadastral.
- [x] 2.5 Rejeitar divergência não vazia de município ou UF como resposta externa incompatível e verificar por teste que não há persistência e o contrato retorna 502.

## 3. Lote 3 — Orquestração do cadastro e contrato REST

- [x] 3.1 Integrar `CepDataProvider` ao `CorretoraService` após CNPJ válido e duplicidade, normalizando/validando o CEP cadastral antes da persistência, e verificar o fluxo completo em teste de service.
- [x] 3.2 Preservar a otimização de duplicidade e verificar por teste que CNPJ já cadastrado não chama nem `CnpjDataProvider` nem `CepDataProvider`.
- [x] 3.3 Atualizar testes do controller e do handler para cobrir 201, 422, 502 e 503 no cadastro por CNPJ, mantendo o request sem campos de endereço e sem chamadas externas reais.
- [x] 3.4 Atualizar `README.md` com ViaCEP, configuração, precedência BrasilAPI/ViaCEP e falhas controladas, e verificar que não há credenciais ou dados sensíveis adicionados.

## 4. Lote 4 — Validação consolidada

- [x] 4.1 Executar os testes automatizados completos e verificar que a suíte normal usa mocks/servidores simulados e não consulta ViaCEP real.
- [x] 4.2 Executar `mvnw.cmd verify` e verificar `BUILD SUCCESS`, testes aprovados e geração do JAR executável.
- [x] 4.3 Executar uma única consulta real opt-in ao ViaCEP usando CEP público, registrar somente resultado não sensível e verificar que o teste permanece fora da suíte normal.
- [x] 4.4 Avaliar a necessidade de PostgreSQL real; executá-lo somente se houver alteração de schema/mapeamento ou dúvida concreta, ou registrar a justificativa técnica para não executá-lo.
- [x] 4.5 Executar `openspec validate integrate-broker-address-cep --strict`, reconciliar as tarefas com evidências reais e manter a mudança ativa para revisão sem arquivá-la.
