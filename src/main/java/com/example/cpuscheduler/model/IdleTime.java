package com.example.cpuscheduler.model;

public class IdleTime {
    private int startTime;
    private int endTime;
    private int duration;

    public IdleTime(int startTime, int endTime, int duration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = endTime - startTime;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}