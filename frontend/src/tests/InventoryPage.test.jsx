import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import InventoryPage from '../pages/InventoryPage.jsx';
import { createLocalComputerService } from '../services/localComputerService.js';
import sampleComputers from '../data/sampleComputers.js';

// Each test gets a fresh standalone service with the 8 sample computers.
async function setup() {
  const user = userEvent.setup();
  const service = createLocalComputerService(sampleComputers);
  render(<InventoryPage service={service} />);
  const table = await screen.findByRole('table', { name: 'All computers' });
  return { user, service, table };
}

function bodyRows(table) {
  return within(table).getAllByRole('row').slice(1); // skip the header row
}

async function fillForm(user, { name, brand, serialNumber, status }) {
  const form = screen.getByRole('form', { name: 'Computer form' });
  const nameInput = within(form).getByLabelText('Name');
  const brandInput = within(form).getByLabelText('Brand');
  const serialInput = within(form).getByLabelText('Serial number');
  await user.clear(nameInput);
  await user.clear(brandInput);
  await user.clear(serialInput);
  if (name) await user.type(nameInput, name);
  if (brand) await user.type(brandInput, brand);
  if (serialNumber) await user.type(serialInput, serialNumber);
  if (status) await user.selectOptions(within(form).getByLabelText('Status'), status);
  return form;
}

describe('Inventory page (standalone mode)', () => {
  beforeEach(() => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
  });

  it('displays the first page of computers', async () => {
    const { table } = await setup();
    expect(bodyRows(table)).toHaveLength(5);
    expect(table).toHaveTextContent('Finance Laptop 01');
    expect(screen.getByText(/Page 1 of 2 \(8 computers\)/)).toBeInTheDocument();
    expect(screen.getByText(/Standalone mode/)).toBeInTheDocument();
  });

  it('moves between pages', async () => {
    const { user } = await setup();
    await user.click(screen.getByRole('button', { name: /Next/ }));
    const table = screen.getByRole('table', { name: 'All computers' });
    expect(await within(table).findByText('Training Room PC 1')).toBeInTheDocument();
    expect(bodyRows(table)).toHaveLength(3);
    expect(screen.getByRole('button', { name: /Next/ })).toBeDisabled();

    await user.click(screen.getByRole('button', { name: /Previous/ }));
    expect(await within(table).findByText('Finance Laptop 01')).toBeInTheDocument();
  });

  it('adds a new computer and shows a success message', async () => {
    const { user, service } = await setup();
    const form = await fillForm(user, {
      name: 'Lab Laptop',
      brand: 'Asus',
      serialNumber: 'AS-7001',
      status: 'IN_USE',
    });
    await user.click(within(form).getByRole('button', { name: 'Add computer' }));

    expect(await screen.findByRole('status')).toHaveTextContent('Added "Lab Laptop" with ID 9.');
    // The page jumps to the last page, where the new computer is.
    const table = screen.getByRole('table', { name: 'All computers' });
    expect(await within(table).findByText('Lab Laptop')).toBeInTheDocument();
    expect((await service.list(0, 50)).totalElements).toBe(9);
    // The form is cleared for the next entry.
    expect(within(form).getByLabelText('Name')).toHaveValue('');
  });

  it('shows validation messages and does not save invalid input', async () => {
    const { user, service } = await setup();
    const form = await fillForm(user, { name: '', brand: 'X', serialNumber: 'bad serial!' });
    await user.click(within(form).getByRole('button', { name: 'Add computer' }));

    expect(within(form).getByText('Name is required.')).toBeInTheDocument();
    expect(within(form).getByText('Brand must be 2-30 characters.')).toBeInTheDocument();
    expect(within(form).getByText(/letters, numbers and dashes/)).toBeInTheDocument();
    expect(within(form).getByLabelText('Name')).toHaveAttribute('aria-invalid', 'true');
    expect((await service.list(0, 50)).totalElements).toBe(8);
  });

  it('rejects a duplicate serial number with a clear message', async () => {
    const { user } = await setup();
    const form = await fillForm(user, { name: 'Copy', brand: 'Dell', serialNumber: 'DL-1001' });
    await user.click(within(form).getByRole('button', { name: 'Add computer' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('DL-1001 is already used');
    expect(within(form).getByText('This serial number is already in use.')).toBeInTheDocument();
  });

  it('edits a computer', async () => {
    const { user, table } = await setup();
    await user.click(within(table).getByRole('button', { name: 'Edit Finance Laptop 02' }));

    const form = screen.getByRole('form', { name: 'Computer form' });
    expect(within(form).getByRole('heading')).toHaveTextContent('Edit computer #2');
    expect(within(form).getByLabelText('Name')).toHaveValue('Finance Laptop 02');

    await fillForm(user, { name: 'Finance Laptop 2B', brand: 'Dell', serialNumber: 'DL-1002', status: 'IN_REPAIR' });
    await user.click(within(form).getByRole('button', { name: 'Save changes' }));

    expect(await screen.findByRole('status')).toHaveTextContent('Saved changes to "Finance Laptop 2B" (ID 2).');
    expect(await within(table).findByText('Finance Laptop 2B')).toBeInTheDocument();
    expect(within(form).getByRole('heading')).toHaveTextContent('Add a computer');
  });

  it('cancels editing', async () => {
    const { user, table } = await setup();
    await user.click(within(table).getByRole('button', { name: 'Edit Reception Desktop' }));
    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.getByRole('heading', { name: 'Add a computer' })).toBeInTheDocument();
  });

  it('deletes a computer after confirmation', async () => {
    const { user, table } = await setup();
    await user.click(within(table).getByRole('button', { name: 'Delete Finance Laptop 01' }));

    expect(window.confirm).toHaveBeenCalled();
    expect(await screen.findByRole('status')).toHaveTextContent('Deleted "Finance Laptop 01" (ID 1).');
    expect(within(table).queryByText('Finance Laptop 01')).not.toBeInTheDocument();
    expect(screen.getByText(/\(7 computers\)/)).toBeInTheDocument();
  });

  it('keeps the computer when the user cancels the delete', async () => {
    window.confirm.mockReturnValue(false);
    const { user, table } = await setup();
    await user.click(within(table).getByRole('button', { name: 'Delete Finance Laptop 01' }));
    expect(within(table).getByText('Finance Laptop 01')).toBeInTheDocument();
  });

  it('goes back a page when the last item on the last page is deleted', async () => {
    const { user, service } = await setup();
    await service.remove(7);
    await service.remove(8);
    await user.click(screen.getByRole('button', { name: /Next/ }));
    const table = screen.getByRole('table', { name: 'All computers' });
    await user.click(await within(table).findByRole('button', { name: 'Delete Training Room PC 1' }));
    expect(await screen.findByText(/Page 1 of 1 \(5 computers\)/)).toBeInTheDocument();
  });

  it('finds a computer by ID', async () => {
    const { user } = await setup();
    await user.type(screen.getByLabelText('Computer ID'), '4');
    await user.click(screen.getByRole('button', { name: 'Search' }));
    const result = await screen.findByRole('table', { name: 'Search result' });
    expect(result).toHaveTextContent('Reception Desktop');
    expect(result).toHaveTextContent('HP-3001');
  });

  it('says when no computer has that ID', async () => {
    const { user } = await setup();
    await user.type(screen.getByLabelText('Computer ID'), '404');
    await user.click(screen.getByRole('button', { name: 'Search' }));
    expect(await screen.findByText('No computer found with ID 404.')).toBeInTheDocument();
  });

  it('rejects a search ID that is not a number', async () => {
    const { user } = await setup();
    await user.type(screen.getByLabelText('Computer ID'), 'abc');
    await user.click(screen.getByRole('button', { name: 'Search' }));
    expect(screen.getByText('Enter a positive whole number ID.')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Clear' }));
    expect(screen.queryByText('Enter a positive whole number ID.')).not.toBeInTheDocument();
  });
});
