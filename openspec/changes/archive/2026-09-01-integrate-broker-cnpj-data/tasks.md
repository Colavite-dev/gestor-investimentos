## 1. Lote 1 — Contrato interno e configuração

- [x] 1.1 Criar `CnpjDataProvider` e `CnpjRegistrationData`, independentes da BrasilAPI, e verificar por compilação que os contratos internos não dependem do DTO externo.
- [x] 1.2 Criar as exceções de CNPJ não encontrado, resposta inválida e provider indisponível, e verificar por testes do handler os status públicos 422, 502 e 503 sem detalhes externos.
- [x] 1.3 Configurar um `RestClient` dedicado com base URL, connect timeout e read timeout externalizáveis, sem nova dependência, e verificar o binding das propriedades em teste de contexto.
  - Evidência do lote: compilação concluída e 4 testes focados passaram, cobrindo propriedades, contexto e contrato de erros sem chamadas externas.

## 2. Lote 2 — Adapter BrasilAPI

- [x] 2.1 Criar `BrasilApiCnpjResponse` somente com os campos oficiais usados pelo domínio e verificar por teste o binding `snake_case` conforme a documentação oficial.
- [x] 2.2 Implementar `BrasilApiCnpjAdapter` para `GET /api/cnpj/v1/{cnpj}`, mapeando e normalizando a resposta para `CnpjRegistrationData`, e verificar por teste HTTP simulado o caminho e os dados resultantes.
- [x] 2.3 Validar CNPJ de resposta, campos obrigatórios, opcionais, tamanhos e formatos sem truncar dados, e verificar por testes respostas válidas, opcionais vazios e respostas incompatíveis.
- [x] 2.4 Classificar 404, corpo ilegível, timeout, falha de conexão, 429 e demais erros HTTP sem retry, e verificar por testes isolados as exceções internas correspondentes.
  - Evidência do lote: 12 testes do adapter passaram com servidor simulado/local, cobrindo binding, mapeamento, validações e todas as classes de falha sem acesso à BrasilAPI real.

## 3. Lote 3 — Cadastro e contrato REST

- [x] 3.1 Reduzir `CorretoraRequest` a CNPJ e adaptar `CorretoraMapper` para os dados cadastrais internos, verificando por testes que todos os campos persistidos vêm do provider e `validadaNaCvm=false` é preservado.
- [x] 3.2 Integrar `CorretoraService` ao port na ordem validar/normalizar, verificar duplicidade, consultar e persistir, verificando por testes sucesso, falhas e ausência de consulta externa em duplicidade.
- [x] 3.3 Atualizar testes do controller e do PostgreSQL opt-in para usar provider substituído, verificando o novo payload, propriedades desconhecidas, 201, 400, 409, 422, 502 e 503 sem rede na suíte normal.
- [x] 3.4 Tornar propriedades desconhecidas inválidas nos DTOs HTTP internos, mantendo o DTO BrasilAPI tolerante a campos externos adicionais, e verificar os dois comportamentos por testes.
- [x] 3.5 Atualizar `README.md` com o novo cadastro por CNPJ, origem de cada campo, configuração/timeouts e erros externos, deixando explícitos os escopos futuros de CEP e CVM.
  - Evidência do lote: 18 testes focados passaram (10 controller, 7 service e 1 mapper), e o README documenta o contrato, configuração e limites de escopo.

## 4. Lote 4 — Validação consolidada

- [x] 4.1 Executar a suíte automatizada completa com H2/Flyway e verificar que nenhum teste normal acessa a BrasilAPI real.
  - Evidência: `mvn test` concluiu com 40 testes, 0 falhas e 0 erros; os testes `*IT` ficaram excluídos e o provider foi simulado nos testes normais.
- [x] 4.2 Executar `mvnw.cmd verify` e registrar `BUILD SUCCESS`, quantidade de testes e geração do JAR.
  - Evidência: Maven 3.9.16 concluiu `verify` com `BUILD SUCCESS`, 40 testes aprovados e JAR executável gerado.
- [x] 4.3 Executar uma única validação real controlada do adapter contra a BrasilAPI, registrar apenas status e campos mapeados não sensíveis e manter o teste opt-in fora da suíte normal.
  - Evidência: `BrasilApiCnpjRealIT` executou uma vez e passou; o CNPJ público `19131243000197` retornou razão social não vazia, UF `SP`, situação `ATIVA` e campos obrigatórios compatíveis.
- [x] 4.4 Avaliar a necessidade de PostgreSQL consolidado; executar uma única validação somente se houver alteração de schema/mapeamento ou dúvida concreta, caso contrário registrar tecnicamente por que H2 e a validação anterior são suficientes.
  - Evidência: não houve migration, alteração da entidade ou do mapeamento JPA; H2/Flyway validou a `V1` e os testes de persistência existentes, portanto uma nova execução PostgreSQL não acrescentaria evidência específica a esta mudança.
- [x] 4.5 Executar `openspec validate integrate-broker-cnpj-data --strict`, reconciliar os artefatos e manter a mudança ativa para revisão sem arquivá-la.
  - Evidência: após preservar os nomes dos scenarios estáveis nos blocos `MODIFIED`, o comando concluiu com `Change 'integrate-broker-cnpj-data' is valid`; a mudança permanece ativa.
