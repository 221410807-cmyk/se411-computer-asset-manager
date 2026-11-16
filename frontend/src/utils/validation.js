// Shared input rules for a computer.
// The Spring Boot backend checks the same rules, so the user sees the same messages
// whether the error is caught in the browser or by the server.

export const STATUS_OPTIONS = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'IN_USE', label: 'In use' },
  { value: 'IN_REPAIR', label: 'In repair' },
  { value: 'RETIRED', label: 'Retired' },
];

export function statusLabel(value) {
  const option = STATUS_OPTIONS.find((o) => o.value === value);
  return option ? option.label : value;
}

const SERIAL_PATTERN = /^[A-Za-z0-9-]+$/;

// Removes extra spaces so " Dell " and "Dell" are treated the same.
export function cleanComputer(values) {
  return {
    name: (values.name ?? '').trim(),
    brand: (values.brand ?? '').trim(),
    serialNumber: (values.serialNumber ?? '').trim(),
    status: values.status ?? '',
  };
}

// Returns an object like { name: 'Name is required.' }.
// An empty object means the input is valid.
export function validateComputer(values) {
  const c = cleanComputer(values);
  const errors = {};

  if (!c.name) {
    errors.name = 'Name is required.';
  } else if (c.name.length < 2 || c.name.length > 50) {
    errors.name = 'Name must be 2-50 characters.';
  }

  if (!c.brand) {
    errors.brand = 'Brand is required.';
  } else if (c.brand.length < 2 || c.brand.length > 30) {
    errors.brand = 'Brand must be 2-30 characters.';
  }

  if (!c.serialNumber) {
    errors.serialNumber = 'Serial number is required.';
  } else if (c.serialNumber.length < 4 || c.serialNumber.length > 30) {
    errors.serialNumber = 'Serial number must be 4-30 characters.';
  } else if (!SERIAL_PATTERN.test(c.serialNumber)) {
    errors.serialNumber = 'Serial number can only contain letters, numbers and dashes.';
  }

  if (!STATUS_OPTIONS.some((o) => o.value === c.status)) {
    errors.status = 'Choose a valid status.';
  }

  return errors;
}

// Checks the text typed in the "Find by ID" box. Returns an error message or ''.
export function validateIdInput(text) {
  const trimmed = String(text ?? '').trim();
  if (!/^\d+$/.test(trimmed) || Number(trimmed) < 1) {
    return 'Enter a positive whole number ID.';
  }
  return '';
}
