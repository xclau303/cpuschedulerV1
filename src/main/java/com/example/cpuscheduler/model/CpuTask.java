package com.example.cpuscheduler.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class CpuTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private int priority;

    @Column(name = "burst_time")
    private int burstTime;

    @Column(name = "arrival_time")
    private int arrivalTime;

    @Column(name = "remaining_time")
    private int remainingTime;

    @Column(name = "completion_time")
    private Integer completionTime;

    @Column(name = "turnaround_time")
    private Integer turnaroundTime;

    @Column(name = "waiting_time")
    private Integer waitingTime;

    @Column(name = "start_time")  // <-- NEW FIELD
    private Integer startTime;

    // Constructor
    public CpuTask(int arrivalTime, int burstTime) {
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }

    public CpuTask(int arrivalTime, int burstTime, int priority) {
        this(arrivalTime, burstTime);
        this.priority = priority;
    }

    public CpuTask() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getBurstTime() { return burstTime; }
    public void setBurstTime(int burstTime) { this.burstTime = burstTime; }

    public int getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(int arrivalTime) { this.arrivalTime = arrivalTime; }

    public int getRemainingTime() { return remainingTime; }
    public void setRemainingTime(int remainingTime) { this.remainingTime = remainingTime; }

    public Integer getCompletionTime() { return completionTime; }
    public void setCompletionTime(Integer completionTime) { this.completionTime = completionTime; }

    public Integer getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(Integer turnaroundTime) { this.turnaroundTime = turnaroundTime; }

    public Integer getWaitingTime() { return waitingTime; }
    public void setWaitingTime(Integer waitingTime) { this.waitingTime = waitingTime; }

    public Integer getStartTime() { return startTime; }
    public void setStartTime(Integer startTime) { this.startTime = startTime; }

    private int processId;
    public int getProcessId() { return processId; }
    public void setProcessId(int processId) { this.processId = processId; }
}
