# Adapt Invest — Frontend

Frontend do Adapt Invest desenvolvido com React, TypeScript e Vite. Em ambiente local, ele espera a API Spring Boot disponível em `http://localhost:8080`; a URL pode ser ajustada pela configuração de ambiente documentada no README principal.

## Execução local

Instale as dependências e inicie o servidor de desenvolvimento:

```bash
npm install
npm run dev
```

A autenticação é realizada pelo backend por JWT Bearer; o frontend não contém segredo de assinatura nem credenciais de providers externos.

## Validação

```bash
npm test
npm run build
npm run lint
```

Consulte o [README principal](../README.md) para preparar banco, backend e variáveis de ambiente.
