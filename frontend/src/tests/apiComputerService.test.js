import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createApiComputerService } from '../services/apiComputerService.js';

// These tests replace fetch() with a fake, so no real server is needed.
// They check that every method calls the correct REST route.

function jsonResponse(status, body) {
  return { ok: status >= 200 && status < 300, status, json: async () => body };
}

describe('API computer service', () => {
  const base = 'http://localhost:8080';
  let service;

  beforeEach(() => {
    service = createApiComputerService(base);
  });

  it('GET /api/computers?page=1&size=5 for a page', async () => {
    const page = { content: [], page: 1, size: 5, totalElements: 6, totalPages: 2 };
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, page));
    await expect(service.list(1, 5)).resolves.toEqual(page);
    expect(fetchMock).toHaveBeenCalledWith(`${base}/api/computers?page=1&size=5`, expect.any(Object));
  });

  it('GET /api/computers/{id} to find one computer', async () => {
    const computer = { id: 7, name: 'PC', brand: 'HP', serialNumber: 'HP-1', status: 'IN_USE' };
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, computer));
    await expect(service.getById(7)).resolves.toEqual(computer);
    expect(fetchMock.mock.calls[0][0]).toBe(`${base}/api/computers/7`);
  });

  it('POST /api/computers with trimmed JSON to add', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(201, { id: 9 }));
    await service.create({ name: ' Lab PC ', brand: 'Dell', serialNumber: 'DL-1', status: 'AVAILABLE' });
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe(`${base}/api/computers`);
    expect(options.method).toBe('POST');
    expect(JSON.parse(options.body)).toEqual({
      name: 'Lab PC',
      brand: 'Dell',
      serialNumber: 'DL-1',
      status: 'AVAILABLE',
    });
  });

  it('PUT /api/computers/{id} to update', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, { id: 2 }));
    await service.update(2, { name: 'PC', brand: 'HP', serialNumber: 'HP-22', status: 'RETIRED' });
    expect(fetchMock.mock.calls[0][0]).toBe(`${base}/api/computers/2`);
    expect(fetchMock.mock.calls[0][1].method).toBe('PUT');
  });

  it('DELETE /api/computers/{id} and accept 204 No Content', async () => {
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue({ ok: true, status: 204, json: async () => null });
    await expect(service.remove(4)).resolves.toBeNull();
    expect(fetchMock.mock.calls[0][1].method).toBe('DELETE');
  });

  it('turns an error response into a ServiceError with the server message', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(400, {
        status: 400,
        error: 'Bad Request',
        message: 'Validation failed',
        fieldErrors: { name: 'Name is required.' },
      }),
    );
    await expect(service.create({})).rejects.toMatchObject({
      status: 400,
      message: 'Validation failed',
      fieldErrors: { name: 'Name is required.' },
    });
  });

  it('explains when the server cannot be reached', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'));
    await expect(service.list()).rejects.toMatchObject({
      status: 0,
      message: expect.stringContaining('Is the Spring Boot backend running?'),
    });
  });
});
