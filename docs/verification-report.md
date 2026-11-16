# Verification Report

What was actually run while building this package, what passed, and what you still need to check on your Mac.

Build environment: Linux, Node.js 22.23, OpenJDK 21, Maven 3.9.11. **Maven Central was blocked by the build environment's network policy**, so no Spring Boot, JUnit or Mockito libraries could be downloaded. Because of that, `mvn test` and `mvn spring-boot:run` were **not** run.

## Commands run and results

| # | Command (from the project root) | Result |
|---|---|---|
| 1 | `cd frontend && npm install` | OK, 0 vulnerabilities |
| 2 | `cd frontend && npm test` (Vitest) | **6 test files, 48 tests passed**, no warnings |
| 3 | `cd frontend && npm run build` (standalone) | OK |
| 4 | `cd frontend && npm run build:api` (integrated) | OK, bundle contains `http://localhost:8080` |
| 5 | Browser smoke test, standalone mode (`npm run dev`, headless Chromium with Playwright) | OK: 8 sample computers, validation messages, add, data kept when moving to About and back |
| 6 | Browser smoke test, API mode (`npm run dev:api`) against a **stand-in server** that copies the backend's routes, JSON shapes and status codes | OK: list page 1 of 3, Next to page 3, add (ID 13), 409 duplicate shown under the field, find by ID 13 and 999, edit, delete, About page. Requests seen: `GET ?page=0/1/2&size=5`, `POST`, `GET /13`, `GET /999`, `PUT /13`, `DELETE /13` |
| 7 | Same, with the stand-in server stopped and then started empty | Error state with **Try again**, then empty state after retry: OK |
| 8 | `bash scripts/smoke-test.sh` against the stand-in server | **13 passed, 0 failed** |
| 9 | `javac --release 17` of all 13 backend main classes against minimal stand-in Spring/Jakarta API classes | Compiles. This checks our Java syntax and types only, not Spring itself |
| 10 | A plain-Java harness running the **real** repository, service, controller and exception-handler code (sample loader → paging → create → duplicate → update → delete → 404 → error bodies) | **20 checks passed** |
| 11 | `python validate.py` on the slide deck + render to images | Valid, 2 slides, checked visually |

The stand-in server in rows 6-8 was a short Node.js test script used only during verification. It is not part of the package, so the only backend you get is the Spring one.

## Not verified here: please check on your Mac

| What | Why it was not run | How to check |
|---|---|---|
| Backend tests (46 JUnit + Mockito tests) compile and pass | Maven Central blocked | `cd backend-reference && mvn test` → expect `Tests run: 46, Failures: 0, Errors: 0` |
| The Spring server starts and serves the API | Same | `mvn spring-boot:run`, then open <http://localhost:8080/api/computers> |
| Real end-to-end check against Spring | Same | With the server running: `bash scripts/smoke-test.sh` → expect `13 passed, 0 failed` |
| React + real Spring (CORS, JSON) | Same | `cd frontend && npm run dev:api`, then follow `docs/presentation-script.md` → Demo checklist |
| Eclipse import and run | No Eclipse here | README → "Eclipse setup" |
| Node version on your Mac | | `node -v` must be 22.22+ or 24.15+ |

## If something fails on your Mac

- **`mvn test` fails with a Mockito / "Could not initialize inline Byte Buddy mock maker" error:** you are probably on a very new JDK. Use JDK 21 (see README) and run again.
- **A compile error in a test file:** send me the error text. The test files could not be compiled here.
- **`Port 8080 was already in use`:** run `lsof -i :8080`, then `kill <PID>`.
- **React shows "Cannot reach the server":** the backend is not running, or it is on another port. Check `frontend/.env.api`.
- **`npm install` complains about the engine:** update Node.js (README → Prerequisites).
