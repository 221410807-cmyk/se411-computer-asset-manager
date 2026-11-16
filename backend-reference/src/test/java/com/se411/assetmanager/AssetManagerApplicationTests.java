package com.se411.assetmanager;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Starts the whole application once (without a real network port) and checks that
 * controller, service, repository and sample data work together.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AssetManagerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("the application starts and has 12 sample computers on 3 pages")
    void sampleDataSupportsPagination() throws Exception {
        mockMvc.perform(get("/api/computers").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    @DisplayName("add, find, update and delete a computer through the real API")
    void fullCrudFlow() throws Exception {
        String json = """
                {"name": "Smoke Test PC", "brand": "Asus", "serialNumber": "AS-9999", "status": "AVAILABLE"}
                """;
        String created = mockMvc.perform(post("/api/computers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/computers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smoke Test PC"));

        String changes = """
                {"name": "Smoke Test PC", "brand": "Asus", "serialNumber": "AS-9999", "status": "IN_REPAIR"}
                """;
        mockMvc.perform(put("/api/computers/" + id).contentType(MediaType.APPLICATION_JSON).content(changes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REPAIR"));

        mockMvc.perform(delete("/api/computers/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/computers/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a sample serial number cannot be used twice")
    void duplicateSerialIsRejected() throws Exception {
        String json = """
                {"name": "Copy", "brand": "Dell", "serialNumber": "DL-1001", "status": "AVAILABLE"}
                """;
        mockMvc.perform(post("/api/computers").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict());
    }
}
