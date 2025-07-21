package com.example.cpuscheduler.controller;

import com.example.cpuscheduler.model.SimulationHistory;
import com.example.cpuscheduler.service.SimulationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/simulation-history")
public class SimulationHistoryController {

    private final SimulationHistoryService simulationHistoryService;

    @Autowired
    public SimulationHistoryController(SimulationHistoryService simulationHistoryService) {
        this.simulationHistoryService = simulationHistoryService;
    }

    // Get all simulation history for a specific session
    @GetMapping
    public List<SimulationHistory> getAllSimulationHistory(@RequestParam String sessionId) {
        return simulationHistoryService.getAllSimulationHistory(sessionId);
    }

    // Get simulation history by ID
    @GetMapping("/{id}")
    public ResponseEntity<SimulationHistory> getSimulationHistoryById(@PathVariable Long id) {
        Optional<SimulationHistory> history = simulationHistoryService.getSimulationHistoryById(id);
        return history.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get simulation history by algorithm and session
    @GetMapping("/algorithm/{algorithm}")
    public List<SimulationHistory> getSimulationHistoryByAlgorithm(@PathVariable String algorithm, @RequestParam String sessionId) {
        return simulationHistoryService.getSimulationHistoryByAlgorithm(algorithm, sessionId);
    }

    // Get most recent N simulations for a specific session
    @GetMapping("/recent/{limit}")
    public List<SimulationHistory> getRecentSimulations(@PathVariable int limit, @RequestParam String sessionId) {
        return simulationHistoryService.getRecentSimulations(limit, sessionId);
    }

    // Delete simulation history by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulationHistory(@PathVariable Long id, @RequestParam String sessionId) {
        simulationHistoryService.deleteSimulationHistory(id);
        return ResponseEntity.noContent().build();
    }

    // Delete all simulation history for a specific session
    @DeleteMapping
    public ResponseEntity<Void> deleteAllSimulationHistory(@RequestParam String sessionId) {
        simulationHistoryService.deleteAllSimulationHistory(sessionId);
        return ResponseEntity.noContent().build();
    }
}