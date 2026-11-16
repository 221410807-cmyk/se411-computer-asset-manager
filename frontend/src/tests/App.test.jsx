import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import App from '../App.jsx';
import team from '../data/team.js';

function renderAt(path) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <App />
    </MemoryRouter>,
  );
}

describe('navigation menu', () => {
  it('shows the Inventory page first', async () => {
    renderAt('/');
    expect(screen.getByRole('heading', { level: 1, name: 'Inventory' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Inventory' })).toHaveClass('active');
    expect(await screen.findByRole('table', { name: 'All computers' })).toBeInTheDocument();
  });

  it('opens the About page from the menu and shows names and student IDs', async () => {
    const user = userEvent.setup();
    renderAt('/');
    await user.click(screen.getByRole('link', { name: 'About' }));

    expect(screen.getByRole('heading', { level: 1, name: 'About' })).toBeInTheDocument();
    const teamTable = screen.getByRole('table', { name: 'Team members' });
    expect(teamTable).toHaveTextContent('Student ID');
    for (const member of team) {
      expect(teamTable).toHaveTextContent(member.name);
    }
  });

  it('goes back to Inventory from the About page', async () => {
    const user = userEvent.setup();
    renderAt('/about');
    await user.click(screen.getByRole('link', { name: 'Inventory' }));
    expect(screen.getByRole('heading', { level: 1, name: 'Inventory' })).toBeInTheDocument();
  });

  it('sends unknown addresses to the Inventory page', () => {
    renderAt('/does-not-exist');
    expect(screen.getByRole('heading', { level: 1, name: 'Inventory' })).toBeInTheDocument();
  });
});
