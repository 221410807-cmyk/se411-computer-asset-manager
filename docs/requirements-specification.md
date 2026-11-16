# Requirements Specification: Computer Asset Manager

SE411 Software Construction, Fall 2026-27. Version 1.0.

## 1. Purpose

Computer Asset Manager helps a company keep track of its computers. Staff can see every computer, add new ones, change their details, remove old ones and find a computer by its ID.

The project has two parts:

- **Part 1:** a standalone React app. Data lives in a JavaScript array. No backend, no database.
- **Part 2:** the same React app connected to a Java Spring Boot REST server.

## 2. Asset type and data model

The app manages one asset type: **company computers**.

| Field | Type | Rules |
|---|---|---|
| `id` | whole number | Set by the system. Unique. Cannot be changed by the user. |
| `name` | text | Required. 2-50 characters. |
| `brand` | text | Required. 2-30 characters. |
| `serialNumber` | text | Required. 4-30 characters. Only letters, numbers and dashes. Unique (case is ignored). |
| `status` | choice | Required. One of `AVAILABLE`, `IN_USE`, `IN_REPAIR`, `RETIRED`. |

Spaces at the start and end of text fields are removed before saving.

## 3. Functional requirements

| ID | Requirement | Part |
|---|---|---|
| FR-1 | The app has a menu with **Inventory** and **About** items. Clicking a menu item opens that page. | 1 |
| FR-2 | The Inventory page displays computers in a table (ID, name, brand, serial number, status). | 1 |
| FR-3 | The user can add a computer using a form. | 1 + 2 |
| FR-4 | The user can edit a computer. The form is filled with the current values. | 1 + 2 |
| FR-5 | The user can delete a computer after confirming. | 1 + 2 |
| FR-6 | The user can find one computer by entering its ID. | 1 + 2 |
| FR-7 | The list is paginated: 5 computers per page by default, with Previous/Next buttons and a 5/10 page-size choice. In Part 2 the server does the paging. | 1 + 2 |
| FR-8 | Invalid input is rejected with a message next to each wrong field. Nothing is saved. | 1 + 2 |
| FR-9 | Every action shows clear feedback: a green message on success, a red message on failure. | 1 + 2 |
| FR-10 | The About page shows each team member's name and student ID. | 1 |
| FR-11 | In Part 2 the list shows a loading message while waiting, an empty message when there are no computers, and an error message with a **Try again** button when the server cannot be reached. | 2 |
| FR-12 | The Spring server offers a REST API: add, find by ID, list with pagination, update and delete (see `api-reference.md`). | 2 |
| FR-13 | The server answers 404 for unknown IDs, 400 for invalid input or paging values, and 409 for a duplicate serial number, always with a JSON error body. | 2 |
| FR-14 | The server starts with 12 sample computers so pagination can be demonstrated (3 pages of 5). | 2 |

## 4. Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-1 | Part 1 runs with only Node.js installed (`npm install`, `npm run dev`). |
| NFR-2 | Part 2's server is a Maven project that imports into Eclipse and runs with `mvn spring-boot:run`. |
| NFR-3 | The code is short and commented so undergraduate students can read and explain it. |
| NFR-4 | Frontend tests (Vitest + React Testing Library) cover the main operations and navigation. |
| NFR-5 | Backend tests (JUnit 5 + Mockito) cover every backend class: successful operations, missing records, validation and pagination limits. |
| NFR-6 | The standalone version keeps working after the backend integration. A setting (`VITE_DATA_MODE`) chooses the mode. |
| NFR-7 | Forms are usable with a keyboard and screen reader: labels on every input, error messages linked to their fields. |

## 5. Constraints and assumptions

- Data in Part 1 is kept in browser memory. It resets when the page is reloaded.
- The reference backend keeps data in server memory. It resets to the 12 sample records on restart. No database is required by the brief.
- No login or user accounts. All users can do everything.
- The brief requires the **team to write the submitted backend by hand**. The backend in `backend-reference/` is for learning and checking only.

## 6. Out of scope

Login, roles, a real database, assigning computers to employees, and search by name or brand.
