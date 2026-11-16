// Sample data for the standalone version (Part 1).
// This is a plain collection of JavaScript objects - there is no database.
// Each computer has the same five fields the Spring Boot backend uses.
const sampleComputers = [
  { id: 1, name: 'Finance Laptop 01', brand: 'Dell', serialNumber: 'DL-1001', status: 'IN_USE' },
  { id: 2, name: 'Finance Laptop 02', brand: 'Dell', serialNumber: 'DL-1002', status: 'AVAILABLE' },
  { id: 3, name: 'Design Workstation', brand: 'Apple', serialNumber: 'AP-2001', status: 'IN_USE' },
  { id: 4, name: 'Reception Desktop', brand: 'HP', serialNumber: 'HP-3001', status: 'IN_REPAIR' },
  { id: 5, name: 'IT Admin Laptop', brand: 'Lenovo', serialNumber: 'LN-4001', status: 'IN_USE' },
  { id: 6, name: 'Training Room PC 1', brand: 'Acer', serialNumber: 'AC-5001', status: 'AVAILABLE' },
  { id: 7, name: 'Training Room PC 2', brand: 'Acer', serialNumber: 'AC-5002', status: 'AVAILABLE' },
  { id: 8, name: 'Old Sales Laptop', brand: 'Toshiba', serialNumber: 'TS-6001', status: 'RETIRED' },
];

export default sampleComputers;
