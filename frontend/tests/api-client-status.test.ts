import assert from 'node:assert/strict';
import { after, test } from 'node:test';

Object.defineProperty(globalThis, 'sessionStorage', {
  configurable: true,
  value: { getItem: () => 'test-session-token' },
});

const originalFetch = globalThis.fetch;
const { ApiError, apiClient, subscribeApiStatus } = await import('../src/api/client.ts');

after(() => {
  globalThis.fetch = originalFetch;
});

test('resposta HTTP não-2xx mantém API online e preserva ApiError de negócio', async () => {
  const statuses: boolean[] = [];
  const unsubscribe = subscribeApiStatus((online) => statuses.push(online));
  globalThis.fetch = async () => new Response(JSON.stringify({ message: 'Regra de negócio recusada.' }), {
    status: 422,
    headers: { 'Content-Type': 'application/json' },
  });

  await assert.rejects(apiClient.get('/teste-http'), (error: unknown) => {
    assert.ok(error instanceof ApiError);
    assert.equal(error.status, 422);
    assert.equal(error.message, 'Regra de negócio recusada.');
    return true;
  });
  assert.deepEqual(statuses, [true]);
  unsubscribe();
});

test('resposta HTTP de sucesso mantém API online', async () => {
  const statuses: boolean[] = [];
  const unsubscribe = subscribeApiStatus((online) => statuses.push(online));
  globalThis.fetch = async () => new Response(JSON.stringify({ ok: true }), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
  });

  assert.deepEqual(await apiClient.get('/teste-sucesso'), { ok: true });
  assert.deepEqual(statuses, [true]);
  unsubscribe();
});

test('falha de rede sem Response marca API offline', async () => {
  const statuses: boolean[] = [];
  const unsubscribe = subscribeApiStatus((online) => statuses.push(online));
  globalThis.fetch = async () => { throw new TypeError('simulated network failure'); };

  await assert.rejects(apiClient.get('/teste-rede'), (error: unknown) => {
    assert.ok(error instanceof ApiError);
    assert.equal(error.status, 503);
    assert.equal(error.message, 'Não foi possível conectar ao backend.');
    return true;
  });
  assert.deepEqual(statuses, [false]);
  unsubscribe();
});
