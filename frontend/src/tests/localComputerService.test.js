import { beforeEach, describe, expect, it } from 'vitest';
import { createLocalComputerService } from '../services/localComputerService.js';
import sampleComputers from '../data/sampleComputers.js';

const newComputer = { name: 'New Laptop', brand: 'Asus', serialNumber: 'AS-7001', status: 'AVAILABLE' };

describe('local (standalone) computer service', () => {
  let service;

  beforeEach(() => {
    service = createLocalComputerService(sampleComputers);
  });

  it('returns the first page in the same shape as the backend', async () => {
    const page = await service.list(0, 5);
    expect(page.content).toHaveLength(5);
    expect(page.page).toBe(0);
    expect(page.size).toBe(5);
    expect(page.totalElements).toBe(sampleComputers.length);
    expect(page.totalPages).toBe(Math.ceil(sampleComputers.length / 5));
  });

  it('returns the remaining items on the last page and nothing past it', async () => {
    expect((await service.list(1, 5)).content).toHaveLength(sampleComputers.length - 5);
    expect((await service.list(5, 5)).content).toHaveLength(0);
  });

  it('finds a computer by ID', async () => {
    expect((await service.getById(3)).name).toBe('Design Workstation');
  });

  it('throws a 404 error for a missing ID', async () => {
    await expect(service.getById(999)).rejects.toMatchObject({ status: 404 });
  });

  it('adds a computer with the next ID', async () => {
    const created = await service.create(newComputer);
    expect(created.id).toBe(sampleComputers.length + 1);
    expect((await service.list(0, 50)).totalElements).toBe(sampleComputers.length + 1);
  });

  it('rejects invalid input with field errors', async () => {
    await expect(service.create({ ...newComputer, name: '' })).rejects.toMatchObject({
      status: 400,
      fieldErrors: { name: 'Name is required.' },
    });
  });

  it('rejects a duplicate serial number', async () => {
    await expect(service.create({ ...newComputer, serialNumber: 'dl-1001' })).rejects.toMatchObject({
      status: 409,
    });
  });

  it('updates a computer and lets it keep its own serial number', async () => {
    const updated = await service.update(1, {
      name: 'Renamed',
      brand: 'Dell',
      serialNumber: 'DL-1001',
      status: 'RETIRED',
    });
    expect(updated).toEqual({ id: 1, name: 'Renamed', brand: 'Dell', serialNumber: 'DL-1001', status: 'RETIRED' });
  });

  it('throws a 404 error when updating a missing ID', async () => {
    await expect(service.update(999, newComputer)).rejects.toMatchObject({ status: 404 });
  });

  it('deletes a computer', async () => {
    await service.remove(2);
    await expect(service.getById(2)).rejects.toMatchObject({ status: 404 });
  });

  it('throws a 404 error when deleting a missing ID', async () => {
    await expect(service.remove(999)).rejects.toMatchObject({ status: 404 });
  });

  it('never changes the original sample array', async () => {
    await service.remove(1);
    expect(sampleComputers[0].id).toBe(1);
  });
});
