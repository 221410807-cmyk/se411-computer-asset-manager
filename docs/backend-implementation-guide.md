# Backend Implementation Guide (organized by Java class)

> **Read this first.** The SE411 brief says the backend must be **coded manually** by the team. The code in `backend-reference/` was generated as a **reference implementation** for learning and for checking that the frontend works. Do not submit it as your backend. Use this guide to write your own version in a new `backend/` folder, then compare with the reference only when you are stuck.

## Suggested way to work

1. Create a new project at <https://start.spring.io>: Maven, Java 17, Spring Boot 3.5.x, group `com.se411`, artifact `computer-asset-manager-backend`, dependencies **Spring Web** and **Validation**. Unzip it as `backend/` next to `frontend/`. (Or copy only `pom.xml` from the reference.)
2. Import it into Eclipse (see README, "Eclipse setup").
3. Write the classes **in the order below**. After each class, write its test and run it. Split the classes between the three team members so each person can explain their part.
4. Run `bash scripts/smoke-test.sh` and the React app in API mode against **your** backend.

All classes live under `src/main/java/com/se411/assetmanager/`.

---

## 1. `model/ComputerStatus.java` (enum)

**Job:** the four allowed status values.

- Write `public enum ComputerStatus { AVAILABLE, IN_USE, IN_REPAIR, RETIRED }`.
- Jackson turns `"IN_USE"` in JSON into `ComputerStatus.IN_USE` automatically. An unknown value like `"LOST"` makes Spring throw `HttpMessageNotReadableException` (you handle it in class 9).

## 2. `model/Computer.java` (the data class)

**Job:** holds one computer and its input rules.

- Five private fields: `Long id`, `String name`, `String brand`, `String serialNumber`, `ComputerStatus status`.
- An **empty constructor** (Jackson needs it), a constructor with all fields, and getters/setters for every field.
- Validation annotations on the fields (from `jakarta.validation.constraints`):
  - `name`: `@NotBlank`, `@Size(min = 2, max = 50)`
  - `brand`: `@NotBlank`, `@Size(min = 2, max = 30)`
  - `serialNumber`: `@NotBlank`, `@Size(min = 4, max = 30)`, `@Pattern(regexp = "^[A-Za-z0-9-]+$")`
  - `status`: `@NotNull`
- Give every annotation a `message = "..."`. Use the same sentences as `frontend/src/utils/validation.js`, so users see the same text everywhere.
- **Explain it:** the annotations do nothing alone. They are checked when a controller parameter has `@Valid`.

## 3. `dto/PageResponse.java` (record)

**Job:** the JSON shape of one page.

- `public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}`
- A record creates the constructor and the accessor methods for you. Jackson turns it into JSON with those five field names. The React app reads exactly these names.

## 4. `dto/ErrorResponse.java` (record)

**Job:** the JSON shape of every error.

- `public record ErrorResponse(int status, String error, String message, Map<String, String> fieldErrors) {}`
- The React app shows `message` in the red banner and puts each `fieldErrors` entry under the matching input.

## 5. `exception/` (three small exception classes)

**Job:** name each kind of problem so the handler can choose the right status code.

| Class | Constructor message | Status (set in class 9) |
|---|---|---|
| `ComputerNotFoundException(Long id)` | `Computer with ID 99 was not found.` | 404 |
| `DuplicateSerialNumberException(String serial)` | `Serial number DL-1001 is already used by another computer.` | 409 |
| `InvalidPageRequestException(String message)` | e.g. `Size must be between 1 and 50.` | 400 |

- Each one `extends RuntimeException` and calls `super(message)`.
- Add `private static final long serialVersionUID = 1L;` to stop Eclipse's yellow warning.

## 6. `repository/ComputerRepository.java` (`@Repository`)

**Job:** store the computers. This version keeps them in memory.

- Fields: `private final Map<Long, Computer> computers = new TreeMap<>();` (a TreeMap keeps ids in order) and `private long nextId = 1;`.
- Methods (mark them `synchronized` so two requests cannot clash):
  - `save(Computer c)`: if `c.getId() == null`, give it `nextId` and increase `nextId`. Then `put` it in the map and return it.
  - `findById(Long id)`: `return Optional.ofNullable(computers.get(id));`
  - `existsById(Long id)`, `count()`, `deleteById(Long id)`
  - `findPage(int page, int size)`: copy the values into a list, compute `from = page * size`. If `from >= list.size()` return an empty list, otherwise return `subList(from, Math.min(from + size, list.size()))`.
  - `existsBySerialNumber(String serial, Long excludeId)`: loop over all computers. Return true if one has the same serial (`equalsIgnoreCase`) **and** a different id. `excludeId` lets a computer keep its own serial number during an update.
- **Optional upgrade:** with Spring Data JPA + H2 this class becomes an interface `extends JpaRepository<Computer, Long>`, and paging uses `PageRequest.of(page, size)`. The service and controller barely change.

## 7. `service/ComputerService.java` (`@Service`)

**Job:** the business rules. It does not know about HTTP.

- Constructor injection: `public ComputerService(ComputerRepository repository)`.
- `create(Computer c)`: trim the text fields; if the serial exists, throw `DuplicateSerialNumberException`; set `id` to `null` (the server chooses ids); `return repository.save(c)`.
- `getById(Long id)`: `return repository.findById(id).orElseThrow(() -> new ComputerNotFoundException(id));`
- `getPage(int page, int size)`: throw `InvalidPageRequestException` if `page < 0` or `size` is not 1-50. Then build `new PageResponse<>(repository.findPage(page, size), page, size, total, totalPages)` where `totalPages = (int) Math.ceil((double) total / size)`.
- `update(Long id, Computer changes)`: `getById(id)` (gives 404 for free), trim, check the serial with `existsBySerialNumber(serial, id)`, copy the four fields onto the existing object, save.
- `delete(Long id)`: if `!existsById(id)` throw `ComputerNotFoundException`; else `deleteById(id)`.

## 8. `controller/ComputerController.java` (`@RestController`)

**Job:** turn HTTP requests into service calls.

- Class annotations: `@RestController`, `@RequestMapping("/api/computers")`, `@CrossOrigin(origins = "http://localhost:5173")` (lets the React dev server call the API).
- Methods:

| Annotation | Signature | Returns |
|---|---|---|
| `@PostMapping` | `create(@Valid @RequestBody Computer c)` | `ResponseEntity.status(HttpStatus.CREATED).body(saved)` |
| `@GetMapping("/{id}")` | `getById(@PathVariable("id") Long id)` | the computer |
| `@GetMapping` | `getPage(@RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "5") int size)` | `PageResponse<Computer>` |
| `@PutMapping("/{id}")` | `update(@PathVariable("id") Long id, @Valid @RequestBody Computer c)` | the updated computer |
| `@DeleteMapping("/{id}")` | `delete(@PathVariable("id") Long id)` | `ResponseEntity.noContent().build()` |

- **Eclipse pitfall:** always write the names in `@PathVariable("id")` and `@RequestParam(name = "page")`. Without them, Spring 6 may fail with "Name for argument of type ... not specified" when Eclipse compiles the code.
- The controller has no `try/catch`. Exceptions go to class 9.

## 9. `exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`)

**Job:** one place that turns exceptions into JSON errors.

- One `@ExceptionHandler` method per exception, each returning `ResponseEntity<ErrorResponse>`:

| Exception | Status | Notes |
|---|---|---|
| `ComputerNotFoundException` | 404 | use `ex.getMessage()` |
| `DuplicateSerialNumberException` | 409 | `fieldErrors`: `serialNumber` → `This serial number is already in use.` |
| `InvalidPageRequestException` | 400 | |
| `MethodArgumentNotValidException` | 400 | loop over `ex.getBindingResult().getFieldErrors()` and put `field → defaultMessage` in a map. An empty field breaks both `@NotBlank` and `@Size`; prefer the `NotBlank` message (`error.getCode()` is `"NotBlank"`). |
| `HttpMessageNotReadableException` | 400 | broken JSON or an unknown status |
| `MethodArgumentTypeMismatchException` | 400 | `/api/computers/abc`; `ex.getName()` gives `"id"` |

- A small private `build(status, message, fieldErrors)` helper avoids repeating code.

## 10. `config/SampleDataLoader.java` (`@Component implements CommandLineRunner`)

**Job:** add 12 sample computers when the server starts, so pagination shows 3 pages.

- `run(String... args)` loops over a list of `new Computer(null, ...)` and calls `repository.save(...)`.
- Use unique serial numbers.

## 11. `AssetManagerApplication.java`

**Job:** starts Spring. `@SpringBootApplication` plus `SpringApplication.run(AssetManagerApplication.class, args);`. In Eclipse: right-click > **Run As > Java Application**.

---

## Tests to write (requirement 10: JUnit + Mockito)

All under `src/test/java/com/se411/assetmanager/`. The reference has 46 tests. Aim for at least one test per bullet.

| Test class | Style | What to cover |
|---|---|---|
| `service/ComputerServiceTest` | `@ExtendWith(MockitoExtension.class)`, `@Mock ComputerRepository`, `@InjectMocks ComputerService` | create OK + trims + id reset; create duplicate → exception and `verify(repository, never()).save(any())`; getById found/missing; getPage first, last partial, past end, empty, size 1 and 50 allowed, page -1 / size 0 / size 51 rejected; update OK, keeps own serial, missing, duplicate; delete OK and missing |
| `controller/ComputerControllerTest` | Mockito mock of the service + `MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler())` | each route's success status (201/200/204); 400 for invalid fields, broken JSON, unknown status, `abc` id, `page=x`, `size=0`; 404 for missing ids; 409 duplicate. Check JSON with `jsonPath("$.fieldErrors.name")` |
| `repository/ComputerRepositoryTest` | plain JUnit, no mocks | ids increase; save replaces; findPage slices; past end is empty; delete; serial check ignores case and excludeId |
| `config/SampleDataLoaderTest` | Mockito | `verify(repository, times(12)).save(any(Computer.class))` |
| `AssetManagerApplicationTests` | `@SpringBootTest` + `@AutoConfigureMockMvc` | app starts; page 2 has 2 items; full add/find/update/delete flow |

Run them with `mvn test`, or in Eclipse: right-click the project > **Run As > Maven test** (or right-click a test class > **Run As > JUnit Test**).

## Checklist before you submit

- [ ] Every class above is written by your team, in `backend/`, not copied.
- [ ] `mvn test` passes.
- [ ] `bash scripts/smoke-test.sh` passes against your backend.
- [ ] `npm run dev:api` shows your data, and paging, search, add, edit and delete work.
- [ ] Each team member can explain at least one class and its test.
