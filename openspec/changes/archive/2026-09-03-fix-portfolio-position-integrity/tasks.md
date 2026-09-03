# Tasks

- [x] 1.1 Estender `OperacaoRepository` e `CarteiraRepository` com as consultas ordenadas e o bloqueio necessários para serializar o registro e avaliar o histórico de cada par carteira+ação.
- [x] 1.2 Implementar em `OperacaoService` a validação transacional da sequência completa, incluindo a operação candidata, antes de qualquer persistência.
- [x] 1.3 Criar o erro de negócio de saldo insuficiente para venda e mapeá-lo no `GlobalExceptionHandler` para resposta `422` padronizada.
- [x] 1.4 Ajustar `CarteiraPosicaoService` para projetar somente operações válidas, sem truncar vendas, e não retornar ativos com saldo final zero.
- [x] 1.5 Cobrir em `OperacaoServiceTest` compra 10/venda 5, compra 10/venda 10, venda 11, venda sem compra, venda retroativa inválida, operação retroativa válida, mesmo timestamp e ausência de persistência na rejeição.
- [x] 1.6 Cobrir em `CarteiraPosicaoServiceTest` saldo zerado ausente das posições e várias compras e vendas intercaladas com quantidade, custo e preço médio corretos; incluir cobertura de repositório/integração para a ordenação e bloqueio adotados quando necessária.
- [x] 1.7 Executar testes focados, `mvn verify` e validar a change em modo strict; registrar evidências reais e confirmar ausência de migration/schema change.
