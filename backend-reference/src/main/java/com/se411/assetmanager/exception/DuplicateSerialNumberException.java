package com.se411.assetmanager.exception;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

/** Thrown when another computer already has the same serial number. The API answers 409 Conflict. */
public class DuplicateSerialNumberException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateSerialNumberException(String serialNumber) {
        super("Serial number " + serialNumber + " is already used by another computer.");
    }
}
