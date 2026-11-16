package com.se411.assetmanager.model;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

/**
 * The only allowed values for a computer's status.
 * In JSON they are written exactly like this, e.g. "status": "IN_USE".
 */
public enum ComputerStatus {
    AVAILABLE,
    IN_USE,
    IN_REPAIR,
    RETIRED
}
