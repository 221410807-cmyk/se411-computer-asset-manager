package com.se411.assetmanager.repository;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.se411.assetmanager.model.Computer;
import com.se411.assetmanager.model.ComputerStatus;

/** Plain JUnit tests for the in-memory repository (no mocks needed). */
class ComputerRepositoryTest {

    private ComputerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ComputerRepository();
        for (int i = 1; i <= 12; i++) {
            repository.save(new Computer(null, "PC " + i, "Dell", "SN-" + (1000 + i), ComputerStatus.AVAILABLE));
        }
    }

    @Test
    @DisplayName("save gives new computers ids 1, 2, 3 ...")
    void saveAssignsIds() {
        Computer saved = repository.save(new Computer(null, "New", "HP", "HP-0001", ComputerStatus.IN_USE));
        assertThat(saved.getId()).isEqualTo(13L);
        assertThat(repository.count()).isEqualTo(13);
    }

    @Test
    @DisplayName("save with an existing id replaces that computer")
    void saveReplacesExisting() {
        repository.save(new Computer(1L, "Renamed", "Dell", "SN-1001", ComputerStatus.RETIRED));
        assertThat(repository.findById(1L).orElseThrow().getName()).isEqualTo("Renamed");
        assertThat(repository.count()).isEqualTo(12);
    }

    @Test
    @DisplayName("findById returns empty for a missing id")
    void findByIdMissing() {
        assertThat(repository.findById(99L)).isEmpty();
        assertThat(repository.existsById(99L)).isFalse();
        assertThat(repository.existsById(5L)).isTrue();
    }

    @Test
    @DisplayName("findPage returns the right slice, ordered by id")
    void findPageSlices() {
        List<Computer> first = repository.findPage(0, 5);
        List<Computer> last = repository.findPage(2, 5);

        assertThat(first).extracting(Computer::getId).containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(last).extracting(Computer::getId).containsExactly(11L, 12L);
    }

    @Test
    @DisplayName("findPage past the end returns an empty list")
    void findPagePastEnd() {
        assertThat(repository.findPage(3, 5)).isEmpty();
    }

    @Test
    @DisplayName("deleteById removes the computer")
    void deleteRemoves() {
        repository.deleteById(3L);
        assertThat(repository.findById(3L)).isEmpty();
        assertThat(repository.count()).isEqualTo(11);
    }

    @Test
    @DisplayName("existsBySerialNumber ignores case and can skip one computer")
    void serialNumberCheck() {
        assertThat(repository.existsBySerialNumber("sn-1001", null)).isTrue();
        assertThat(repository.existsBySerialNumber("SN-1001", 1L)).isFalse(); // computer 1 itself
        assertThat(repository.existsBySerialNumber("SN-1001", 2L)).isTrue();
        assertThat(repository.existsBySerialNumber("NEW-1", null)).isFalse();
    }
}
