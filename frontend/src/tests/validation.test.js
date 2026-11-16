import { describe, expect, it } from 'vitest';
import { cleanComputer, statusLabel, validateComputer, validateIdInput } from '../utils/validation.js';

const valid = { name: 'Lab PC', brand: 'Dell', serialNumber: 'DL-9000', status: 'AVAILABLE' };

describe('validateComputer', () => {
  it('accepts a valid computer', () => {
    expect(validateComputer(valid)).toEqual({});
  });

  it('requires every text field', () => {
    const errors = validateComputer({ name: ' ', brand: '', serialNumber: '', status: 'AVAILABLE' });
    expect(errors.name).toBe('Name is required.');
    expect(errors.brand).toBe('Brand is required.');
    expect(errors.serialNumber).toBe('Serial number is required.');
  });

  it('checks lengths', () => {
    const errors = validateComputer({ ...valid, name: 'A', brand: 'B', serialNumber: 'X1' });
    expect(errors.name).toMatch(/2-50/);
    expect(errors.brand).toMatch(/2-30/);
    expect(errors.serialNumber).toMatch(/4-30/);
  });

  it('only allows letters, numbers and dashes in the serial number', () => {
    expect(validateComputer({ ...valid, serialNumber: 'DL 9000!' }).serialNumber).toMatch(
      /letters, numbers and dashes/,
    );
  });

  it('rejects an unknown status', () => {
    expect(validateComputer({ ...valid, status: 'LOST' }).status).toBe('Choose a valid status.');
  });
});

describe('helpers', () => {
  it('trims spaces', () => {
    expect(cleanComputer({ ...valid, name: '  Lab PC  ' }).name).toBe('Lab PC');
  });

  it('turns status codes into labels', () => {
    expect(statusLabel('IN_REPAIR')).toBe('In repair');
  });

  it('validates the search ID', () => {
    expect(validateIdInput('3')).toBe('');
    expect(validateIdInput('0')).not.toBe('');
    expect(validateIdInput('abc')).not.toBe('');
    expect(validateIdInput('-2')).not.toBe('');
    expect(validateIdInput('')).not.toBe('');
  });
});
