package com.se411.assetmanager.exception;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

/** Thrown when no computer has the requested id. The API answers 404 Not Found. */
public class ComputerNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ComputerNotFoundException(Long id) {
        super("Computer with ID " + id + " was not found.");
    }
}
