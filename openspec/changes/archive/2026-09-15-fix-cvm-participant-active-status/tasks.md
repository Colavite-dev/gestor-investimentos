## 1. Corrigir interpretação da situação CVM

- [x] 1.1 Alterar `CvmParticipantEligibility` para considerar somente `EM FUNCIONAMENTO NORMAL` como situação ativa e verificar que `CANCELADA`, `LIQUIDAÇÃO EXTRAJUDICIAL`, `ATIVO`, valores vazios e desconhecidos permanecem inelegíveis.

## 2. Alinhar testes ao contrato oficial

- [x] 2.1 Adicionar cobertura unitária da matriz situação/categoria e atualizar mocks ou fixtures relacionados que usam artificialmente `ATIVO` para representar o campo `SIT`, sem alterar cenários alheios à CVM.
- [x] 2.2 Adicionar ao `CvmParticipantAdapterTest` a regressão do CNPJ `02.332.886/0001-04` com registros de Corretora, Custodiante e Escriturador, verificando elegibilidade pela linha `CORRETORAS + EM FUNCIONAMENTO NORMAL` e independência da ordem.
- [x] 2.3 Fortalecer `CvmParticipantRealIT` para verificar que o registro selecionado para a XP é elegível, mantendo o teste opt-in e fora da suíte Maven normal.

## 3. Validar a correção

- [x] 3.1 Executar os testes focados de elegibilidade, adapter, service e controller de corretoras; executar separadamente o teste real opt-in quando houver rede e verificar que nenhum teste normal depende da internet.
- [x] 3.2 Executar a suíte backend completa com Java 17 por `./mvnw.cmd verify` e confirmar ausência de regressões, migrations ou alterações fora do escopo.
- [x] 3.3 Executar `openspec validate fix-cvm-participant-active-status --strict`, `openspec validate --all --strict` e revisão final de consistência entre implementação, testes e delta spec.
