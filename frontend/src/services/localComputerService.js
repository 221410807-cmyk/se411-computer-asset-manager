// STANDALONE data service (Part 1).
// Keeps computers in a JavaScript array in memory. No backend, no database.
// It returns Promises and the same page shape as the Spring Boot API, so the
// Inventory page works the same way with either service.
import sampleComputers from '../data/sampleComputers.js';
import ServiceError from './ServiceError.js';
import { cleanComputer, validateComputer } from '../utils/validation.js';

export function createLocalComputerService(initialComputers = sampleComputers) {
  // Copy the sample data so changes never modify the original array.
  let computers = initialComputers.map((c) => ({ ...c }));
  let nextId = computers.reduce((max, c) => Math.max(max, c.id), 0) + 1;

  function findIndex(id) {
    return computers.findIndex((c) => c.id === Number(id));
  }

  function notFound(id) {
    return new ServiceError(`Computer with ID ${id} was not found.`, 404);
  }

  function check(values, ignoreId = null) {
    const errors = validateComputer(values);
    if (Object.keys(errors).length > 0) {
      throw new ServiceError('Please fix the highlighted fields.', 400, errors);
    }
    const clean = cleanComputer(values);
    const duplicate = computers.some(
      (c) => c.serialNumber.toLowerCase() === clean.serialNumber.toLowerCase() && c.id !== ignoreId,
    );
    if (duplicate) {
      throw new ServiceError(
        `Serial number ${clean.serialNumber} is already used by another computer.`,
        409,
        { serialNumber: 'This serial number is already in use.' },
      );
    }
    return clean;
  }

  return {
    mode: 'local',

    // Returns one page: { content, page, size, totalElements, totalPages }
    async list(page = 0, size = 5) {
      const sorted = [...computers].sort((a, b) => a.id - b.id);
      const start = page * size;
      return {
        content: sorted.slice(start, start + size).map((c) => ({ ...c })),
        page,
        size,
        totalElements: sorted.length,
        totalPages: Math.ceil(sorted.length / size),
      };
    },

    async getById(id) {
      const index = findIndex(id);
      if (index === -1) throw notFound(id);
      return { ...computers[index] };
    },

    async create(values) {
      const clean = check(values);
      const created = { id: nextId, ...clean };
      nextId += 1;
      computers.push(created);
      return { ...created };
    },

    async update(id, values) {
      const index = findIndex(id);
      if (index === -1) throw notFound(id);
      const clean = check(values, Number(id));
      computers[index] = { id: Number(id), ...clean };
      return { ...computers[index] };
    },

    async remove(id) {
      const index = findIndex(id);
      if (index === -1) throw notFound(id);
      computers.splice(index, 1);
    },
  };
}

// One shared instance, so data survives moving between the Inventory and About pages.
const localComputerService = createLocalComputerService();
export default localComputerService;
