package com.se411.assetmanager.model;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A company computer. This is the only asset type the app manages.
 *
 * The annotations are the input rules. When a controller method uses @Valid,
 * Spring checks these rules and returns 400 Bad Request if one is broken.
 * The React frontend uses the same rules and messages.
 *
 * JSON example:
 * { "id": 1, "name": "Finance Laptop 01", "brand": "Dell", "serialNumber": "DL-1001", "status": "IN_USE" }
 */
public class Computer {

    // Set by the server. Any id sent by the client in the request body is ignored.
    private Long id;

    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 50, message = "Name must be 2-50 characters.")
    private String name;

    @NotBlank(message = "Brand is required.")
    @Size(min = 2, max = 30, message = "Brand must be 2-30 characters.")
    private String brand;

    @NotBlank(message = "Serial number is required.")
    @Size(min = 4, max = 30, message = "Serial number must be 4-30 characters.")
    @Pattern(regexp = "^[A-Za-z0-9-]+$",
             message = "Serial number can only contain letters, numbers and dashes.")
    private String serialNumber;

    @NotNull(message = "Status is required.")
    private ComputerStatus status;

    /** Empty constructor: Jackson needs it to turn JSON into a Computer. */
    public Computer() {
    }

    public Computer(Long id, String name, String brand, String serialNumber, ComputerStatus status) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.serialNumber = serialNumber;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public ComputerStatus getStatus() {
        return status;
    }

    public void setStatus(ComputerStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Computer{id=" + id + ", name='" + name + "', brand='" + brand
                + "', serialNumber='" + serialNumber + "', status=" + status + "}";
    }
}
