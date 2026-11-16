package com.se411.assetmanager.config;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.se411.assetmanager.model.Computer;
import com.se411.assetmanager.repository.ComputerRepository;

@ExtendWith(MockitoExtension.class)
class SampleDataLoaderTest {

    @Mock
    private ComputerRepository repository;

    @InjectMocks
    private SampleDataLoader loader;

    @Test
    @DisplayName("saves 12 sample computers at startup")
    void savesTwelveComputers() {
        loader.run();
        verify(repository, times(12)).save(any(Computer.class));
    }

    @Test
    @DisplayName("sample serial numbers are unique")
    void serialNumbersAreUnique() {
        assertThat(SampleDataLoader.sampleComputers())
                .extracting(Computer::getSerialNumber)
                .doesNotHaveDuplicates();
    }
}
