package com.se411.assetmanager.controller;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.se411.assetmanager.dto.PageResponse;
import com.se411.assetmanager.exception.ComputerNotFoundException;
import com.se411.assetmanager.exception.DuplicateSerialNumberException;
import com.se411.assetmanager.exception.GlobalExceptionHandler;
import com.se411.assetmanager.exception.InvalidPageRequestException;
import com.se411.assetmanager.model.Computer;
import com.se411.assetmanager.model.ComputerStatus;
import com.se411.assetmanager.service.ComputerService;

/**
 * Unit tests for ComputerController and GlobalExceptionHandler.
 *
 * The service is a Mockito mock. MockMvc sends fake HTTP requests to the controller
 * without starting a real server, so we can check status codes and JSON bodies.
 */
@ExtendWith(MockitoExtension.class)
class ComputerControllerTest {

    @Mock
    private ComputerService service;

    @InjectMocks
    private ComputerController controller;

    private MockMvc mockMvc;

    private static final String VALID_JSON = """
            {"name": "Lab PC", "brand": "Dell", "serialNumber": "DL-9000", "status": "AVAILABLE"}
            """;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Computer computer(long id) {
        return new Computer(id, "Lab PC " + id, "Dell", "DL-900" + id, ComputerStatus.AVAILABLE);
    }

    // ---------- POST /api/computers (requirement 6) ----------

    @Test
    @DisplayName("POST valid computer -> 201 Created with the new id")
    void createReturns201() throws Exception {
        when(service.create(any(Computer.class))).thenReturn(computer(13));

        mockMvc.perform(post("/api/computers").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(13))
                .andExpect(jsonPath("$.name").value("Lab PC 13"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("POST with empty and invalid fields -> 400 with one message per field")
    void createRejectsInvalidFields() throws Exception {
        String json = """
                {"name": "", "brand": "D", "serialNumber": "bad serial!"}
                """;

        mockMvc.perform(post("/api/computers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed. Please fix the highlighted fields."))
                .andExpect(jsonPath("$.fieldErrors.name").value("Name is required."))
                .andExpect(jsonPath("$.fieldErrors.brand").value("Brand must be 2-30 characters."))
                .andExpect(jsonPath("$.fieldErrors.serialNumber")
                        .value("Serial number can only contain letters, numbers and dashes."))
                .andExpect(jsonPath("$.fieldErrors.status").value("Status is required."));
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("POST with an unknown status -> 400")
    void createRejectsUnknownStatus() throws Exception {
        String json = """
                {"name": "Lab PC", "brand": "Dell", "serialNumber": "DL-9000", "status": "LOST"}
                """;

        mockMvc.perform(post("/api/computers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("POST broken JSON -> 400")
    void createRejectsBrokenJson() throws Exception {
        mockMvc.perform(post("/api/computers").contentType(MediaType.APPLICATION_JSON).content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST duplicate serial number -> 409 Conflict")
    void createDuplicateReturns409() throws Exception {
        when(service.create(any(Computer.class))).thenThrow(new DuplicateSerialNumberException("DL-9000"));

        mockMvc.perform(post("/api/computers").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Serial number DL-9000 is already used by another computer."))
                .andExpect(jsonPath("$.fieldErrors.serialNumber").value("This serial number is already in use."));
    }

    // ---------- GET /api/computers/{id} (requirement 7) ----------

    @Test
    @DisplayName("GET existing id -> 200 with the computer")
    void getByIdReturns200() throws Exception {
        when(service.getById(3L)).thenReturn(computer(3));

        mockMvc.perform(get("/api/computers/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.serialNumber").value("DL-9003"));
    }

    @Test
    @DisplayName("GET missing id -> 404 Not Found")
    void getByIdMissingReturns404() throws Exception {
        when(service.getById(99L)).thenThrow(new ComputerNotFoundException(99L));

        mockMvc.perform(get("/api/computers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Computer with ID 99 was not found."));
    }

    @Test
    @DisplayName("GET id that is not a number -> 400")
    void getByIdNotANumberReturns400() throws Exception {
        mockMvc.perform(get("/api/computers/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'id' must be a whole number."));
    }

    // ---------- GET /api/computers?page=&size= (requirement 8) ----------

    @Test
    @DisplayName("GET list without parameters uses page 0 and size 5")
    void listUsesDefaults() throws Exception {
        List<Computer> five = List.of(computer(1), computer(2), computer(3), computer(4), computer(5));
        when(service.getPage(0, 5)).thenReturn(new PageResponse<>(five, 0, 5, 12, 3));

        mockMvc.perform(get("/api/computers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("GET list passes page and size to the service")
    void listPassesPageAndSize() throws Exception {
        when(service.getPage(2, 5)).thenReturn(new PageResponse<>(List.of(computer(11), computer(12)), 2, 5, 12, 3));

        mockMvc.perform(get("/api/computers").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(11))
                .andExpect(jsonPath("$.page").value(2));
    }

    @Test
    @DisplayName("GET list with size=0 -> 400")
    void listBadSizeReturns400() throws Exception {
        when(service.getPage(eq(0), anyInt())).thenThrow(new InvalidPageRequestException("Size must be between 1 and 50."));

        mockMvc.perform(get("/api/computers").param("page", "0").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Size must be between 1 and 50."));
    }

    @Test
    @DisplayName("GET list with page=x -> 400")
    void listPageNotANumberReturns400() throws Exception {
        mockMvc.perform(get("/api/computers").param("page", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'page' must be a whole number."));
    }

    // ---------- PUT /api/computers/{id} (requirement 9) ----------

    @Test
    @DisplayName("PUT valid changes -> 200 with the updated computer")
    void updateReturns200() throws Exception {
        Computer updated = new Computer(2L, "Lab PC", "Dell", "DL-9000", ComputerStatus.AVAILABLE);
        when(service.update(eq(2L), any(Computer.class))).thenReturn(updated);

        mockMvc.perform(put("/api/computers/2").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.serialNumber").value("DL-9000"));
    }

    @Test
    @DisplayName("PUT missing id -> 404")
    void updateMissingReturns404() throws Exception {
        when(service.update(eq(99L), any(Computer.class))).thenThrow(new ComputerNotFoundException(99L));

        mockMvc.perform(put("/api/computers/99").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT invalid body -> 400 and the service is not called")
    void updateInvalidReturns400() throws Exception {
        String json = """
                {"name": "Lab PC", "brand": "Dell", "serialNumber": "X1", "status": "IN_USE"}
                """;

        mockMvc.perform(put("/api/computers/2").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.serialNumber").value("Serial number must be 4-30 characters."));
        verify(service, never()).update(any(), any());
    }

    // ---------- DELETE /api/computers/{id} ----------

    @Test
    @DisplayName("DELETE existing id -> 204 No Content")
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/computers/4"))
                .andExpect(status().isNoContent());
        verify(service).delete(4L);
    }

    @Test
    @DisplayName("DELETE missing id -> 404")
    void deleteMissingReturns404() throws Exception {
        doThrow(new ComputerNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete("/api/computers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Computer with ID 99 was not found."));
    }
}
