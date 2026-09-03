# Gestor de Investimentos

## Persistência local

A aplicação Spring Boot executa diretamente no host. Somente o PostgreSQL executa em Docker nesta etapa. PostgreSQL é o banco principal, Flyway controla a evolução do schema e Hibernate apenas valida o schema existente.

### Pré-requisitos

- Java 17;
- Docker Desktop com Docker Compose;
- Maven Wrapper incluído no projeto.

Confirme as ferramentas:

```powershell
java --version
docker --version
docker compose version
```

### Configuração

Crie a configuração local a partir do exemplo:

```powershell
Copy-Item .env.example .env
```

Preencha localmente as cinco variáveis de `.env`:

- `DB_HOST`: host usado pela aplicação; normalmente `localhost`;
- `DB_PORT`: porta publicada no host; o padrão do projeto é `5433`;
- `DB_NAME`: nome local do banco;
- `DB_USERNAME`: usuário local do banco;
- `DB_PASSWORD`: senha local obrigatória.

O arquivo `.env` é ignorado pelo Git e não deve ser compartilhado ou versionado. O projeto não adiciona biblioteca para carregar `.env`: o Docker Compose lê esse arquivo, enquanto a aplicação recebe as mesmas variáveis pelo shell ou pela configuração da IDE.

Por padrão, a aplicação acessa o banco em `localhost:5433`. O PostgreSQL continua ouvindo em `5432` dentro do contêiner; o Compose publica a porta como `5433:5432`. Essa separação evita conflito com uma instalação nativa do PostgreSQL que esteja usando `localhost:5432`.

Valide a configuração sem iniciar contêineres:

```powershell
docker compose --env-file .env config --quiet
```

O comando deve terminar com código de saída zero e sem mensagens de erro.

### Iniciar e verificar o PostgreSQL

Inicie somente o banco:

```powershell
docker compose --env-file .env up -d postgres
docker compose --env-file .env ps
```

O serviço `postgres` deve aparecer como `healthy` e publicar a porta do host como `5433->5432`. Confirme também a prontidão e a conexão com consultas somente leitura:

```powershell
docker compose --env-file .env exec -T postgres sh -c 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
docker compose --env-file .env exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT current_database(), current_schema();"'
```

O primeiro comando deve informar que o servidor aceita conexões. O segundo deve mostrar o banco configurado e o schema `public`.

### Validar a persistência do volume

A persistência deve ser verificada sem criar tabelas, executar DDL ou alterar o schema fora do Flyway. Compare somente o identificador persistente do cluster:

```powershell
$clusterIdBefore = docker compose --env-file .env exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "SELECT system_identifier FROM pg_control_system();"'

docker compose --env-file .env up -d --force-recreate postgres
docker compose --env-file .env ps

$clusterIdAfter = docker compose --env-file .env exec -T postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "SELECT system_identifier FROM pg_control_system();"'
$clusterIdBefore -eq $clusterIdAfter
```

Após o serviço voltar ao estado `healthy`, a comparação deve retornar `True`. Esse procedimento é somente leitura e não substitui migrations. Toda alteração estrutural futura deve ser feita por uma nova migration Flyway.

### Executar a aplicação no host

Exporte no PowerShell os mesmos valores usados no `.env`. A senha é solicitada sem ser registrada na documentação:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "5433"
$env:DB_NAME = "gestor_investimento"
$env:DB_USERNAME = "gestor_investimento"
$env:DB_PASSWORD = Read-Host "DB_PASSWORD"

.\mvnw.cmd spring-boot:run
```

Se valores diferentes foram escolhidos no `.env`, use os mesmos valores no shell. Com os padrões do projeto, a URL é `jdbc:postgresql://localhost:5433/gestor_investimento`. Uma porta sobrescrita em `DB_PORT` é usada tanto pelo Compose quanto pela URL JDBC, enquanto a porta interna do contêiner permanece `5432`.

Na inicialização bem-sucedida, os logs devem confirmar:

- conexão do datasource ao PostgreSQL;
- execução do `flywayInitializer` no datasource principal e schema `public`;
- ausência de migrations pendentes nesta etapa;
- inicialização do Hibernate com `ddl-auto=validate`, sem DDL de criação ou atualização.

Não existe migration técnica inicial. A primeira mudança que criar estrutura persistente deverá introduzir `V1__...sql`.

### Executar os testes

```powershell
.\mvnw.cmd test
```

`contextLoads` deve usar o perfil `test` e o H2 em memória, sem exigir PostgreSQL, Docker ou Testcontainers. H2 possui escopo Maven `test` e não está disponível no runtime normal.

## API de corretoras

Esta etapa disponibiliza o gerenciamento básico persistente de corretoras:

| Método | Endpoint | Resultado |
|---|---|---|
| `POST` | `/corretoras` | Cadastra uma corretora e responde `201 Created` |
| `GET` | `/corretoras` | Lista corretoras por `id` crescente |
| `GET` | `/corretoras/{id}` | Consulta uma corretora por ID |
| `GET` | `/corretoras/cnpj/{cnpj}` | Consulta por CNPJ com 14 dígitos |

### Cadastro por CNPJ

Exemplo de request:

```json
{
  "cnpj": "11.222.333/0001-81"
}
```

O CNPJ pode ser enviado com máscara ou com 14 dígitos no `POST`. A aplicação valida os dígitos verificadores, normaliza o valor, verifica duplicidade e consulta `GET /api/cnpj/v1/{cnpj}` da BrasilAPI. Propriedades adicionais no request são rejeitadas para impedir a sobrescrita dos dados cadastrais.

Os campos empresariais e o CEP vêm da resposta cadastral de CNPJ. O ViaCEP valida esse CEP e só completa `logradouro`, `bairro`, `cidade` e `uf` quando a fonte cadastral os retorna ausentes; nunca sobrescreve valores cadastrais não vazios, nem inventa número ou complemento. Divergência de cidade ou UF entre as fontes interrompe o cadastro para evitar persistência geograficamente inconsistente. CEP é armazenado somente com dígitos, UF em letras maiúsculas e campos opcionais vazios como `null`. `id`, `dataCadastro` e `validadaNaCvm` continuam controlados pelo sistema.

A resposta contém todos os campos persistidos, incluindo:

```json
{
  "id": 1,
  "cnpj": "11222333000181",
  "validadaNaCvm": true,
  "dataCadastro": "2026-09-01T18:00:00Z"
}
```

O JSON acima está abreviado; a resposta real também inclui os demais dados cadastrais e de endereço. O header `Location` do cadastro aponta para `/corretoras/{id}`.

### Erros

- `400 Bad Request`: CNPJ local inválido, propriedade desconhecida ou parâmetro incompatível;
- `404 Not Found`: corretora não encontrada por ID ou CNPJ;
- `409 Conflict`: CNPJ já cadastrado.
- `422 Unprocessable Entity`: CNPJ válido não encontrado pela fonte cadastral;
- `502 Bad Gateway`: resposta cadastral inválida ou incompatível;
- `503 Service Unavailable`: timeout, indisponibilidade, rate limit ou outro erro técnico da fonte.

Erros usam um contrato JSON comum com `timestamp`, `status`, `error`, `message`, `path` e `fieldErrors`.

### Configuração e limites da integração

O provider inicial usa os seguintes defaults, sobrescrevíveis por variáveis de ambiente:

| Variável | Default | Finalidade |
|---|---|---|
| `BRASIL_API_BASE_URL` | `https://brasilapi.com.br` | URL base do provider |
| `BRASIL_API_CONNECT_TIMEOUT` | `2s` | Limite para estabelecer conexão |
| `BRASIL_API_READ_TIMEOUT` | `5s` | Limite para receber a resposta |
| `VIA_CEP_BASE_URL` | `https://viacep.com.br` | URL base do provider de CEP |
| `VIA_CEP_CONNECT_TIMEOUT` | `2s` | Limite para estabelecer conexão com ViaCEP |
| `VIA_CEP_READ_TIMEOUT` | `5s` | Limite para receber a resposta do ViaCEP |
| `CVM_PARTICIPANTS_DATASET_URL` | `https://dados.cvm.gov.br/dados/INTERMED/CAD/DADOS/cad_intermed.zip` | ZIP diário oficial de Participantes Intermediários |
| `CVM_PARTICIPANTS_CONNECT_TIMEOUT` | `2s` | Limite para estabelecer conexão com a CVM |
| `CVM_PARTICIPANTS_READ_TIMEOUT` | `10s` | Limite para receber o dataset da CVM |
| `CVM_PARTICIPANTS_REFRESH_INTERVAL` | `24h` | Janela de reutilização do snapshot local da CVM |
| `BRAPI_TOKEN` | vazio | Token opcional da brapi, enviado somente ao provider brasileiro |
| `TWELVE_DATA_API_KEY` | vazio | Chave da Twelve Data, necessária somente para ações dos Estados Unidos |
| `TWELVE_DATA_BASE_URL` | `https://api.twelvedata.com` | URL base da Twelve Data |
| `TWELVE_DATA_CONNECT_TIMEOUT` | `2s` | Limite para estabelecer conexão com a Twelve Data |
| `TWELVE_DATA_READ_TIMEOUT` | `5s` | Limite para receber a resposta da Twelve Data |

Não há retry automático. BrasilAPI e ViaCEP não exigem credenciais para esses endpoints; nenhum token ou corpo de erro externo é exposto pela nossa API. CEP inexistente retorna `422`; resposta de CEP inválida ou conflito geográfico retorna `502`; indisponibilidade, timeout e erros técnicos retornam `503`.

Depois da BrasilAPI e do ViaCEP, o cadastro consulta o dataset diário **Participantes Intermediários: Informação Cadastral** da CVM. O CNPJ normalizado precisa estar em registro ativo e em categoria de Corretora ou Distribuidora de títulos e valores mobiliários. O ZIP é processado em um snapshot local indexado por CNPJ e reutilizado até a janela configurada; não há download por cadastro, nem credenciais. A CVM apenas completa essa validação: BrasilAPI continua sendo a fonte cadastral empresarial e ViaCEP continua sendo a validação/enriquecimento de endereço.

Somente o registro aceito pela CVM é persistido com `validadaNaCvm=true`. Participante ausente, inativo ou de categoria incompatível retorna `422`; ZIP/CSV incompatível retorna `502`; timeout, rate limit, conexão e indisponibilidade retornam `503`. Nenhuma resposta expõe conteúdo ou detalhes internos da CVM.

Os testes automatizados substituem os ports internos e não chamam BrasilAPI, ViaCEP ou CVM reais. A validação real da CVM é opt-in e pode ser executada de forma controlada com `-Dtest=CvmParticipantRealIT -DrunCvmRealIT=true test`.

## API de ações

Esta etapa cria o cadastro mestre de ações, sem representar compras, vendas, carteira ou vínculo com Corretora. A identidade do ativo é `(ticker, mercado)`; portanto, o mesmo ticker pode existir uma vez no Brasil e outra nos Estados Unidos.

`POST /acoes` recebe o ticker e o mercado para selecionar deterministicamente a fonte externa. Nenhum dado cadastral ou de cotação pode ser enviado manualmente:

```json
{
  "ticker": "PETR4",
  "mercado": "BRASIL"
}
```

Para Brasil, a aplicação consulta `GET /api/v2/stocks/quote` da brapi, persiste o ticker canônico quando houver renome e exige moeda `BRL`.

```json
{
  "ticker": "AAPL",
  "mercado": "ESTADOS_UNIDOS"
}
```

Para Estados Unidos, a aplicação consulta `/symbol_search` e `/quote` da Twelve Data. O instrumento precisa ter correspondência exata de ticker, `country` igual a `United States`, `instrument_type` igual a `Common Stock` e moeda `USD`; a cotação usa `symbol`, `name`, `currency`, `close` e `timestamp`. A fonte completa somente dados externos válidos: nome, moeda, cotação e data/hora não podem ser sobrescritos pelo cliente.

`BRAPI_TOKEN` é opcional e deve ficar apenas no ambiente local. Quando presente, é enviado como `Authorization: Bearer`. `TWELVE_DATA_API_KEY` também deve existir somente no ambiente local e é enviada exclusivamente como `Authorization: apikey`; operações brasileiras não dependem dessa chave. Nenhuma credencial deve ser registrada em código, documentação ou logs. Ticker ou instrumento incompatível retorna `422`; resposta externa incompatível retorna `502`; rate limit, timeout e indisponibilidade retornam `503`.

Os testes reais são opt-in e não pertencem à suíte normal: brapi com `-Dtest=BrapiStockRealIT -DrunBrapiRealIT=true test` e Twelve Data com `-Dtest=TwelveDataStockRealIT -DrunTwelveDataRealIT=true test`.

Os endpoints disponíveis são `POST /acoes`, `GET /acoes`, `GET /acoes/{id}`, `GET /acoes/ticker/{ticker}` e `PUT /acoes/{id}/atualizar-cotacao`. A busca por ticker aceita `?mercado=BRASIL` ou `?mercado=ESTADOS_UNIDOS`; sem o parâmetro, retorna o ativo somente quando o ticker não é ambíguo.

### Atualizar cotação

`PUT /acoes/{id}/atualizar-cotacao` não recebe corpo. O mercado persistido seleciona diretamente o provider: brapi para `BRASIL` e Twelve Data para `ESTADOS_UNIDOS`. A operação faz uma única consulta de cotação, atualiza exclusivamente `cotacaoAtual` e `dataHoraCotacao` e preserva ticker, mercado, nome da empresa e moeda.

```text
PUT /acoes/1/atualizar-cotacao
```

A resposta é `200 OK` com a representação atualizada da ação. Ação inexistente retorna `404`; ticker não consultável retorna `422`; dados externos incompatíveis retornam `502`; timeout, rate limit, indisponibilidade e configuração externa necessária retornam `503`. Para ação americana já cadastrada, a atualização consulta somente `/quote`, sem nova chamada a `/symbol_search`.

## Entregáveis acadêmicos

Os artefatos complementares da entrega estão disponíveis em:

- [Coleção Postman da API](docs/api/gestor-investimento.postman_collection.json): importe o arquivo no Postman e ajuste apenas a variável `baseUrl` para o ambiente local. A coleção não contém credenciais.
- [Diagrama simplificado de entidades](docs/architecture/entity-model.md): arquivo Markdown com diagrama Mermaid visualizável no GitHub ou em editor compatível.

A coleção reúne exemplos dos endpoints atuais de corretoras e ações, incluindo cadastro, consultas, atualização de cotação, requests inválidos, recursos inexistentes e duplicidade. Configure localmente quaisquer variáveis de ambiente exigidas pela aplicação conforme a seção de configuração; não copie senhas ou tokens para a coleção ou para o repositório.

### Parar o ambiente

Pare o contêiner preservando o volume `postgres_data`:

```powershell
docker compose --env-file .env down
```

O comando abaixo também remove o volume e apaga os dados locais. Use-o somente quando a exclusão for intencional:

```powershell
docker compose --env-file .env down -v
```

## HistÃ³rico de cotaÃ§Ãµes

O endpoint `GET /acoes/{id}/historico-cotacoes` consulta as observaÃ§Ãµes reais usadas no cadastro e nas atualizaÃ§Ãµes de cotaÃ§Ã£o. O resultado Ã© somente leitura e ordenado pelo timestamp da fonte e, em empate, pelo identificador.

```text
GET /acoes/1/historico-cotacoes?de=2026-09-01T00:00:00Z&ate=2026-09-30T23:59:59Z
```

Os parÃ¢metros `de` e `ate` sÃ£o opcionais e usam ISO-8601; intervalo invertido retorna `400`. AÃ§Ã£o inexistente retorna `404`. O histÃ³rico nÃ£o consulta providers, nÃ£o permite alteraÃ§Ã£o ou exclusÃ£o e nÃ£o converte moedas.

## Carteiras

O cadastro inicial de carteiras está disponível em `POST /carteiras`, `GET /carteiras` e `GET /carteiras/{id}`. O POST recebe `nome` obrigatório e `descricao` opcional; o nome é normalizado para impedir duplicidade. Operações, posições e cálculos de patrimônio serão adicionados posteriormente.
## OperaÃ§Ãµes

Registre compras e vendas em `POST /operacoes` informando `carteiraId`, `acaoId`, `tipo` (`COMPRA` ou `VENDA`), `quantidade`, `precoUnitario` e `dataOperacao`. Consulte uma operaÃ§Ã£o em `GET /operacoes/{id}` ou liste as operaÃ§Ãµes de uma carteira em `GET /carteiras/{id}/operacoes`. Esta versÃ£o registra os fatos transacionais, sem calcular posiÃ§Ã£o, preÃ§o mÃ©dio ou patrimÃ´nio.
## Posições e resumo

Consulte `GET /carteiras/{id}/posicoes` para posições agregadas por ação e `GET /carteiras/{id}/resumo` para totais separados por moeda. Os cálculos usam operações e a última cotação persistida, sem conversão cambial ou chamadas externas.
