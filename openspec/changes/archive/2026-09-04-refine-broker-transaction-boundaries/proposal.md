## Why

O cadastro de corretora hoje abre uma transação antes de consultar BrasilAPI, ViaCEP e CVM, mantendo a unidade de banco ativa durante I/O externo potencialmente lento. Embora a constraint de CNPJ já impeça duplicidade física, o tratamento atual converte qualquer falha de integridade em conflito e o teste PostgreSQL existente não representa mais o fluxo validado pela CVM nem comprova a fronteira transacional real.

## What Changes

- Separar a orquestração externa do cadastro da persistência curta e atômica de `Corretora`, preservando a arquitetura Service → Port/Provider → Adapter.
- Manter as chamadas a BrasilAPI, ViaCEP e CVM antes da transação de escrita, construir a entidade somente depois das validações e persistir com rollback integral em caso de falha de banco.
- Preservar a verificação antecipada de CNPJ para evitar chamadas externas redundantes e tratar especificamente a violação de unicidade do CNPJ que ocorrer por corrida concorrente como `409 Conflict`, sem mascarar outras violações de integridade.
- Atualizar a cobertura unitária, de integração H2 e PostgreSQL opt-in para confirmar ausência de persistência em falhas externas, atomicidade de escrita, unicidade concorrente e comportamento real de Flyway/Hibernate/PostgreSQL.
- Modernizar `CorretoraPostgresIT`: eliminar as premissas anteriores à validação de CEP/CVM, evitar transação de teste que esconda o commit do serviço e manter todas as integrações externas mockadas.

## Capabilities

### New Capabilities

Nenhuma.

### Modified Capabilities

- `broker-management`: explicitar que colisões concorrentes do mesmo CNPJ deixam somente um cadastro persistido e respondem `409 Conflict` para a tentativa perdedora.

## Impact

- Código afetado: `CorretoraService`, novo componente de persistência transacional se confirmado pela implementação, `CorretoraRepository`, exceções/tradução de constraint e testes de corretora.
- API pública: nenhum endpoint ou payload de sucesso novo; a corrida de CNPJ duplicado passa a ter a mesma resposta `409` já definida para duplicidade comum, em vez de poder expor erro interno ou classificar outra violação incorretamente.
- Persistência: nenhuma migration planejada; a constraint existente `uk_corretoras_cnpj` continua sendo a proteção definitiva no banco.
- Infraestrutura de testes: PostgreSQL real via Docker permanece opt-in, sem internet e sem entrar no ciclo normal de `mvn verify`.
