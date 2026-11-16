// INTEGRATED data service (Part 2).
// Sends HTTP requests to the Spring Boot backend with fetch().
// Every method matches a REST route:
//   list     -> GET    /api/computers?page=0&size=5
//   getById  -> GET    /api/computers/{id}
//   create   -> POST   /api/computers
//   update   -> PUT    /api/computers/{id}
//   remove   -> DELETE /api/computers/{id}
import ServiceError from './ServiceError.js';
import { cleanComputer } from '../utils/validation.js';

export function createApiComputerService(baseUrl = 'http://localhost:8080') {
  const root = `${baseUrl}/api/computers`;

  // Small helper that sends a request and turns error responses into ServiceError.
  async function request(url, options = {}) {
    let response;
    try {
      response = await fetch(url, {
        headers: { 'Content-Type': 'application/json' },
        ...options,
      });
    } catch {
      throw new ServiceError(
        `Cannot reach the server at ${baseUrl}. Is the Spring Boot backend running?`,
        0,
      );
    }

    if (response.status === 204) return null; // DELETE returns no body.

    const body = await response.json().catch(() => null);
    if (!response.ok) {
      const message = body?.message || `Request failed with status ${response.status}.`;
      throw new ServiceError(message, response.status, body?.fieldErrors || {});
    }
    return body;
  }

  return {
    mode: 'api',
    baseUrl,

    list(page = 0, size = 5) {
      return request(`${root}?page=${page}&size=${size}`);
    },

    getById(id) {
      return request(`${root}/${encodeURIComponent(id)}`);
    },

    create(values) {
      return request(root, { method: 'POST', body: JSON.stringify(cleanComputer(values)) });
    },

    update(id, values) {
      return request(`${root}/${encodeURIComponent(id)}`, {
        method: 'PUT',
        body: JSON.stringify(cleanComputer(values)),
      });
    },

    remove(id) {
      return request(`${root}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    },
  };
}
