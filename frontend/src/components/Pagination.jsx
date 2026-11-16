// Previous / Next buttons plus a page-size selector.
// "page" starts at 0 (like Spring), but we show it to people starting at 1.
export default function Pagination({ page, size, totalPages, totalElements, onPageChange, onSizeChange }) {
  const shownTotalPages = Math.max(totalPages, 1);
  return (
    <div className="pagination">
      <button
        type="button"
        className="button small"
        onClick={() => onPageChange(page - 1)}
        disabled={page <= 0}
      >
        ← Previous
      </button>
      <span aria-live="polite">
        Page {page + 1} of {shownTotalPages} ({totalElements} computers)
      </span>
      <button
        type="button"
        className="button small"
        onClick={() => onPageChange(page + 1)}
        disabled={page + 1 >= totalPages}
      >
        Next →
      </button>
      <label className="page-size">
        Per page
        <select value={size} onChange={(e) => onSizeChange(Number(e.target.value))}>
          <option value={5}>5</option>
          <option value={10}>10</option>
        </select>
      </label>
    </div>
  );
}
