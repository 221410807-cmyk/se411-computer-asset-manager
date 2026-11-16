package com.se411.assetmanager.config;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.se411.assetmanager.model.Computer;
import com.se411.assetmanager.model.ComputerStatus;
import com.se411.assetmanager.repository.ComputerRepository;

/**
 * Adds 12 sample computers when the server starts.
 * With the default page size of 5 that gives 3 pages (5 + 5 + 2), which is
 * enough to demonstrate pagination.
 */
@Component
public class SampleDataLoader implements CommandLineRunner {

    private final ComputerRepository repository;

    public SampleDataLoader(ComputerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        for (Computer computer : sampleComputers()) {
            repository.save(computer);
        }
    }

    /** The sample records. Their ids are null, so the repository gives them ids 1 to 12. */
    public static List<Computer> sampleComputers() {
        return List.of(
                new Computer(null, "Finance Laptop 01", "Dell", "DL-1001", ComputerStatus.IN_USE),
                new Computer(null, "Finance Laptop 02", "Dell", "DL-1002", ComputerStatus.AVAILABLE),
                new Computer(null, "Design Workstation", "Apple", "AP-2001", ComputerStatus.IN_USE),
                new Computer(null, "Reception Desktop", "HP", "HP-3001", ComputerStatus.IN_REPAIR),
                new Computer(null, "IT Admin Laptop", "Lenovo", "LN-4001", ComputerStatus.IN_USE),
                new Computer(null, "Training Room PC 1", "Acer", "AC-5001", ComputerStatus.AVAILABLE),
                new Computer(null, "Training Room PC 2", "Acer", "AC-5002", ComputerStatus.AVAILABLE),
                new Computer(null, "Old Sales Laptop", "Toshiba", "TS-6001", ComputerStatus.RETIRED),
                new Computer(null, "HR Laptop", "Lenovo", "LN-4002", ComputerStatus.IN_USE),
                new Computer(null, "Server Room Console", "HP", "HP-3002", ComputerStatus.IN_USE),
                new Computer(null, "Marketing MacBook", "Apple", "AP-2002", ComputerStatus.AVAILABLE),
                new Computer(null, "Spare Desktop", "Dell", "DL-1003", ComputerStatus.RETIRED));
    }
}
