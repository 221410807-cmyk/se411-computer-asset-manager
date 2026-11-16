package com.se411.assetmanager.service;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import java.util.List;

import org.springframework.stereotype.Service;

import com.se411.assetmanager.dto.PageResponse;
import com.se411.assetmanager.exception.ComputerNotFoundException;
import com.se411.assetmanager.exception.DuplicateSerialNumberException;
import com.se411.assetmanager.exception.InvalidPageRequestException;
import com.se411.assetmanager.model.Computer;
import com.se411.assetmanager.repository.ComputerRepository;

/**
 * The business rules for computers. The controller calls this class;
 * this class calls the repository. It does not know anything about HTTP.
 *
 * Rules:
 *  - a missing id throws ComputerNotFoundException (404)
 *  - two computers cannot share a serial number (409)
 *  - page must be 0 or more, size must be 1..50 (400)
 */
@Service
public class ComputerService {

    public static final int MAX_PAGE_SIZE = 50;

    private final ComputerRepository repository;

    // Constructor injection: Spring passes in the repository automatically.
    public ComputerService(ComputerRepository repository) {
        this.repository = repository;
    }

    /** Requirement 6: add a computer. */
    public Computer create(Computer computer) {
        trimText(computer);
        if (repository.existsBySerialNumber(computer.getSerialNumber(), null)) {
            throw new DuplicateSerialNumberException(computer.getSerialNumber());
        }
        computer.setId(null); // the server always chooses the id
        return repository.save(computer);
    }

    /** Requirement 7: find one computer by id. */
    public Computer getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ComputerNotFoundException(id));
    }

    /** Requirement 8: list all computers, one page at a time. */
    public PageResponse<Computer> getPage(int page, int size) {
        if (page < 0) {
            throw new InvalidPageRequestException("Page must be 0 or greater.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPageRequestException("Size must be between 1 and " + MAX_PAGE_SIZE + ".");
        }
        List<Computer> content = repository.findPage(page, size);
        long totalElements = repository.count();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }

    /** Requirement 9: update a computer. The id comes from the URL, not the body. */
    public Computer update(Long id, Computer changes) {
        Computer existing = getById(id); // throws 404 if missing
        trimText(changes);
        if (repository.existsBySerialNumber(changes.getSerialNumber(), id)) {
            throw new DuplicateSerialNumberException(changes.getSerialNumber());
        }
        existing.setName(changes.getName());
        existing.setBrand(changes.getBrand());
        existing.setSerialNumber(changes.getSerialNumber());
        existing.setStatus(changes.getStatus());
        return repository.save(existing);
    }

    /** Delete a computer (part of "manage data" in Part 1). */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ComputerNotFoundException(id);
        }
        repository.deleteById(id);
    }

    /** Removes spaces at the start and end, so " Dell " is saved as "Dell". */
    private void trimText(Computer computer) {
        computer.setName(computer.getName().trim());
        computer.setBrand(computer.getBrand().trim());
        computer.setSerialNumber(computer.getSerialNumber().trim());
    }
}
