package com.example.cpuscheduler.model;

public class GanttChart {
    private int startTime;
    private int endTime;
    private Long id;

    public GanttChart(int startTime, int endTime, Long id) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.id = id;
    }

    // Getters and setters
    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}