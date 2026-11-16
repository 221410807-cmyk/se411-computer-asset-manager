# Computer Asset Manager

SE411 Software Construction project (Fall 2026-27). A web app for managing a company's computers: view, add, edit, delete, find by ID, and page through the list.

- **Part 1:** a standalone React app. The data is a JavaScript array of objects. No backend needed.
- **Part 2:** the same React app connected to a Java Spring Boot REST server.

> ⚠️ **About `backend-reference/`:** this backend is a **reference implementation for learning and local verification**. The brief requires the team to **write the submitted backend by hand**. Write yours in a new `backend/` folder using [`docs/backend-implementation-guide.md`](docs/backend-implementation-guide.md). The root `.gitignore` keeps `backend-reference/` out of your public GitHub repository.

**Team:** _Name 1 (ID), Name 2 (ID), Name 3 (ID)_. Fill in here and in `frontend/src/data/team.js`.
**GitHub:** _add your public repository URL here_

---

## Architecture

```
 Browser (React + Vite, port 5173)                      Spring Boot server (port 8080)
┌───────────────────────────────────────┐              ┌──────────────────────────────────┐
│ NavBar: Inventory | About             │              │ ComputerController  /api/computers│
│ InventoryPage ─ ComputerForm          │  HTTP + JSON │   @Valid, status codes            │
│               ─ ComputerTable         │ ───────────► │ ComputerService                   │
│               ─ Pagination, Find by ID│ ◄─────────── │   rules: 404, 409, paging limits  │
│ AboutPage                             │              │ ComputerRepository                │
│        │                              │              │   in-memory TreeMap, 12 samples   │
│ computerService (VITE_DATA_MODE)      │              │ GlobalExceptionHandler → JSON errs│
│   ├─ local: localComputerService      │              └──────────────────────────────────┘
│   │        (JS array, Part 1)         │
│   └─ api:   apiComputerService ───────┘ (fetch, Part 2)
```

The pages never touch the data directly. They call a **service** with the same five methods (`list`, `getById`, `create`, `update`, `remove`). One setting chooses which service is used:

| Mode | Command | Data |
|---|---|---|
| Standalone (Part 1) | `npm run dev` | `localComputerService`: a JavaScript array in the browser |
| Integrated (Part 2) | `npm run dev:api` | `apiComputerService`: REST calls to Spring Boot |

So the standalone version keeps working after integration.

Data flow when adding a computer: the user fills the form → the browser checks the rules → `POST /api/computers` with JSON → Spring checks `@Valid` and the service rules → the repository saves it and gives it an id → `201 Created` with JSON → the table refreshes and a green message appears.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 19, React Router 7, Vite 8, plain CSS |
| Frontend tests | Vitest 5, React Testing Library, jsdom |
| Backend | Java 17+, Spring Boot 3.5.15 (Spring Web, Bean Validation), Maven |
| Backend tests | JUnit 5, Mockito, AssertJ, Spring MockMvc |
| Data | Part 1: JavaScript array. Part 2: in-memory repository (no database needed) |

## Features

- Menu with **Inventory** and **About** pages.
- Inventory table with ID, name, brand, serial number and a coloured status pill.
- **Add** and **edit** with one form, **delete** after confirmation.
- **Find by ID**.
- **Pagination:** 5 per page by default (5 or 10). In Part 2 the server does the paging.
- **Validation** in the browser and on the server, with the same messages: required fields, lengths, serial-number characters, unique serial number.
- **Clear feedback:** green success messages, red error messages, messages under each wrong field.
- **Loading, empty and error states** (with **Try again**) for the integrated version.
- **About page** with team names and student IDs.
- REST API with 5 routes and consistent JSON errors (400, 404, 409). See [`docs/api-reference.md`](docs/api-reference.md).

## Folder structure

```
computer-asset-manager/
├── README.md                      ← this file
├── .gitignore                     ← keeps node_modules, target and backend-reference out of Git
├── frontend/                      ← React app (Parts 1 and 2)
│   ├── package.json               ← scripts: dev, dev:api, build, test
│   ├── .env                       ← VITE_DATA_MODE=local (standalone)
│   ├── .env.api                   ← VITE_DATA_MODE=api + VITE_API_URL
│   ├── index.html, vite.config.js
│   └── src/
│       ├── main.jsx, App.jsx, App.css
│       ├── components/            ← NavBar, ComputerForm, ComputerTable, Pagination, FeedbackMessage
│       ├── pages/                 ← InventoryPage, AboutPage
│       ├── services/              ← computerService (picker), localComputerService, apiComputerService, ServiceError
│       ├── data/                  ← sampleComputers.js, team.js
│       ├── utils/validation.js    ← shared input rules
│       └── tests/                 ← 6 test files, 48 tests
├── backend-reference/             ← REFERENCE Spring Boot backend (Maven project)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/se411/assetmanager/
│       │   ├── AssetManagerApplication.java
│       │   ├── model/             ← Computer, ComputerStatus
│       │   ├── dto/               ← PageResponse, ErrorResponse
│       │   ├── repository/        ← ComputerRepository (in memory)
│       │   ├── service/           ← ComputerService
│       │   ├── controller/        ← ComputerController (REST routes)
│       │   ├── exception/         ← 3 exceptions + GlobalExceptionHandler
│       │   └── config/            ← SampleDataLoader (12 computers)
│       └── test/java/...          ← 5 test classes, 46 tests (JUnit 5 + Mockito)
├── scripts/smoke-test.sh          ← curl check of every route against a running backend
├── slides/SE411-Computer-Asset-Manager-Slides.pptx   ← the 2 presentation slides
└── docs/
    ├── requirements-specification.md
    ├── visual-design.md + screenshots/
    ├── api-reference.md
    ├── backend-implementation-guide.md
    ├── presentation-script.md     ← speaking scripts + demo checklist
    ├── requirements-checklist.md  ← brief requirement → file/test
    └── verification-report.md     ← what was tested and what to check on your Mac
```

## Prerequisites (Mac)

Check what you have in Terminal:

```bash
node -v      # need 22.22 or newer (24 LTS recommended)
java -version  # need 17 or newer (21 recommended)
mvn -v       # optional: only needed to run Maven from Terminal
```

Install what is missing (with [Homebrew](https://brew.sh)):

```bash
brew install node@24            # or download the LTS installer from nodejs.org
brew install --cask temurin@21  # Java 21 (Eclipse Temurin)
brew install maven              # optional; Eclipse has Maven built in
```

Eclipse: use **Eclipse IDE for Enterprise Java and Web Developers** (or "for Java Developers"). Both include Maven support.

## Run the standalone version (Part 1)

```bash
cd frontend
npm install       # first time only
npm run dev
```

Open <http://localhost:5173>. The badge says **Standalone mode**. Data resets when you reload the page.

## Run the backend

**Terminal:**

```bash
cd backend-reference      # or: cd backend   (your own hand-written version)
mvn spring-boot:run
```

Wait for `Started AssetManagerApplication`. Check <http://localhost:8080/api/computers?page=0&size=5> in the browser. Stop with `Ctrl + C`.

**Eclipse:** see "Eclipse setup" below.

## Run the integrated version (Part 2)

1. Start the backend (above) and leave it running.
2. In a second Terminal window:

   ```bash
   cd frontend
   npm run dev:api
   ```

3. Open <http://localhost:5173>. The badge says **Connected to the Spring Boot API (http://localhost:8080)**.

If you see "Cannot reach the server", the backend is not running. To use another backend address, edit `VITE_API_URL` in `frontend/.env.api`.

## Run the tests

```bash
# Frontend (Vitest): 48 tests
cd frontend
npm test

# Backend (JUnit 5 + Mockito): 46 tests
cd backend-reference      # or your own backend/
mvn test

# End-to-end smoke check (backend must be running)
bash scripts/smoke-test.sh
```

Expected: Vitest `48 passed`; Maven `Tests run: 46, Failures: 0, Errors: 0` and `BUILD SUCCESS`; smoke test `13 passed, 0 failed`.

## Eclipse setup (Mac)

1. **Add Java 21:** Eclipse > Settings > Java > Installed JREs > Add > Standard VM > Directory: `/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home` > Finish, then tick it.
2. **Import:** File > Import > Maven > **Existing Maven Projects** > Next > Root Directory: choose `backend-reference` (or your `backend`) > make sure `pom.xml` is ticked > Finish. Wait for the progress bar at the bottom right to finish downloading dependencies (a few minutes the first time).
3. If you see red errors after import: right-click the project > **Maven > Update Project…** > OK.
4. **Run the server:** open `src/main/java/com/se411/assetmanager/AssetManagerApplication.java` > right-click > **Run As > Java Application**. The Console shows `Started AssetManagerApplication`. Stop it with the red square in the Console.
5. **Run the tests:** right-click the project > **Run As > Maven test**, or right-click `src/test/java` > **Run As > JUnit Test** to see the green JUnit bar.
6. The React frontend is easiest to run from Terminal (`npm run dev` / `npm run dev:api`). Eclipse is only needed for the Java part.

## Publish to a public GitHub repository

1. Write your own backend in `backend/` (see the implementation guide). The root `.gitignore` already excludes `backend-reference/`, `node_modules/` and `target/`.
2. Fill in the team names and student IDs (`frontend/src/data/team.js` and the top of this README).
3. On <https://github.com/new>, create a **Public** repository called `se411-computer-asset-manager`. Do **not** add a README or .gitignore there.
4. In Terminal:

   ```bash
   cd computer-asset-manager
   git init
   git add .
   git status            # check: frontend/, backend/, docs/, slides/ listed; no node_modules, no backend-reference
   git commit -m "SE411 Computer Asset Manager: React frontend and Spring Boot backend"
   git branch -M main
   git remote add origin https://github.com/YOUR-USERNAME/se411-computer-asset-manager.git
   git push -u origin main
   ```

   GitHub asks for a password: use a **personal access token** (GitHub > Settings > Developer settings > Personal access tokens), not your account password. If you have the GitHub CLI, `gh repo create se411-computer-asset-manager --public --source=. --push` does steps 3-4 in one command.

5. Open the repository page in a private browser window to confirm it is public. Put the URL at the top of this README and commit again.

## What you still need to do

- [ ] Replace the placeholder names and student IDs in `frontend/src/data/team.js` and at the top of this README.
- [ ] Write your own backend in `backend/` (the brief requires hand-written backend code).
- [ ] Run `mvn test` and `bash scripts/smoke-test.sh` on your Mac (not possible in the build environment; see [`docs/verification-report.md`](docs/verification-report.md)).
- [ ] Publish to GitHub and add the URL.
- [ ] Practise the demo with [`docs/presentation-script.md`](docs/presentation-script.md).
