import { Navigate, Route, Routes } from 'react-router-dom';
import NavBar from './components/NavBar.jsx';
import InventoryPage from './pages/InventoryPage.jsx';
import AboutPage from './pages/AboutPage.jsx';
import './App.css';

// The app shell: the menu on top and one page underneath.
// The router itself is created in main.jsx (and in tests with MemoryRouter).
export default function App() {
  return (
    <div className="app">
      <NavBar />
      <main className="container">
        <Routes>
          <Route path="/" element={<InventoryPage />} />
          <Route path="/about" element={<AboutPage />} />
          {/* Any unknown address goes back to the inventory */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="footer">Computer Asset Manager · SE411 project</footer>
    </div>
  );
}
