import { statusLabel } from '../utils/validation.js';

// Shows a list of computers with Edit and Delete buttons on each row.
export default function ComputerTable({ computers, onEdit, onDelete, caption }) {
  return (
    <div className="table-wrapper">
      <table>
        <caption className="visually-hidden">{caption}</caption>
        <thead>
          <tr>
            <th scope="col">ID</th>
            <th scope="col">Name</th>
            <th scope="col">Brand</th>
            <th scope="col">Serial number</th>
            <th scope="col">Status</th>
            <th scope="col">
              <span className="visually-hidden">Actions</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {computers.map((computer) => (
            <tr key={computer.id}>
              <td>{computer.id}</td>
              <td>{computer.name}</td>
              <td>{computer.brand}</td>
              <td className="mono">{computer.serialNumber}</td>
              <td>
                <span className={`status status-${computer.status}`}>
                  {statusLabel(computer.status)}
                </span>
              </td>
              <td className="row-actions">
                <button
                  type="button"
                  className="button small"
                  onClick={() => onEdit(computer)}
                  aria-label={`Edit ${computer.name}`}
                >
                  Edit
                </button>
                <button
                  type="button"
                  className="button small danger"
                  onClick={() => onDelete(computer)}
                  aria-label={`Delete ${computer.name}`}
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
