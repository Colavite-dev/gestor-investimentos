## Context

O adapter já consome o dataset oficial diário `cad_intermed.zip`, lê `cad_intermed.csv`, normaliza CNPJs, agrupa todas as linhas por CNPJ e seleciona deterministicamente um representante elegível. O defeito está isolado em `CvmParticipantEligibility`: o predicado exige `ATIVO`, enquanto o domínio real do campo `SIT` usa `EM FUNCIONAMENTO NORMAL`, `CANCELADA` e `LIQUIDAÇÃO EXTRAJUDICIAL`.

O CNPJ `02.332.886/0001-04` possui registros de `CORRETORAS`, `CUSTODIANTES DE VALORES MOBILIÁRIOS` e `ESCRITURADORES DE VALORES MOBILIÁRIOS`, todos com situação `EM FUNCIONAMENTO NORMAL`. A linha de corretora deveria tornar o CNPJ elegível, mas é rejeitada pela comparação literal incorreta.

## Goals / Non-Goals

**Goals:**

- Alinhar a interpretação de situação ativa ao contrato real do campo `SIT`.
- Manter a elegibilidade restrita às famílias Corretora e Distribuidora.
- Rejeitar de forma segura situações canceladas, em liquidação ou desconhecidas.
- Preservar agregação, deduplicação por CNPJ e seleção determinística já existentes.
- Cobrir o caso real da XP sem introduzir regra específica por instituição.

**Non-Goals:**

- Alterar controller, service, endpoint, resposta HTTP, BrasilAPI, ViaCEP ou persistência de corretoras.
- Mudar autenticação, frontend visual, carteiras, operações, ações ou providers de cotação.
- Criar migration, alterar banco, adicionar provider ou redesenhar o cache do dataset.

## Decisions

### 1. Mapear explicitamente o valor operacional oficial

`CvmParticipantEligibility.isEligible` considerará situação ativa somente quando o valor normalizado de `SIT` for `EM FUNCIONAMENTO NORMAL`. `CANCELADA`, `LIQUIDAÇÃO EXTRAJUDICIAL`, o antigo valor artificial `ATIVO` e qualquer valor ausente ou desconhecido permanecerão inelegíveis.

Uma lista permissiva, inferência por palavras ou fallback foi rejeitada porque poderia aceitar uma situação futura cujo significado ainda não foi analisado. O comportamento continuará fail-closed.

### 2. Preservar integralmente o critério de categoria

O predicado continuará exigindo categoria normalizada pertencente às famílias `CORRETORA` ou `DISTRIBUIDORA`. Bancos, custodiantes, depositários, cooperativas, entidades administradoras e escrituradores continuarão inelegíveis quando não houver outra linha compatível para o mesmo CNPJ.

Não haverá whitelist de CNPJ, nome empresarial ou instituição. A XP será apenas um fixture de regressão que exercita a regra genérica.

### 3. Reutilizar a agregação existente para múltiplos registros

`CvmParticipantAdapter` continuará agrupando todas as linhas pelo CNPJ normalizado e usando o mesmo predicado compartilhado para priorizar um registro elegível. Nenhuma mudança estrutural no provider ou no modelo de retorno é necessária.

O teste de regressão representará as três categorias observadas para a XP e verificará que a existência de `CORRETORAS + EM FUNCIONAMENTO NORMAL` prevalece sobre as linhas incompatíveis, independentemente da ordem.

### 4. Alinhar testes ao contrato real sem tornar a suíte dependente da internet

Fixtures e mocks que simulam o campo `SIT` serão atualizados de `ATIVO` para `EM FUNCIONAMENTO NORMAL`. Um teste unitário cobrirá a matriz de situação e categoria; o teste do adapter cobrirá múltiplos registros da XP.

O `CvmParticipantRealIT` continuará opt-in, mas passará a afirmar que o participante retornado para a XP é elegível, em vez de verificar apenas presença. A suíte Maven normal continuará usando doubles e não dependerá da rede.

## Risks / Trade-offs

- [A CVM introduzir um novo valor operacional] → rejeitar por padrão e exigir análise explícita antes de ampliar a lista aceita.
- [Fixtures antigas mascararem novamente divergência de vocabulário] → usar headers e valores reais do `cad_intermed.csv` nos testes relacionados.
- [Regressão na seleção entre múltiplas categorias] → testar permutações com uma linha elegível e linhas incompatíveis do mesmo CNPJ.
- [Instabilidade do serviço oficial] → manter o teste real fora do ciclo Maven padrão e conservar os testes normais determinísticos.

## Migration Plan

1. Alterar o predicado compartilhado de elegibilidade.
2. Atualizar fixtures e executar testes focados offline.
3. Executar o teste CVM real opt-in quando houver acesso de rede.
4. Executar a suíte Maven completa e validar a change em modo strict.

Não há migration de schema ou dados. O rollback é exclusivamente de código; após implantação, o snapshot existente pode ser reutilizado porque os valores brutos de situação e categoria já estão armazenados em memória e serão avaliados pelo predicado corrigido.
