package com.se411.assetmanager.dto;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import java.util.Map;

/**
 * The JSON body sent back when something goes wrong. Every error uses this same shape,
 * so the React app can always read "message" and "fieldErrors".
 *
 * JSON example:
 * { "status": 400, "error": "Bad Request", "message": "Validation failed. Please fix the highlighted fields.",
 *   "fieldErrors": { "name": "Name is required." } }
 *
 * @param status      the HTTP status code, e.g. 404
 * @param error       the HTTP status name, e.g. "Not Found"
 * @param message     a sentence the user can read
 * @param fieldErrors one message per invalid field (empty when not a field problem)
 */
public record ErrorResponse(int status, String error, String message, Map<String, String> fieldErrors) {
}
