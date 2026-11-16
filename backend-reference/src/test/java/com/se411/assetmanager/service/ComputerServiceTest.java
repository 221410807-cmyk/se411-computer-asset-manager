package com.se411.assetmanager.service;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.se411.assetmanager.dto.PageResponse;
import com.se411.assetmanager.exception.ComputerNotFoundException;
import com.se411.assetmanager.exception.DuplicateSerialNumberException;
import com.se411.assetmanager.exception.InvalidPageRequestException;
import com.se411.assetmanager.model.Computer;
import com.se411.assetmanager.model.ComputerStatus;
import com.se411.assetmanager.repository.ComputerRepository;

/**
 * Unit tests for ComputerService.
 * The repository is a Mockito mock, so only the service's own logic is tested.
 */
@ExtendWith(MockitoExtension.class)
class ComputerServiceTest {

    @Mock
    private ComputerRepository repository;

    @InjectMocks
    private ComputerService service;

    private Computer computer(Long id, String serialNumber) {
        return new Computer(id, "Lab PC", "Dell", serialNumber, ComputerStatus.AVAILABLE);
    }

    private List<Computer> computers(int count) {
        List<Computer> list = new ArrayList<>();
        for (long i = 1; i <= count; i++) {
            list.add(computer(i, "SN-000" + i));
        }
        return list;
    }

    // ---------- create (requirement 6) ----------

    @Test
    @DisplayName("create: trims text, ignores the client id and saves")
    void createSavesTrimmedComputer() {
        Computer input = new Computer(99L, "  Lab PC ", " Dell ", " DL-9000 ", ComputerStatus.AVAILABLE);
        when(repository.existsBySerialNumber("DL-9000", null)).thenReturn(false);
        when(repository.save(any(Computer.class))).thenAnswer(invocation -> {
            Computer toSave = invocation.getArgument(0);
            assertThat(toSave.getId()).isNull(); // the client's id 99 was removed
            toSave.setId(13L);
            return toSave;
        });

        Computer saved = service.create(input);

        assertThat(saved.getId()).isEqualTo(13L);
        assertThat(saved.getName()).isEqualTo("Lab PC");
        assertThat(saved.getBrand()).isEqualTo("Dell");
        assertThat(saved.getSerialNumber()).isEqualTo("DL-9000");
    }

    @Test
    @DisplayName("create: rejects a duplicate serial number and saves nothing")
    void createRejectsDuplicateSerial() {
        when(repository.existsBySerialNumber("DL-1001", null)).thenReturn(true);

        assertThatThrownBy(() -> service.create(computer(null, "DL-1001")))
                .isInstanceOf(DuplicateSerialNumberException.class)
                .hasMessageContaining("DL-1001");
        verify(repository, never()).save(any());
    }

    // ---------- getById (requirement 7) ----------

    @Test
    @DisplayName("getById: returns the computer when it exists")
    void getByIdFound() {
        Computer existing = computer(3L, "AP-2001");
        when(repository.findById(3L)).thenReturn(Optional.of(existing));

        assertThat(service.getById(3L)).isSameAs(existing);
    }

    @Test
    @DisplayName("getById: throws ComputerNotFoundException for a missing id")
    void getByIdMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ComputerNotFoundException.class)
                .hasMessage("Computer with ID 99 was not found.");
    }

    // ---------- getPage (requirement 8) ----------

    @Test
    @DisplayName("getPage: first page of 12 computers has 5 items and 3 pages")
    void getPageFirstPage() {
        when(repository.findPage(0, 5)).thenReturn(computers(5));
        when(repository.count()).thenReturn(12L);

        PageResponse<Computer> page = service.getPage(0, 5);

        assertThat(page.content()).hasSize(5);
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(5);
        assertThat(page.totalElements()).isEqualTo(12);
        assertThat(page.totalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("getPage: last page can be partly full")
    void getPageLastPartialPage() {
        when(repository.findPage(2, 5)).thenReturn(computers(2));
        when(repository.count()).thenReturn(12L);

        PageResponse<Computer> page = service.getPage(2, 5);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("getPage: a page after the last one is empty, not an error")
    void getPageBeyondLastPage() {
        when(repository.findPage(10, 5)).thenReturn(List.of());
        when(repository.count()).thenReturn(12L);

        assertThat(service.getPage(10, 5).content()).isEmpty();
    }

    @Test
    @DisplayName("getPage: no computers gives 0 pages")
    void getPageWhenEmpty() {
        when(repository.findPage(0, 5)).thenReturn(List.of());
        when(repository.count()).thenReturn(0L);

        PageResponse<Computer> page = service.getPage(0, 5);

        assertThat(page.totalElements()).isZero();
        assertThat(page.totalPages()).isZero();
    }

    @Test
    @DisplayName("getPage: size 1 and size 50 are the allowed limits")
    void getPageSizeLimitsAreAllowed() {
        when(repository.findPage(0, 1)).thenReturn(computers(1));
        when(repository.findPage(0, 50)).thenReturn(computers(12));
        when(repository.count()).thenReturn(12L);

        assertThat(service.getPage(0, 1).totalPages()).isEqualTo(12);
        assertThat(service.getPage(0, 50).totalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPage: rejects a negative page")
    void getPageRejectsNegativePage() {
        assertThatThrownBy(() -> service.getPage(-1, 5))
                .isInstanceOf(InvalidPageRequestException.class)
                .hasMessage("Page must be 0 or greater.");
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("getPage: rejects size 0 and size 51")
    void getPageRejectsBadSize() {
        assertThatThrownBy(() -> service.getPage(0, 0)).isInstanceOf(InvalidPageRequestException.class);
        assertThatThrownBy(() -> service.getPage(0, 51)).isInstanceOf(InvalidPageRequestException.class);
        verifyNoInteractions(repository);
    }

    // ---------- update (requirement 9) ----------

    @Test
    @DisplayName("update: copies the new values onto the existing computer")
    void updateExisting() {
        Computer existing = computer(2L, "DL-1002");
        Computer changes = new Computer(null, " New Name ", "HP", "HP-7777", ComputerStatus.IN_REPAIR);
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.existsBySerialNumber("HP-7777", 2L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);

        Computer updated = service.update(2L, changes);

        assertThat(updated.getId()).isEqualTo(2L);
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getBrand()).isEqualTo("HP");
        assertThat(updated.getSerialNumber()).isEqualTo("HP-7777");
        assertThat(updated.getStatus()).isEqualTo(ComputerStatus.IN_REPAIR);
    }

    @Test
    @DisplayName("update: a computer may keep its own serial number")
    void updateKeepsOwnSerial() {
        Computer existing = computer(2L, "DL-1002");
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.existsBySerialNumber("DL-1002", 2L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);

        Computer updated = service.update(2L, computer(null, "DL-1002"));

        assertThat(updated.getSerialNumber()).isEqualTo("DL-1002");
    }

    @Test
    @DisplayName("update: throws ComputerNotFoundException for a missing id")
    void updateMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, computer(null, "DL-9999")))
                .isInstanceOf(ComputerNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update: rejects a serial number used by another computer")
    void updateRejectsDuplicateSerial() {
        when(repository.findById(2L)).thenReturn(Optional.of(computer(2L, "DL-1002")));
        when(repository.existsBySerialNumber("DL-1001", 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(2L, computer(null, "DL-1001")))
                .isInstanceOf(DuplicateSerialNumberException.class);
        verify(repository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete: removes an existing computer")
    void deleteExisting() {
        when(repository.existsById(4L)).thenReturn(true);

        service.delete(4L);

        verify(repository).deleteById(4L);
    }

    @Test
    @DisplayName("delete: throws ComputerNotFoundException for a missing id")
    void deleteMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ComputerNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
