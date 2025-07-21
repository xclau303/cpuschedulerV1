package com.example.cpuscheduler.service;

import com.example.cpuscheduler.model.CpuTask;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CpuTaskService {

    private final List<CpuTask> tasks = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong();

    // Add Task
    public CpuTask addTask(CpuTask task) {
        task.setId(idCounter.incrementAndGet());
        tasks.add(task);
        return task;
    }

    // Obtain All Tasks
    public List<CpuTask> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    // Obtain Tasks by ID
    public CpuTask getTaskById(Long id) {
        return tasks.stream().filter(task -> task.getId().equals(id)).findFirst().orElse(null);
    }

    // Update Tasks
    public CpuTask updateCpuTask(CpuTask updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            CpuTask existingTask = tasks.get(i);
            if (existingTask.getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                return updatedTask;
            }
        }
        return null;
    }

    // Delete Tasks by ID
    public void deleteTaskById(Long id) {
        tasks.removeIf(task -> task.getId().equals(id));
    }

    // Delete all Tasks
    public void deleteAllTasks() {
        tasks.clear();
    }
}
