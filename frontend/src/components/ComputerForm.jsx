import { useEffect, useRef, useState } from 'react';
import { STATUS_OPTIONS, validateComputer } from '../utils/validation.js';

const EMPTY_FORM = { name: '', brand: '', serialNumber: '', status: 'AVAILABLE' };

// Form used for both adding and editing a computer.
// - computer: the computer being edited, or null when adding a new one
// - onSubmit(values): must return a Promise; if it rejects with fieldErrors we show them
// - onCancel(): leave edit mode
export default function ComputerForm({ computer, onSubmit, onCancel }) {
  const [values, setValues] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const isEditing = Boolean(computer);
  const firstInputRef = useRef(null);

  // When the user clicks "Edit" on a row, copy that computer into the form
  // and move the cursor to the first field.
  useEffect(() => {
    setValues(computer ? { ...computer } : EMPTY_FORM);
    setErrors({});
    if (computer) firstInputRef.current?.focus();
  }, [computer]);

  function handleChange(event) {
    const { name, value } = event.target;
    setValues((old) => ({ ...old, [name]: value }));
    setErrors((old) => ({ ...old, [name]: undefined })); // clear the error while typing
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const found = validateComputer(values);
    setErrors(found);
    if (Object.keys(found).length > 0) return; // stop: show the messages

    setSaving(true);
    try {
      await onSubmit(values);
      if (!isEditing) setValues(EMPTY_FORM); // ready for the next computer
    } catch (error) {
      // The server (or local service) can also reject input, e.g. a duplicate serial number.
      if (error?.fieldErrors) setErrors(error.fieldErrors);
    } finally {
      setSaving(false);
    }
  }

  // Small helper that draws one labelled text input with its error message.
  function textField(name, label, placeholder) {
    const errorId = `${name}-error`;
    return (
      <div className="field">
        <label htmlFor={name}>{label}</label>
        <input
          ref={name === 'name' ? firstInputRef : undefined}
          id={name}
          name={name}
          value={values[name]}
          onChange={handleChange}
          placeholder={placeholder}
          aria-invalid={Boolean(errors[name])}
          aria-describedby={errors[name] ? errorId : undefined}
        />
        {errors[name] && (
          <p className="field-error" id={errorId}>
            {errors[name]}
          </p>
        )}
      </div>
    );
  }

  return (
    <form className="card" onSubmit={handleSubmit} noValidate aria-label="Computer form">
      <h2>{isEditing ? `Edit computer #${computer.id}` : 'Add a computer'}</h2>

      {textField('name', 'Name', 'e.g. Finance Laptop 03')}
      {textField('brand', 'Brand', 'e.g. Dell')}
      {textField('serialNumber', 'Serial number', 'e.g. DL-1003')}

      <div className="field">
        <label htmlFor="status">Status</label>
        <select
          id="status"
          name="status"
          value={values.status}
          onChange={handleChange}
          aria-invalid={Boolean(errors.status)}
        >
          {STATUS_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        {errors.status && <p className="field-error">{errors.status}</p>}
      </div>

      <div className="form-actions">
        <button type="submit" className="button primary" disabled={saving}>
          {saving ? 'Saving…' : isEditing ? 'Save changes' : 'Add computer'}
        </button>
        {isEditing && (
          <button type="button" className="button" onClick={onCancel}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
