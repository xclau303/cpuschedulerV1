package com.example.cpuscheduler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimulationHistoryResponse {

    private Long id;
    private LocalDateTime timestamp;
    private String algorithm;
    private String arrivalTimes;
    private String burstTimes;
    private String priorities;
    private Integer quantum;
    private Double averageTAT;
    private Double averageWT;
    private Object ganttChart; // Will be parsed from JSON
    private Object results; // Will be parsed from JSON

    // Constructors
    public SimulationHistoryResponse() {}

    public SimulationHistoryResponse(Long id, LocalDateTime timestamp, String algorithm,
                                     String arrivalTimes, String burstTimes, String priorities,
                                     Integer quantum, Double averageTAT, Double averageWT,
                                     Object ganttChart, Object results) {
        this.id = id;
        this.timestamp = timestamp;
        this.algorithm = algorithm;
        this.arrivalTimes = arrivalTimes;
        this.burstTimes = burstTimes;
        this.priorities = priorities;
        this.quantum = quantum;
        this.averageTAT = averageTAT;
        this.averageWT = averageWT;
        this.ganttChart = ganttChart;
        this.results = results;
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

    public Object getGanttChart() { return ganttChart; }
    public void setGanttChart(Object ganttChart) { this.ganttChart = ganttChart; }

    public Object getResults() { return results; }
    public void setResults(Object results) { this.results = results; }
}