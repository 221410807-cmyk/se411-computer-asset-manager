# Requirements Checklist

Each numbered requirement from the SE411 project brief, mapped to the file, feature or test that satisfies it. Paths are relative to the project root.

## Part 1

| # | Requirement | Where it is satisfied | Proof (test) |
|---|---|---|---|
| 1 | The project must be a React project. | `frontend/package.json` (React 19, Vite), `frontend/src/main.jsx`, `frontend/src/App.jsx` | `npm run build` and `npm test` (48 tests) |
| 2 | A menu to navigate between features. | `frontend/src/components/NavBar.jsx` (Inventory, About), routes in `frontend/src/App.jsx` | `src/tests/App.test.jsx`: Inventory first, open About, back to Inventory, unknown URL |
| 3 | Manage data: add, remove, update, display. Data in a collection of objects. | Data: `frontend/src/data/sampleComputers.js` (array of objects). Logic: `frontend/src/services/localComputerService.js`. UI: `frontend/src/pages/InventoryPage.jsx`, `components/ComputerForm.jsx`, `components/ComputerTable.jsx`, `components/Pagination.jsx`. Validation: `frontend/src/utils/validation.js` | `src/tests/InventoryPage.test.jsx` (display, paging, add, invalid input, duplicate, edit, cancel, delete, cancel delete, search), `src/tests/localComputerService.test.js`, `src/tests/validation.test.js` |
| 4 | About page with team names and student IDs, opened from a menu item. | `frontend/src/pages/AboutPage.jsx`, data in `frontend/src/data/team.js` | `src/tests/App.test.jsx`: "opens the About page from the menu and shows names and student IDs". **Team must replace the placeholders.** |

## Part 2

| # | Requirement | Where it is satisfied | Proof (test) |
|---|---|---|---|
| 5 | The Java application is a Spring server. | `backend-reference/pom.xml` (Spring Boot 3.5.15, Maven), `AssetManagerApplication.java` | `AssetManagerApplicationTests` (context starts). **Team writes its own copy in `backend/`.** |
| 6 | The user can add an element. | `POST /api/computers` in `ComputerController.create` → `ComputerService.create`. Frontend: form + `apiComputerService.create` | `ComputerControllerTest.createReturns201`, `createRejectsInvalidFields`, `createDuplicateReturns409`; `ComputerServiceTest.createSavesTrimmedComputer`; frontend `apiComputerService.test.js` (POST) |
| 7 | The user can search for an element by id. | `GET /api/computers/{id}` in `ComputerController.getById`. Frontend: **Find by ID** card in `InventoryPage.jsx` | `ComputerControllerTest.getByIdReturns200`, `getByIdMissingReturns404`, `getByIdNotANumberReturns400`; `ComputerServiceTest.getByIdFound/Missing`; frontend "finds a computer by ID", "says when no computer has that ID" |
| 8 | Search all elements and display paginated results. | `GET /api/computers?page=0&size=5` in `ComputerController.getPage` → `ComputerService.getPage` → `ComputerRepository.findPage`. 12 sample records in `SampleDataLoader`. Frontend: `Pagination.jsx` | `ComputerServiceTest` (first, last partial, past end, empty, size limits, bad page/size); `ComputerRepositoryTest.findPageSlices`; `AssetManagerApplicationTests.sampleDataSupportsPagination`; frontend `InventoryStates.test.jsx` "asks the server for the next page" |
| 9 | The user can update an element. | `PUT /api/computers/{id}` in `ComputerController.update` → `ComputerService.update`. Frontend: **Edit** button + form | `ComputerControllerTest.updateReturns200/updateMissingReturns404/updateInvalidReturns400`; `ComputerServiceTest.update*`; frontend "edits a computer" |
| 10 | A JUnit + Mockito unit test suite for all backend code. | `backend-reference/src/test/java/...`: `ComputerServiceTest` (17), `ComputerControllerTest` (17), `ComputerRepositoryTest` (7), `SampleDataLoaderTest` (2), `AssetManagerApplicationTests` (3) = 46 tests | `mvn test`. **Not run in this session (see verification report). Run it on your Mac.** |

## Submission items from the brief

| Item | Where |
|---|---|
| AI-generated requirements specification | `docs/requirements-specification.md` |
| AI-generated visual design | `docs/visual-design.md` + `docs/screenshots/` |
| Documentation: architecture, technology stack, features | `README.md` |
| AI-generated tests, run later | Frontend: `frontend/src/tests/` (48 tests, run). Backend: see requirement 10 |
| Code on a public GitHub repository | `README.md`, "Publish to GitHub". **Team must do this and add the URL.** |
| Presentation: overall architecture design | Slide 1 of `slides/SE411-Computer-Asset-Manager-Slides.pptx` |
| Presentation: REST API route table | Slide 2 (columns: HTTP method, route, purpose, request, response/status) |
| Only two slides | The deck has exactly 2 slides |
| Demo, preferably packaged as a Maven project | `backend-reference/pom.xml` (your `backend/pom.xml`); demo steps in `docs/presentation-script.md` |
| Back end coded manually | **Team responsibility.** `docs/backend-implementation-guide.md` explains each class. `.gitignore` keeps `backend-reference/` out of the public repo |

## Extra items you asked for

| Item | Where |
|---|---|
| One asset type with id, name, brand, serialNumber, status | `sampleComputers.js`, `Computer.java` |
| Input validation and clear feedback | `validation.js`, `ComputerForm.jsx`, `FeedbackMessage.jsx`, `Computer.java` annotations, `GlobalExceptionHandler.java` |
| Standalone version still runs after integration | `npm run dev` (local mode) vs `npm run dev:api`, chosen in `services/computerService.js` |
| Loading, empty and error states | `InventoryPage.jsx`; tested in `InventoryStates.test.jsx`; screenshots in `docs/screenshots/` |
| Missing IDs and invalid requests handled | `GlobalExceptionHandler.java` (404, 400, 409) |
| Documented endpoints | `docs/api-reference.md` |
| Reference label + per-class guide | Header comments in every backend file; `docs/backend-implementation-guide.md` |
| End-to-end smoke check | `scripts/smoke-test.sh` |
| Speaking script and demo checklist | `docs/presentation-script.md` |
