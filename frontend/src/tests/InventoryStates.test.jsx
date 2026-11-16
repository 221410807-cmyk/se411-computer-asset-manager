import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import InventoryPage from '../pages/InventoryPage.jsx';
import ServiceError from '../services/ServiceError.js';

// A fake API service. Each test decides what list() returns.
function fakeApiService(list) {
  return {
    mode: 'api',
    baseUrl: 'http://localhost:8080',
    list,
    getById: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    remove: vi.fn(),
  };
}

const emptyPage = { content: [], page: 0, size: 5, totalElements: 0, totalPages: 0 };

describe('Inventory page loading, empty and error states (integrated mode)', () => {
  it('shows a loading message while waiting for the server', () => {
    const service = fakeApiService(() => new Promise(() => {})); // never answers
    render(<InventoryPage service={service} />);
    expect(screen.getByText('Loading computers…')).toBeInTheDocument();
    expect(screen.getByText(/Connected to the Spring Boot API/)).toBeInTheDocument();
  });

  it('shows an empty message when there are no computers', async () => {
    render(<InventoryPage service={fakeApiService(async () => emptyPage)} />);
    expect(await screen.findByText(/No computers yet/)).toBeInTheDocument();
  });

  it('shows an error with a retry button when the server is down', async () => {
    const user = userEvent.setup();
    const list = vi
      .fn()
      .mockRejectedValueOnce(new ServiceError('Cannot reach the server at http://localhost:8080.', 0))
      .mockResolvedValueOnce({
        content: [{ id: 1, name: 'Server PC', brand: 'HP', serialNumber: 'HP-1000', status: 'IN_USE' }],
        page: 0,
        size: 5,
        totalElements: 1,
        totalPages: 1,
      });
    render(<InventoryPage service={fakeApiService(list)} />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Cannot reach the server');
    await user.click(screen.getByRole('button', { name: 'Try again' }));
    expect(await screen.findByText('Server PC')).toBeInTheDocument();
    expect(list).toHaveBeenCalledTimes(2);
  });

  it('asks the server for the next page (server-side pagination)', async () => {
    const user = userEvent.setup();
    const list = vi.fn(async (page, size) => ({
      content: [{ id: page + 1, name: `PC on page ${page}`, brand: 'HP', serialNumber: `HP-000${page}`, status: 'AVAILABLE' }],
      page,
      size,
      totalElements: 6,
      totalPages: 2,
    }));
    render(<InventoryPage service={fakeApiService(list)} />);
    await screen.findByText('PC on page 0');
    await user.click(screen.getByRole('button', { name: /Next/ }));
    expect(await screen.findByText('PC on page 1')).toBeInTheDocument();
    expect(list).toHaveBeenLastCalledWith(1, 5);
  });
});
