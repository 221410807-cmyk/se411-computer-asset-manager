import { useEffect, useState } from 'react';
import computerService from '../services/computerService.js';
import ComputerForm from '../components/ComputerForm.jsx';
import ComputerTable from '../components/ComputerTable.jsx';
import Pagination from '../components/Pagination.jsx';
import FeedbackMessage from '../components/FeedbackMessage.jsx';
import { validateIdInput } from '../utils/validation.js';

// The main page: view, add, edit, delete and find computers.
// "service" is either the local (standalone) service or the API service.
// Tests can pass their own service through this prop.
export default function InventoryPage({ service = computerService }) {
  // Paging state. page starts at 0, like Spring's pagination.
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(5);
  const [reloadKey, setReloadKey] = useState(0); // change it to reload the list

  // What the list shows.
  const [pageData, setPageData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');

  // Feedback banner and edit mode.
  const [message, setMessage] = useState(null);
  const [editing, setEditing] = useState(null);

  // "Find by ID" state.
  const [searchText, setSearchText] = useState('');
  const [searchError, setSearchError] = useState('');
  const [searchResult, setSearchResult] = useState(null);
  const [searching, setSearching] = useState(false);

  // Load one page whenever the page number, page size or reloadKey changes.
  useEffect(() => {
    let ignore = false; // ignore answers that arrive after the user moved on
    setLoading(true);
    setLoadError('');
    service
      .list(page, size)
      .then((data) => {
        if (ignore) return;
        // If we deleted the last item on the last page, go back one page.
        if (data.content.length === 0 && page > 0) {
          setPage(page - 1);
          return;
        }
        setPageData(data);
      })
      .catch((error) => {
        if (!ignore) setLoadError(error.message);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, [service, page, size, reloadKey]);

  function reload() {
    setReloadKey((key) => key + 1);
  }

  function showSuccess(text) {
    setMessage({ type: 'success', text });
  }

  function showError(text) {
    setMessage({ type: 'error', text });
  }

  // Called by the form for both "Add" and "Save changes".
  async function handleSubmit(values) {
    try {
      if (editing) {
        const updated = await service.update(editing.id, values);
        setEditing(null);
        if (searchResult?.id === updated.id) setSearchResult(updated);
        showSuccess(`Saved changes to "${updated.name}" (ID ${updated.id}).`);
      } else {
        const created = await service.create(values);
        // New computers get the highest ID, so they appear on the last page.
        const totalBefore = pageData ? pageData.totalElements : 0;
        setPage(Math.floor(totalBefore / size));
        showSuccess(`Added "${created.name}" with ID ${created.id}.`);
      }
      reload();
    } catch (error) {
      showError(error.message);
      throw error; // lets the form highlight the fields the server rejected
    }
  }

  async function handleDelete(computer) {
    const sure = window.confirm(
      `Delete "${computer.name}" (ID ${computer.id})? This cannot be undone.`,
    );
    if (!sure) return;
    try {
      await service.remove(computer.id);
      if (editing?.id === computer.id) setEditing(null);
      if (searchResult?.id === computer.id) setSearchResult(null);
      showSuccess(`Deleted "${computer.name}" (ID ${computer.id}).`);
      reload();
    } catch (error) {
      showError(error.message);
    }
  }

  async function handleSearch(event) {
    event.preventDefault();
    setSearchResult(null);
    const error = validateIdInput(searchText);
    setSearchError(error);
    if (error) return;

    const id = Number(searchText.trim());
    setSearching(true);
    try {
      setSearchResult(await service.getById(id));
    } catch (err) {
      setSearchError(err.status === 404 ? `No computer found with ID ${id}.` : err.message);
    } finally {
      setSearching(false);
    }
  }

  function clearSearch() {
    setSearchText('');
    setSearchError('');
    setSearchResult(null);
  }

  function handleSizeChange(newSize) {
    setSize(newSize);
    setPage(0);
  }

  const hasRows = pageData && pageData.content.length > 0;

  return (
    <section>
      <div className="page-header">
        <h1>Inventory</h1>
        <p className="mode-badge">
          {service.mode === 'api'
            ? `Connected to the Spring Boot API (${service.baseUrl})`
            : 'Standalone mode: sample data stored in the browser'}
        </p>
      </div>

      <FeedbackMessage message={message} onClose={() => setMessage(null)} />

      <div className="layout">
        <ComputerForm computer={editing} onSubmit={handleSubmit} onCancel={() => setEditing(null)} />

        <div className="main-column">
          {/* Find a single computer by its ID */}
          <div className="card">
            <h2>Find by ID</h2>
            <form className="search-row" onSubmit={handleSearch} role="search" noValidate>
              <label htmlFor="search-id" className="visually-hidden">
                Computer ID
              </label>
              <input
                id="search-id"
                inputMode="numeric"
                placeholder="Computer ID, e.g. 3"
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
              />
              <button type="submit" className="button primary" disabled={searching}>
                {searching ? 'Searching…' : 'Search'}
              </button>
              {(searchResult || searchError) && (
                <button type="button" className="button" onClick={clearSearch}>
                  Clear
                </button>
              )}
            </form>
            {searchError && (
              <p className="field-error" role="alert">
                {searchError}
              </p>
            )}
            {searchResult && (
              <ComputerTable
                computers={[searchResult]}
                caption="Search result"
                onEdit={setEditing}
                onDelete={handleDelete}
              />
            )}
          </div>

          {/* The paginated list of all computers */}
          <div className="card">
            <h2>All computers</h2>

            {loading && (
              <p className="state" role="status">
                Loading computers…
              </p>
            )}

            {!loading && loadError && (
              <div className="state state-error" role="alert">
                <p>Could not load computers. {loadError}</p>
                <button type="button" className="button" onClick={reload}>
                  Try again
                </button>
              </div>
            )}

            {!loading && !loadError && pageData && pageData.totalElements === 0 && (
              <p className="state">No computers yet. Use the form to add the first one.</p>
            )}

            {!loadError && hasRows && (
              <div className={loading ? 'is-loading' : undefined}>
                <ComputerTable
                  computers={pageData.content}
                  caption="All computers"
                  onEdit={setEditing}
                  onDelete={handleDelete}
                />
                <Pagination
                  page={pageData.page}
                  size={size}
                  totalPages={pageData.totalPages}
                  totalElements={pageData.totalElements}
                  onPageChange={setPage}
                  onSizeChange={handleSizeChange}
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
