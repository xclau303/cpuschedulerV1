package com.example.cpuscheduler.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "simulation_history")
public class SimulationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "algorithm", nullable = false)
    private String algorithm;

    @Column(name = "arrival_times", nullable = false)
    private String arrivalTimes;

    @Column(name = "burst_times", nullable = false)
    private String burstTimes;

    @Column(name = "priorities")
    private String priorities;

    @Column(name = "quantum")
    private Integer quantum;

    @Column(name = "average_tat", nullable = false)
    private Double averageTAT;

    @Column(name = "average_wt", nullable = false)
    private Double averageWT;

    @Column(name = "gantt_chart", columnDefinition = "TEXT")
    private String ganttChart; // JSON string

    @Column(name = "results", columnDefinition = "TEXT")
    private String results; // JSON string

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    // Constructors
    public SimulationHistory() {}

    public SimulationHistory(String algorithm, String arrivalTimes, String burstTimes,
                             String priorities, Integer quantum, Double averageTAT,
                             Double averageWT, String ganttChart, String results, String sessionId) {
        this.timestamp = LocalDateTime.now();
        this.algorithm = algorithm;
        this.arrivalTimes = arrivalTimes;
        this.burstTimes = burstTimes;
        this.priorities = priorities;
        this.quantum = quantum;
        this.averageTAT = averageTAT;
        this.averageWT = averageWT;
        this.ganttChart = ganttChart;
        this.results = results;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getArrivalTimes() { return arrivalTimes; }
    public void setArrivalTimes(String arrivalTimes) { this.arrivalTimes = arrivalTimes; }

    public String getBurstTimes() { return burstTimes; }
    public void setBurstTimes(String burstTimes) { this.burstTimes = burstTimes; }

    public String getPriorities() { return priorities; }
    public void setPriorities(String priorities) { this.priorities = priorities; }

    public Integer getQuantum() { return quantum; }
    public void setQuantum(Integer quantum) { this.quantum = quantum; }

    public Double getAverageTAT() { return averageTAT; }
    public void setAverageTAT(Double averageTAT) { this.averageTAT = averageTAT; }

    public Double getAverageWT() { return averageWT; }
    public void setAverageWT(Double averageWT) { this.averageWT = averageWT; }

    public String getGanttChart() { return ganttChart; }
    public void setGanttChart(String ganttChart) { this.ganttChart = ganttChart; }

    public String getResults() { return results; }
    public void setResults(String results) { this.results = results; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}