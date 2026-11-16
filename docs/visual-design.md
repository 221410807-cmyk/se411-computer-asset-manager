# Visual Design Description

The design is clean and calm. It is a working tool, so the table and the form are the heroes. There are no decorations that compete with the data.

## Layout

```
+---------------------------------------------------------------+
| ▣ Computer Asset Manager                    [Inventory] About  |  <- navy menu bar
+---------------------------------------------------------------+
| Inventory                         Standalone mode / API mode    |
| [ green or red feedback message                          ✕ ]   |
| +-------------------+  +------------------------------------+  |
| | Add a computer    |  | Find by ID  [ 3      ] [Search]    |  |
| | Name     [      ] |  +------------------------------------+  |
| | Brand    [      ] |  | All computers                      |  |
| | Serial # [      ] |  | ID  Name  Brand  Serial  Status  ⋯ |  |
| | Status   [  v   ] |  | ...                    [Edit][Del] |  |
| | [Add computer]    |  | ← Previous  Page 1 of 3  Next →  5v |  |
| +-------------------+  +------------------------------------+  |
+---------------------------------------------------------------+
```

- **Menu bar:** dark navy with the app name on the left and the two menu items on the right. The current page's item is filled blue.
- **Inventory page:** a 300 px form column on the left and the search card plus the list card on the right. Below 800 px wide, the columns stack (form first).
- **About page:** two equal cards: the team table and a short project description.

## Colours

| Use | Colour |
|---|---|
| Menu bar | Navy `#1e2a44` |
| Primary buttons, active menu item | Blue `#2563eb` |
| Page background | Light grey `#f4f6fa` |
| Cards | White with a light border `#e5e7eb` |
| Errors and Delete button text | Red `#dc2626` |

Status is shown as a coloured pill so it can be read at a glance:

| Status | Pill |
|---|---|
| Available | green |
| In use | blue |
| In repair | amber |
| Retired | grey |

The text label is always shown inside the pill, so colour is never the only signal.

## Typography

- The system font (San Francisco on a Mac). Page title 1.6 rem bold, card titles 1.1 rem.
- Serial numbers and student IDs use a monospace font so the characters line up.
- Table headers are small, uppercase and grey.

## Components and states

| Component | Behaviour |
|---|---|
| Form | One form for adding and editing. The title changes to "Edit computer #2" and a **Cancel** button appears. A wrong field gets a red border and a red message under it. |
| Feedback banner | Green for success ("Added "Lab Laptop" with ID 9."), red for errors. It can be closed with ✕. |
| Table rows | Each row has **Edit** and **Delete** buttons. Delete asks for confirmation first. |
| Find by ID | Shows the result as a one-row table, or "No computer found with ID 404.", or "Enter a positive whole number ID." |
| Pagination | Previous/Next are disabled on the first/last page. The middle text says "Page 1 of 3 (12 computers)". |
| Loading | "Loading computers…". When changing pages, the old rows stay visible but faded. |
| Empty | "No computers yet. Use the form to add the first one." |
| Error | A red message with the reason and a **Try again** button. |

## Accessibility

Every input has a label. Error messages are linked to their field with `aria-describedby` and fields are marked `aria-invalid`. Feedback uses `role="status"` (success) or `role="alert"` (error) so screen readers announce it. Buttons in each row have names like "Edit Finance Laptop 01". Keyboard focus is visible and moves to the Name field when editing starts.

## Screenshots

| Screen | File |
|---|---|
| Inventory with sample data (API mode) | `screenshots/inventory.png` |
| Validation messages (standalone mode) | `screenshots/validation.png` |
| Find by ID and duplicate-serial error | `screenshots/search-and-duplicate.png` |
| Server unreachable (error state) | `screenshots/error-state.png` |
| No computers (empty state) | `screenshots/empty-state.png` |
| About page | `screenshots/about.png` |

The API-mode screenshots were taken during verification against a stand-in server that copies the backend's API contract (see `verification-report.md`). The screens look the same with the real backend.
