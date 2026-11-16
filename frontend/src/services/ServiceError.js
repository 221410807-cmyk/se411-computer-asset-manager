// One error type for both data services, so the UI can show errors the same way.
// status: HTTP-like status code (0 = server could not be reached)
// fieldErrors: e.g. { name: 'Name is required.' } for validation problems
export default class ServiceError extends Error {
  constructor(message, status = 0, fieldErrors = {}) {
    super(message);
    this.name = 'ServiceError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}
