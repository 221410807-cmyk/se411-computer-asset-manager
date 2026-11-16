package com.se411.assetmanager.controller;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.se411.assetmanager.dto.PageResponse;
import com.se411.assetmanager.model.Computer;
import com.se411.assetmanager.service.ComputerService;

import jakarta.validation.Valid;

/**
 * The REST API. Each method is one route:
 *
 *   POST   /api/computers                 add a computer           -> 201 Created
 *   GET    /api/computers/{id}            find one by id           -> 200 OK / 404
 *   GET    /api/computers?page=0&size=5   list one page            -> 200 OK / 400
 *   PUT    /api/computers/{id}            update a computer        -> 200 OK / 400 / 404 / 409
 *   DELETE /api/computers/{id}            delete a computer        -> 204 No Content / 404
 *
 * Errors are turned into JSON by GlobalExceptionHandler.
 * @CrossOrigin lets the React dev server (port 5173) call this API from the browser.
 */
@RestController
@RequestMapping("/api/computers")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ComputerController {

    private final ComputerService service;

    public ComputerController(ComputerService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Computer> create(@Valid @RequestBody Computer computer) {
        Computer saved = service.create(computer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // The names in quotes ("id", "page", "size") are written out on purpose,
    // so the code also works when Eclipse compiles it without extra settings.
    @GetMapping("/{id}")
    public Computer getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    @GetMapping
    public PageResponse<Computer> getPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        return service.getPage(page, size);
    }

    @PutMapping("/{id}")
    public Computer update(@PathVariable("id") Long id, @Valid @RequestBody Computer computer) {
        return service.update(id, computer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
