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

Os campos `razaoSocial`, `nomeFantasia`, `email`, `telefone`, `cep`, `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `uf` e `situacaoCadastral` vêm da resposta cadastral. CEP é armazenado somente com dígitos, UF em letras maiúsculas e campos opcionais vazios como `null`. `id`, `dataCadastro` e `validadaNaCvm` continuam controlados pelo sistema.

A resposta contém todos os campos persistidos, incluindo:

```json
{
  "id": 1,
  "cnpj": "11222333000181",
  "validadaNaCvm": false,
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

Não há retry automático. A BrasilAPI não exige credencial para esse endpoint e nenhum token ou corpo de erro externo é exposto pela nossa API.

Esta integração usa os dados de endereço presentes na própria resposta de CNPJ, mas ainda não realiza uma consulta específica a provider de CEP. Todo registro continua criado com `validadaNaCvm=false`; portanto, a consulta cadastral não confirma que a instituição está autorizada a atuar no mercado financeiro. CEP e CVM permanecem em mudanças futuras separadas.

Os testes automatizados substituem o port interno e não chamam a BrasilAPI real. A validação real do adapter é opt-in e deve ser executada somente de forma controlada.

### Parar o ambiente

Pare o contêiner preservando o volume `postgres_data`:

```powershell
docker compose --env-file .env down
```

O comando abaixo também remove o volume e apaga os dados locais. Use-o somente quando a exclusão for intencional:

```powershell
docker compose --env-file .env down -v
```
