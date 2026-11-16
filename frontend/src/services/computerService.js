// Picks which data service the app uses, based on the VITE_DATA_MODE setting.
//   npm run dev      -> .env      -> VITE_DATA_MODE=local -> sample data in the browser
//   npm run dev:api  -> .env.api  -> VITE_DATA_MODE=api   -> Spring Boot backend
import localComputerService from './localComputerService.js';
import { createApiComputerService } from './apiComputerService.js';

const mode = import.meta.env.VITE_DATA_MODE === 'api' ? 'api' : 'local';
const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const computerService = mode === 'api' ? createApiComputerService(apiUrl) : localComputerService;

export default computerService;
