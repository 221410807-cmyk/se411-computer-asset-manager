package com.se411.assetmanager.exception;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

/** Thrown when page or size is out of range, e.g. page=-1 or size=0. The API answers 400 Bad Request. */
public class InvalidPageRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidPageRequestException(String message) {
        super(message);
    }
}
