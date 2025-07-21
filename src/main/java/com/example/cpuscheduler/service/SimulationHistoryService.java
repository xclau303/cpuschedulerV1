package com.example.cpuscheduler.service;

import com.example.cpuscheduler.model.SimulationHistory;
import com.example.cpuscheduler.repository.SimulationHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SimulationHistoryService {

    private final SimulationHistoryRepository repository;
    private final ObjectMapper objectMapper;
    private static final int MAX_ENTRIES = 10;

    @Autowired
    public SimulationHistoryService(SimulationHistoryRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
    }

    // Save simulation history with session ID and automatic cleanup
    public SimulationHistory saveSimulationHistory(String algorithm, String arrivalTimes,
                                                   String burstTimes, String priorities,
                                                   Integer quantum, Double averageTAT,
                                                   Double averageWT, Object ganttChart,
                                                   Object results, String sessionId) {
        try {
            // Clean up old simulations first
            cleanupOldSimulations();

            String ganttChartJson = objectMapper.writeValueAsString(ganttChart);
            String resultsJson = objectMapper.writeValueAsString(results);

            SimulationHistory history = new SimulationHistory(
                    algorithm, arrivalTimes, burstTimes, priorities, quantum,
                    averageTAT, averageWT, ganttChartJson, resultsJson, sessionId
            );

            SimulationHistory savedHistory = repository.save(history);

            // Check if this user exceeds the limit
            List<SimulationHistory> userEntries = repository.findBySessionIdOrderByTimestampDesc(sessionId);
            if (userEntries.size() > MAX_ENTRIES) {
                int entriesToDelete = userEntries.size() - MAX_ENTRIES;
                for (int i = MAX_ENTRIES; i < userEntries.size(); i++) {
                    repository.deleteById(userEntries.get(i).getId());
                }
            }

            return savedHistory;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing simulation data", e);
        }
    }

    // Clean up simulations older than 5 days
    @Transactional
    public void cleanupOldSimulations() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(5);
        repository.deleteOldSimulations(cutoffDate);
    }

    // Get all simulation history for a specific session
    public List<SimulationHistory> getAllSimulationHistory(String sessionId) {
        return repository.findBySessionIdOrderByTimestampDesc(sessionId);
    }

    // Get simulation history by ID
    public Optional<SimulationHistory> getSimulationHistoryById(Long id) {
        return repository.findById(id);
    }

    // Get simulation history by algorithm and session
    public List<SimulationHistory> getSimulationHistoryByAlgorithm(String algorithm, String sessionId) {
        return repository.findByAlgorithmAndSessionIdOrderByTimestampDesc(algorithm, sessionId);
    }

    // Get most recent N simulations for a specific session
    public List<SimulationHistory> getRecentSimulations(int limit, String sessionId) {
        if (limit <= 0) {
            return new ArrayList<>();
        }
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return repository.findRecentSimulationsBySessionId(sessionId, pageable);
    }

    // Delete simulation history by ID
    public void deleteSimulationHistory(Long id) {
        repository.deleteById(id);
    }

    // Delete all simulation history for a specific session
    public void deleteAllSimulationHistory(String sessionId) {
        List<SimulationHistory> userEntries = repository.findBySessionIdOrderByTimestampDesc(sessionId);
        for (SimulationHistory entry : userEntries) {
            repository.deleteById(entry.getId());
        }
    }

    // Helper method to parse JSON strings back to objects
    public Object parseJsonString(String jsonString, Class<?> targetClass) {
        try {
            return objectMapper.readValue(jsonString, targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON string", e);
        }
    }
}