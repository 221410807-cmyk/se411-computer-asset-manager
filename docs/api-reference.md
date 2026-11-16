# REST API Reference

Base URL: `http://localhost:8080`. All bodies are JSON (`Content-Type: application/json`).

## Summary

| HTTP method | Route | Purpose | Request | Response / status |
|---|---|---|---|---|
| POST | `/api/computers` | Add a computer (req. 6) | Body: `name`, `brand`, `serialNumber`, `status` | **201** + new computer with `id` · 400 invalid · 409 duplicate serial |
| GET | `/api/computers/{id}` | Find a computer by ID (req. 7) | Path: `id` | **200** + computer · 404 not found · 400 id not a number |
| GET | `/api/computers?page=0&size=5` | List all computers, one page at a time (req. 8) | Query: `page` (default 0), `size` (default 5, max 50) | **200** + page object · 400 bad page/size |
| PUT | `/api/computers/{id}` | Update a computer (req. 9) | Path: `id`. Body: `name`, `brand`, `serialNumber`, `status` | **200** + updated computer · 400 · 404 · 409 |
| DELETE | `/api/computers/{id}` | Delete a computer | Path: `id` | **204** no body · 404 not found |

## The computer object

```json
{ "id": 1, "name": "Finance Laptop 01", "brand": "Dell", "serialNumber": "DL-1001", "status": "IN_USE" }
```

`status` must be `AVAILABLE`, `IN_USE`, `IN_REPAIR` or `RETIRED`. An `id` sent in a request body is ignored: the server chooses ids for new computers, and PUT uses the id from the URL.

## The page object

```json
{
  "content": [ { "id": 11, "...": "..." }, { "id": 12, "...": "..." } ],
  "page": 2,
  "size": 5,
  "totalElements": 12,
  "totalPages": 3
}
```

Pages start at 0. A page after the last one returns `"content": []` with status 200.

## The error object

Every error has the same shape:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed. Please fix the highlighted fields.",
  "fieldErrors": { "name": "Name is required.", "serialNumber": "Serial number must be 4-30 characters." }
}
```

`fieldErrors` is `{}` when the problem is not about a particular field.

| Status | When | Example `message` |
|---|---|---|
| 400 | A field breaks a rule | `Validation failed. Please fix the highlighted fields.` |
| 400 | Body is not JSON, or `status` is unknown | `The request body is missing or is not valid JSON. ...` |
| 400 | `id` or `page` is not a number | `Parameter 'id' must be a whole number.` |
| 400 | `page < 0`, or `size` is not 1-50 | `Size must be between 1 and 50.` |
| 404 | No computer has that id | `Computer with ID 99 was not found.` |
| 409 | Serial number already used | `Serial number DL-1001 is already used by another computer.` |

## Try it with curl

```bash
# List page 0 (5 per page)
curl "http://localhost:8080/api/computers?page=0&size=5"

# Find by id
curl http://localhost:8080/api/computers/3

# Add
curl -X POST http://localhost:8080/api/computers \
  -H "Content-Type: application/json" \
  -d '{"name":"Lab Laptop","brand":"Asus","serialNumber":"AS-7001","status":"AVAILABLE"}'

# Update
curl -X PUT http://localhost:8080/api/computers/13 \
  -H "Content-Type: application/json" \
  -d '{"name":"Lab Laptop","brand":"Asus","serialNumber":"AS-7001","status":"IN_REPAIR"}'

# Delete
curl -X DELETE http://localhost:8080/api/computers/13
```

`scripts/smoke-test.sh` runs all of these and checks the status codes.
