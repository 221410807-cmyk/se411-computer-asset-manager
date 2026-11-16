import { NavLink } from 'react-router-dom';

// Top menu. NavLink adds the "active" class to the current page's link.
export default function NavBar() {
  return (
    <header className="navbar">
      <div className="brand">
        <span className="brand-icon" aria-hidden="true">▣</span>
        Computer Asset Manager
      </div>
      <nav aria-label="Main menu">
        <NavLink to="/" end>
          Inventory
        </NavLink>
        <NavLink to="/about">About</NavLink>
      </nav>
    </header>
  );
}
