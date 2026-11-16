# Presentation Script and Demo Checklist

Slides: `slides/SE411-Computer-Asset-Manager-Slides.pptx` (exactly two slides). Each script takes about 1 to 1.5 minutes to say. Split the parts between the three team members.

Before presenting: if your own backend has a different number of tests than the reference (46), update the number on slide 1 and in the script below.

## Slide 1: Architecture and data flow (about 75 seconds)

> Our project is the Computer Asset Manager. It helps a company keep track of its computers. Each computer has an ID, a name, a brand, a serial number and a status.
>
> On the left is the browser side. It's a React app built with Vite. The menu uses React Router and has two pages, Inventory and About. The Inventory page is made of small components: the form, the table, the pagination bar and the Find by ID box.
>
> The pages never talk to the data directly. They go through one file, computerService, and a setting decides which version it uses. In Part 1, the standalone version, the data is a JavaScript array of objects in the browser. In Part 2, the integrated version, the same pages call our Spring Boot server with fetch. That's why the standalone version still works after integration.
>
> On the right is the Spring Boot server on port 8080. It has three layers. The controller receives the HTTP request and checks the input with @Valid. The service holds the rules, like "this ID doesn't exist" or "this serial number is already used". The repository stores the computers. It starts with twelve sample computers so we can show pagination. If anything goes wrong, the Global Exception Handler turns it into a clear JSON error.
>
> At the bottom you can see what happens when we add a computer. The user fills the form, and the browser checks the rules first. Then React sends a POST request with JSON. The server checks it again, saves it and gives it a new ID. It answers 201 Created, and the table refreshes.
>
> We tested both sides. The frontend has 48 tests with Vitest. The backend has 46 JUnit and Mockito tests.

## Slide 2: REST API routes (about 60 seconds)

> This table shows our REST API. All routes start with /api/computers.
>
> POST adds a computer. That's requirement 6. We send the four fields as JSON. The server answers 201 Created with the new ID. It answers 400 if a field is invalid and 409 if the serial number is already used.
>
> GET with an ID finds one computer. That's requirement 7. We get 200 with the computer, or 404 if the ID doesn't exist.
>
> GET with page and size lists all computers one page at a time. That's requirement 8. The answer has the items on that page plus the total number of items and pages, so React can draw the Previous and Next buttons. Pages start at zero and the default size is five.
>
> PUT updates a computer. That's requirement 9. And DELETE removes one and answers 204 No Content.
>
> At the bottom: every error uses the same JSON shape. For validation errors, fieldErrors holds one message per field, and React shows each message right under that input.
>
> Now we'll show it working.

## Demo checklist

**Before the demo**

- [ ] Backend running: `cd backend && mvn spring-boot:run` (or Run As > Java Application in Eclipse). Use your own backend. The reference backend is only for checking.
- [ ] Frontend running in API mode: `cd frontend && npm run dev:api`, then open <http://localhost:5173>.
- [ ] The badge under "Inventory" says **Connected to the Spring Boot API**.
- [ ] Restart the backend just before presenting so the sample data is fresh (12 computers).

**1. Show the list and pagination (requirement 8)**

- [ ] Point at "Page 1 of 3 (12 computers)". Five rows are shown.
- [ ] Click **Next** twice. Page 3 shows two computers (IDs 11 and 12). Next is disabled.
- [ ] Optional: change **Per page** to 10. It shows "Page 1 of 2".
- [ ] Say: "Each click sends `GET /api/computers?page=…&size=…`, so the server does the paging."

**2. Add a computer (requirement 6)**

- [ ] First click **Add computer** with empty fields. Red messages appear: "Name is required." etc.
- [ ] Fill in: Name `Demo Laptop`, Brand `Asus`, Serial number `AS-7001`, Status `In use`. Click **Add computer**.
- [ ] Green message: **Added "Demo Laptop" with ID 13.** The list jumps to the last page and shows it.
- [ ] Optional: try serial `DL-1001` to show the 409 duplicate message.

**3. Find it by ID (requirement 7)**

- [ ] Type `13` in **Find by ID** and click **Search**. One row with Demo Laptop appears.
- [ ] Type `999` and search: "No computer found with ID 999." (404 from the server.)

**4. Update it (requirement 9)**

- [ ] Search `13` again. Click **Edit** on the result. The form title says "Edit computer #13".
- [ ] Change Status to `In repair` and click **Save changes**.
- [ ] Green message: **Saved changes to "Demo Laptop" (ID 13).** The status pill turns amber.

**5. Delete it**

- [ ] Click **Delete** on the Demo Laptop row and confirm.
- [ ] Green message: **Deleted "Demo Laptop" (ID 13).** The total goes back to 12 computers.
- [ ] Search `13` again: "No computer found with ID 13."

**6. About page (requirement 4)**

- [ ] Click **About** in the menu. It shows each team member's name and student ID.

**7. Tests (requirement 10), if there is time**

- [ ] `cd backend && mvn test` shows the JUnit/Mockito results (Tests run: …, Failures: 0).
- [ ] `cd frontend && npm test` shows the Vitest results.

**Backup plan:** if the backend fails during the demo, stop it and refresh the page to show the red error state with **Try again**. Then run `npm run dev` (standalone mode) to show the same features with sample data.
