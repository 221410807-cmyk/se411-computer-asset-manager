package com.se411.assetmanager.repository;

// REFERENCE IMPLEMENTATION (SE411): for learning and local verification only.
// The submitted backend must be written by the team. See docs/backend-implementation-guide.md.

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.stereotype.Repository;

import com.se411.assetmanager.model.Computer;

/**
 * Stores computers in memory (a TreeMap sorted by id). There is no database,
 * so the data goes back to the sample records every time the server restarts.
 *
 * Only this class knows HOW data is stored. If you later switch to a real
 * database (for example Spring Data JPA + H2), only this class has to change.
 *
 * Methods are "synchronized" so two requests at the same time cannot corrupt the map.
 */
@Repository
public class ComputerRepository {

    private final Map<Long, Computer> computers = new TreeMap<>();
    private long nextId = 1;

    /** Adds a new computer (id == null gets the next id) or replaces an existing one. */
    public synchronized Computer save(Computer computer) {
        if (computer.getId() == null) {
            computer.setId(nextId);
            nextId++;
        }
        computers.put(computer.getId(), computer);
        return computer;
    }

    public synchronized Optional<Computer> findById(Long id) {
        return Optional.ofNullable(computers.get(id));
    }

    public synchronized boolean existsById(Long id) {
        return computers.containsKey(id);
    }

    /** Returns one page of computers, ordered by id. A page past the end is empty. */
    public synchronized List<Computer> findPage(int page, int size) {
        List<Computer> all = new ArrayList<>(computers.values());
        int from = page * size;
        if (from >= all.size()) {
            return new ArrayList<>();
        }
        int to = Math.min(from + size, all.size());
        return new ArrayList<>(all.subList(from, to));
    }

    public synchronized long count() {
        return computers.size();
    }

    public synchronized void deleteById(Long id) {
        computers.remove(id);
    }

    /**
     * True if another computer already uses this serial number (ignoring upper/lower case).
     * excludeId lets a computer keep its own serial number when it is updated; use null when adding.
     */
    public synchronized boolean existsBySerialNumber(String serialNumber, Long excludeId) {
        for (Computer computer : computers.values()) {
            boolean sameSerial = computer.getSerialNumber().equalsIgnoreCase(serialNumber);
            boolean sameComputer = computer.getId().equals(excludeId);
            if (sameSerial && !sameComputer) {
                return true;
            }
        }
        return false;
    }
}
