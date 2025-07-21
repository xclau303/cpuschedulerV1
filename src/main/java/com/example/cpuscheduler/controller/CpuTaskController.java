package com.example.cpuscheduler.controller;

import com.example.cpuscheduler.model.*;
import com.example.cpuscheduler.service.CpuTaskService;
import com.example.cpuscheduler.service.SchedulingAlgorithmService;
import com.example.cpuscheduler.service.SimulationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cpu-tasks")
public class CpuTaskController {

    private final CpuTaskService cpuTaskService;
    private final SchedulingAlgorithmService schedulingAlgorithmService;
    private final SimulationHistoryService simulationHistoryService;

    @Autowired
    public CpuTaskController(CpuTaskService cpuTaskService,
                             SchedulingAlgorithmService schedulingAlgorithmService,
                             SimulationHistoryService simulationHistoryService) {
        this.cpuTaskService = cpuTaskService;
        this.schedulingAlgorithmService = schedulingAlgorithmService;
        this.simulationHistoryService = simulationHistoryService;
    }

    // Test endpoint to verify backend is working
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Backend is working!");
        response.put("port", "8085");
        response.put("timestamp", new Date().toString());
        return ResponseEntity.ok(response);
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "CPU Scheduler");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/schedule/{algorithm}")
    public Map<String, Object> scheduleCpuTasks(
            @PathVariable String algorithm,
            @RequestBody Map<String, String> data) {

        String arrivalTimesStr = data.get("arrivalTimes");
        String burstTimesStr = data.get("burstTimes");

        int[] arrivalTimes = Arrays.stream(arrivalTimesStr.split(" ")).mapToInt(Integer::parseInt).toArray();
        int[] burstTimes = Arrays.stream(burstTimesStr.split(" ")).mapToInt(Integer::parseInt).toArray();

        List<CpuTask> tasks = new ArrayList<>();
        for (int i = 0; i < arrivalTimes.length; i++) {
            CpuTask task = new CpuTask(arrivalTimes[i], burstTimes[i]);
            task.setProcessId(i + 1);

            if (algorithm.equals("priority")) {
                String prioritiesStr = data.get("priorities");
                int[] priorities = Arrays.stream(prioritiesStr.split(" ")).mapToInt(Integer::parseInt).toArray();
                task.setPriority(priorities[i]);
            }

            CpuTask savedTask = cpuTaskService.addTask(task);
            tasks.add(savedTask);
        }

        Map<String, Object> serviceResult;
        switch (algorithm) {
            case "fcfs":
                serviceResult = schedulingAlgorithmService.scheduleFCFS(tasks);
                break;
            case "sjf":
                serviceResult = schedulingAlgorithmService.scheduleSJF(tasks);
                break;
            case "priority":
                serviceResult = schedulingAlgorithmService.schedulePriority(tasks);
                break;
            case "rr":
                int quantum = Integer.parseInt(data.get("quantum"));
                serviceResult = schedulingAlgorithmService.scheduleRR(tasks, quantum);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported scheduling algorithm");
        }

        // SAVE SIMULATION HISTORY with session ID
        try {
            simulationHistoryService.saveSimulationHistory(
                    algorithm,
                    arrivalTimesStr,
                    burstTimesStr,
                    data.get("priorities"),
                    algorithm.equals("rr") ? Integer.parseInt(data.get("quantum")) : null,
                    (Double) serviceResult.get("averageTAT"),
                    (Double) serviceResult.get("averageWT"),
                    serviceResult.get("ganttChart"),
                    serviceResult.get("scheduledTasks"),
                    data.get("sessionId") // Add session ID
            );
        } catch (Exception e) {
            System.err.println("Failed to save simulation history: " + e.getMessage());
        }

        return serviceResult;
    }
}