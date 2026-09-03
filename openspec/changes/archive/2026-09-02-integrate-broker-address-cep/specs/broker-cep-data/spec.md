## Purpose

Definir a validação de CEP e o enriquecimento seguro do endereço cadastral de corretoras por uma fonte externa substituível, usando ViaCEP como provider inicial.

## ADDED Requirements

### Requirement: Consulta externa de CEP cadastral
Após receber dados cadastrais válidos da fonte de CNPJ, o sistema SHALL normalizar o CEP para oito dígitos e consultar uma fonte de CEP configurada antes de persistir a corretora. A consulta SHALL usar apenas o CEP fornecido pela fonte cadastral e não SHALL exigir CEP no contrato de criação.

#### Scenario: CEP cadastral válido
- **WHEN** a fonte cadastral retorna um CEP com oito dígitos e a fonte de CEP o localiza
- **THEN** o sistema usa a confirmação para prosseguir com o cadastro

#### Scenario: CEP cadastral em formato mascarado
- **WHEN** a fonte cadastral retorna um CEP na representação `12345-678`
- **THEN** o sistema consulta a fonte de CEP com `12345678` e persiste o CEP normalizado

#### Scenario: CEP cadastral inválido
- **WHEN** a fonte cadastral retorna CEP vazio, com caracteres incompatíveis ou diferente de oito dígitos após normalização
- **THEN** o sistema não consulta a fonte de CEP, não persiste a corretora e responde `502 Bad Gateway` com erro estável

### Requirement: Precedência e enriquecimento de endereço
O sistema SHALL manter CNPJ, dados empresariais, CEP e todo campo de endereço não vazio provenientes da fonte cadastral de CNPJ. A fonte de CEP SHALL preencher apenas logradouro, bairro, município e UF que estejam ausentes na fonte cadastral; ela MUST NOT inventar ou alterar o número do imóvel, nem substituir valores cadastrais não vazios somente por diferença de representação. Complemento SHALL continuar sendo exclusivamente cadastral.

#### Scenario: Campo de endereço ausente é enriquecido
- **WHEN** logradouro, bairro, município ou UF está ausente nos dados cadastrais e a fonte de CEP retorna o respectivo valor não vazio
- **THEN** o sistema persiste o valor retornado pela fonte de CEP para o campo ausente

#### Scenario: Dado cadastral não vazio é preservado
- **WHEN** a fonte cadastral e a fonte de CEP retornam valores não vazios diferentes para logradouro ou bairro
- **THEN** o sistema preserva o valor da fonte cadastral

#### Scenario: Número e complemento não são inventados
- **WHEN** a fonte de CEP retorna dados de endereço para uma corretora
- **THEN** o número e o complemento persistidos permanecem os recebidos da fonte cadastral

#### Scenario: Conflito relevante de município ou UF
- **WHEN** a fonte cadastral informa município ou UF não vazio que diverge do município ou UF retornado pela fonte de CEP para o mesmo CEP
- **THEN** o sistema não persiste a corretora e responde `502 Bad Gateway` com erro estável, sem expor dados dos providers

### Requirement: CEP inexistente e falhas externas controladas
O sistema SHALL distinguir CEP válido não encontrado de resposta inválida e de falhas técnicas da fonte de CEP. CEP não encontrado SHALL produzir `422 Unprocessable Entity`; resposta ilegível ou incompatível SHALL produzir `502 Bad Gateway`; timeout, indisponibilidade, falha de conexão, rate limit e demais erros HTTP externos SHALL produzir `503 Service Unavailable`. Nenhum caso SHALL persistir dados parciais, expor corpo, headers ou mensagens internas do provider, nem realizar retry automático.

#### Scenario: CEP não encontrado
- **WHEN** a fonte de CEP informa que um CEP de oito dígitos não existe
- **THEN** o sistema responde `422 Unprocessable Entity` sem persistir a corretora

#### Scenario: Resposta de CEP inválida
- **WHEN** a fonte retorna corpo ilegível, CEP divergente ou campos incompatíveis com o enriquecimento previsto
- **THEN** o sistema responde `502 Bad Gateway` sem persistir a corretora

#### Scenario: Falha transitória da fonte de CEP
- **WHEN** ocorre timeout, falha de conexão, rate limit, indisponibilidade ou outro erro HTTP externo não classificado como CEP inexistente
- **THEN** o sistema responde `503 Service Unavailable` sem persistir a corretora
