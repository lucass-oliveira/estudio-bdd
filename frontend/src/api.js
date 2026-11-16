import { useEffect, useState } from 'react';

const baseUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api').replace(/\/$/, '');

export async function request(path, { userId, method = 'GET', body, signal, download = false } = {}) {
  const headers = new Headers({ Accept: download ? 'text/plain, application/problem+json' : 'application/json, application/problem+json' });
  if (userId != null) headers.set('X-Usuario-Id', userId);
  if (body !== undefined) headers.set('Content-Type', 'application/json');

  let response;
  try {
    response = await fetch(`${baseUrl}${path}`, { method, headers, body: JSON.stringify(body), signal });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new Error('Não foi possível conectar à API. Verifique se o servidor está em execução.');
  }

  if (!response.ok) {
    const problem = await response.json().catch(() => ({}));
    const details = Object.values(problem.erros || {}).flat().filter(Boolean);
    const message = problem.detail || problem.title || `A API retornou o erro ${response.status}.`;
    throw new Error([message, ...details].join(' '));
  }

  if (download) {
    const disposition = response.headers.get('Content-Disposition') || '';
    const utf8Name = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1];
    const name = utf8Name ? decodeURIComponent(utf8Name) : disposition.match(/filename="?([^";]+)/i)?.[1];
    return { blob: await response.blob(), name: name || 'historia.feature' };
  }
  return response.status === 204 ? null : response.json();
}

export function useResource(path, userId, revision = 0) {
  const [result, setResult] = useState(null);

  useEffect(() => {
    if (!path) return;
    const controller = new AbortController();
    request(path, { userId, signal: controller.signal })
      .then(data => setResult({ path, userId, revision, data }))
      .catch(error => {
        if (error.name !== 'AbortError') setResult({ path, userId, revision, error: error.message });
      });
    return () => controller.abort();
  }, [path, userId, revision]);

  if (!path) return { loading: false };
  const matches = result?.path === path && result.userId === userId;
  if (!matches) return { loading: true };
  return { ...result, loading: result.revision !== revision };
}
