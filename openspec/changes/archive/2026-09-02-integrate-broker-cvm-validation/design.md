## Context

O fluxo de cadastro atual já é orquestrado por `CorretoraService`: valida CNPJ, evita duplicidade, consulta dados cadastrais pela BrasilAPI e valida/enriquece CEP pelo ViaCEP antes de persistir. A tabela existente já contém `validadaNaCvm`; esta mudança não requer schema, migration ou alteração JPA. Veja `proposal.md` e as delta specs para o comportamento contratado.

## Goals / Non-Goals

**Goals:**

- Inserir a consulta CVM após CNPJ e CEP, antes de qualquer persistência.
- Manter a regra de negócio independente de HTTP, ZIP, CSV e dos nomes de colunas do dataset.
- Reutilizar um snapshot em memória indexado por CNPJ normalizado, com atualização configurável, para evitar processar o arquivo a cada `POST`.
- Converter resultados negativos e falhas externas nos erros de aplicação contratados.

**Non-Goals:**

- Consultar uma API REST inexistente por CNPJ, alterar BrasilAPI/ViaCEP, validar CVM de categorias fora de Corretora e Distribuidora, criar cache distribuído, retry/circuit breaker, ou armazenar o dataset no banco.
- Criar migration, modificar a entidade ou implementar Ações, carteira e demais módulos fora de Corretora.

## Decisions

### Dataset oficial diário, não endpoint presumido

O adapter consumirá o recurso oficial `cad_intermed.zip` do conjunto **Participantes Intermediários: Informação Cadastral** da CVM. O portal descreve o recurso como ZIP de atualização diária e informa que ele contém `cad_intermed.csv` para o cadastro básico; este é o único arquivo usado para a decisão. A fonte possui também dicionário de dados, que será tratado como referência para validar cabeçalhos no adapter.

Alternativa considerada: chamar a consulta pública interativa da CVM. Ela não é um contrato de integração por CNPJ adequado e pode usar mecanismos de interação humana; foi descartada.

### Port interno e modelo de decisão

`CorretoraService` dependerá de um `CvmParticipantProvider`, que recebe CNPJ normalizado e devolve dados internos suficientes para a decisão ou exceções da aplicação. `CvmParticipantAdapter` concentrará HTTP, ZIP, CSV, cabeçalhos, codificação e índices. DTOs ou linhas CSV da CVM não atravessarão a fronteira do port.

Alternativa considerada: expor o CSV ao service. Foi descartada para preservar a arquitetura de providers já usada por CNPJ e CEP.

### Critério de aceitação estrito

O adapter identificará o registro pelo CNPJ normalizado e o service aceitará somente situação ativa e categoria de Corretora ou Distribuidora de títulos e valores mobiliários. A comparação de categoria tratará variações de apresentação do dataset (caixa, acentos e qualificadores), sem ampliar as famílias aceitas. Todo outro tipo listado pela CVM, inclusive bancos, é rejeitado. Registro ausente, inativo, categoria ausente ou incompatível produz rejeição de negócio.

Alternativa considerada: aceitar qualquer participante intermediário ativo. Foi descartada porque o cadastro do projeto representa Corretoras e a presença em outra categoria não prova a compatibilidade requerida.

### Snapshot local com atualização preguiçosa e configurável

O adapter guardará um snapshot imutável indexado por CNPJ e a data/hora de carga. Na primeira consulta, ou depois de expirar a duração configurável (padrão diário), uma única atualização sincronizada baixa e interpreta o ZIP antes de publicar o novo snapshot; consultas dentro da janela somente consultam o índice. Em caso de falha na atualização exigida, a solicitação falha controladamente; não será usado snapshot vencido como fallback silencioso.

Alternativas consideradas: download por requisição e scheduler periódico. O primeiro é ineficiente; o segundo adiciona ciclo de vida e comportamento de inicialização desnecessários para este escopo.

### Falhas externas e contrato HTTP

Instituição não aceita será uma exceção de negócio convertida em 422. ZIP/CSV ou cabeçalhos incompatíveis será exceção de resposta externa convertida em 502. Timeout, conexão, indisponibilidade e rate limit serão exceções de indisponibilidade convertidas em 503. O handler central continuará devolvendo o contrato de erro existente, sem corpo recebido da CVM, mensagens do client ou stack trace.

## Risks / Trade-offs

- [O layout do dataset diário pode mudar] → validar explicitamente arquivo e cabeçalhos necessários; conteúdo inesperado falha como 502, sem aprovação por suposição.
- [Uma atualização pode falhar após a expiração] → responder 503 de modo explícito e não aceitar dado vencido silenciosamente.
- [Snapshot somente local por instância] → suficiente para o projeto acadêmico; instâncias diferentes podem atualizar em momentos distintos.
- [Categorias podem ter variações textuais] → normalizar texto apenas para reconhecer as duas famílias aprovadas e cobrir fixtures representativas.

## Migration Plan

1. Adicionar configuração sem credenciais para URL, timeouts e intervalo de atualização da fonte CVM.
2. Implantar o adapter e a orquestração antes da persistência; não há migration ou dado existente a transformar.
3. Para rollback, remover a versão da aplicação; o banco permanece compatível porque não haverá alteração estrutural.
