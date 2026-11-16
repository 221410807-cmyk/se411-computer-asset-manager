package com.se411.assetmanager.dto;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import java.util.List;

/**
 * One page of results, returned by GET /api/computers?page=0&size=5.
 *
 * A Java "record" is a short way to write a class that only holds data:
 * Java creates the constructor and the getters for us.
 *
 * JSON example:
 * { "content": [ ... ], "page": 0, "size": 5, "totalElements": 12, "totalPages": 3 }
 *
 * @param content       the items on this page
 * @param page          the page number, starting at 0
 * @param size          how many items fit on one page
 * @param totalElements how many items exist in total
 * @param totalPages    how many pages exist in total
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
