## 1. Contratos e configuração da integração CVM

- [x] 1.1 Criar o modelo interno e o `CvmParticipantProvider` para consultar participante por CNPJ normalizado, verificando por testes unitários que DTOs ZIP/CSV não atravessam o contrato da aplicação.
- [x] 1.2 Adicionar propriedades configuráveis, sem credenciais, para URL do dataset oficial, timeouts e janela de atualização, verificando o binding no contexto de teste.
- [x] 1.3 Criar exceções internas para participante não aceito, conteúdo CVM incompatível e indisponibilidade, verificando suas classificações em testes unitários.

## 2. Adapter do dataset oficial CVM

- [x] 2.1 Implementar o adapter que baixa o ZIP oficial e interpreta somente `cad_intermed.csv`, validando arquivo e cabeçalhos obrigatórios com fixtures ZIP/CSV válidas e incompatíveis.
- [x] 2.2 Indexar o snapshot processado pelo CNPJ de 14 dígitos e verificar em testes a normalização da chave e a localização do registro correspondente.
- [x] 2.3 Implementar o critério de aceitação de registro ativo nas famílias Corretora e Distribuidora de títulos e valores mobiliários, verificando participante aceito, inativo, categoria incompatível e categoria ausente.
- [x] 2.4 Implementar a atualização preguiçosa do snapshot com reutilização dentro da janela configurável, verificando por teste que consultas sucessivas não provocam novo download e que a expiração atualiza os dados.
- [x] 2.5 Converter timeout, conexão, indisponibilidade, rate limit e conteúdo externo inválido nas exceções internas corretas, verificando cada conversão em testes isolados do adapter.

## 3. Orquestração do cadastro e contrato HTTP

- [x] 3.1 Integrar o `CvmParticipantProvider` ao `CorretoraService` após BrasilAPI e ViaCEP e antes do repositório, verificando por teste de service a sequência, a persistência somente após aceite e `validadaNaCvm=true`.
- [x] 3.2 Preservar a verificação de duplicidade antes de todos os providers, verificando que CNPJ duplicado não consulta BrasilAPI, ViaCEP nem CVM e não persiste novo registro.
- [x] 3.3 Converter participante ausente, inativo ou incompatível para `422`, conteúdo CVM incompatível para `502` e indisponibilidade técnica para `503`, verificando o contrato JSON centralizado em testes de controller.
- [x] 3.4 Cobrir por teste de regressão o fluxo completo BrasilAPI → ViaCEP → CVM → persistência, incluindo rejeição CVM sem persistência e sem alteração do comportamento dos providers anteriores.

## 4. Documentação e validação consolidada

- [x] 4.1 Atualizar a documentação técnica necessária com a fonte oficial, ausência de credenciais, critério de aceite, janela de atualização e comportamento de indisponibilidade, verificando a consistência com as specs.
- [x] 4.2 Criar, se tecnicamente adequado, teste opt-in e excluído da suíte normal para consultar uma vez o dataset público atual da CVM, verificando que não há dependência de internet nos testes regulares.
- [x] 4.3 Executar `mvn verify` e `openspec validate integrate-broker-cvm-validation --strict` após os testes automatizados, registrar ambos os resultados e reconciliar esta lista conforme evidências reais; não executar validação PostgreSQL/Docker se não houver alteração de schema ou JPA.
