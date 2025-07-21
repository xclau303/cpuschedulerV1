package com.example.cpuscheduler.service;

import com.example.cpuscheduler.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class SchedulingAlgorithmService {

    // First Come First Serve Scheduling (FCFS)
    public Map<String, Object> scheduleFCFS(List<CpuTask> tasks) {
        AtomicInteger currentTime = new AtomicInteger(0);
        List<FcfsSjfTaskResponse> responses = new ArrayList<>();
        List<GanttChart> ganttChart = new ArrayList<>();

        // Sort tasks by arrival time
        List<CpuTask> sortedTasks = tasks.stream()
                .sorted(Comparator.comparingInt(CpuTask::getArrivalTime))
                .collect(Collectors.toList());

        // Check for initial idle time (from 0 to first task arrival)
        if (!sortedTasks.isEmpty() && sortedTasks.get(0).getArrivalTime() > 0) {
            ganttChart.add(new GanttChart(0, sortedTasks.get(0).getArrivalTime(), null)); // null for IDLE
            currentTime.set(sortedTasks.get(0).getArrivalTime());
        }

        sortedTasks.forEach(task -> {
            // Check for idle time between tasks
            if (currentTime.get() < task.getArrivalTime()) {
                ganttChart.add(new GanttChart(currentTime.get(), task.getArrivalTime(), null)); // null for IDLE
                currentTime.set(task.getArrivalTime());
            }

            int startTime = currentTime.get();
            int completionTime = currentTime.get() + task.getBurstTime();
            task.setCompletionTime(completionTime);

            // Add to Gantt chart
            ganttChart.add(new GanttChart(startTime, completionTime, (long) task.getProcessId()));

            currentTime.set(completionTime);
            responses.add(mapToFcfsSjfTaskResponse(task, task.getProcessId(), startTime));
        });

        // Sort responses by processId
        responses.sort(Comparator.comparingLong(FcfsSjfTaskResponse::getId));

        // Calculate averages
        double averageTAT = responses.stream()
                .mapToInt(FcfsSjfTaskResponse::getTurnaroundTime)
                .average()
                .orElse(0.0);

        double averageWT = responses.stream()
                .mapToInt(FcfsSjfTaskResponse::getWaitingTime)
                .average()
                .orElse(0.0);

        // Return map with all required fields
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("scheduledTasks", responses);
        response.put("ganttChart", ganttChart);
        response.put("averageTAT", roundToTwoDecimals(averageTAT));
        response.put("averageWT", roundToTwoDecimals(averageWT));
        return response;
    }

    private FcfsSjfTaskResponse mapToFcfsSjfTaskResponse(CpuTask task, int processId, int startTime) {
        FcfsSjfTaskResponse response = new FcfsSjfTaskResponse();
        response.setId((long) processId);
        response.setArrivalTime(task.getArrivalTime());
        response.setBurstTime(task.getBurstTime());
        response.setStartTime(startTime);
        response.setCompletionTime(task.getCompletionTime());
        response.setTurnaroundTime(task.getCompletionTime() - task.getArrivalTime());
        response.setWaitingTime(response.getTurnaroundTime() - task.getBurstTime());
        response.setRemainingTime(0); // Always 0 for non-preemptive algorithms
        return response;
    }

    // Shortest Job First Scheduling (SJF)
    public Map<String, Object> scheduleSJF(List<CpuTask> tasks) {
        AtomicInteger currentTime = new AtomicInteger(0);
        List<FcfsSjfTaskResponse> responses = new ArrayList<>();
        List<GanttChart> ganttChart = new ArrayList<>();

        List<CpuTask> originalTasks = new ArrayList<>(tasks);
        tasks.sort(Comparator.comparingInt(CpuTask::getArrivalTime));

        // Check for initial idle time (from 0 to first task arrival)
        if (!tasks.isEmpty() && tasks.get(0).getArrivalTime() > 0) {
            ganttChart.add(new GanttChart(0, tasks.get(0).getArrivalTime(), null)); // null for IDLE
            currentTime.set(tasks.get(0).getArrivalTime());
        }

        while (!tasks.isEmpty()) {
            int currentTimeSnapshot = currentTime.get();

            List<CpuTask> arrivedTasks = tasks.stream()
                    .filter(t -> t.getArrivalTime() <= currentTimeSnapshot)
                    .collect(Collectors.toList());

            if (!arrivedTasks.isEmpty()) {
                CpuTask shortestTask = arrivedTasks.stream()
                        .min(Comparator.comparingInt(CpuTask::getBurstTime)
                                .thenComparingInt(CpuTask::getArrivalTime))
                        .orElseThrow();

                if (currentTime.get() < shortestTask.getArrivalTime()) {
                    ganttChart.add(new GanttChart(currentTime.get(), shortestTask.getArrivalTime(), null)); // null for IDLE
                    currentTime.set(shortestTask.getArrivalTime());
                }

                tasks.remove(shortestTask);

                int startTime = currentTime.get();
                int completionTime = currentTime.get() + shortestTask.getBurstTime();
                shortestTask.setCompletionTime(completionTime);

                // Add to Gantt chart
                ganttChart.add(new GanttChart(startTime, completionTime, (long) shortestTask.getProcessId()));

                int turnaroundTime = completionTime - shortestTask.getArrivalTime();
                int waitingTime = turnaroundTime - shortestTask.getBurstTime();

                FcfsSjfTaskResponse response = mapToFcfsSjfTaskResponse(shortestTask, shortestTask.getProcessId(), startTime);
                response.setTurnaroundTime(turnaroundTime);
                response.setWaitingTime(waitingTime);
                responses.add(response);

                currentTime.set(completionTime);
            } else {
                int nextArrivalTime = tasks.stream()
                        .mapToInt(CpuTask::getArrivalTime)
                        .min()
                        .orElseThrow();

                ganttChart.add(new GanttChart(currentTime.get(), nextArrivalTime, null)); // null for IDLE
                currentTime.set(nextArrivalTime);
            }
        }

        // Sort responses by processId
        responses.sort(Comparator.comparingLong(FcfsSjfTaskResponse::getId));

        // Calculate averages
        double averageTAT = responses.stream()
                .mapToInt(FcfsSjfTaskResponse::getTurnaroundTime)
                .average()
                .orElse(0.0);

        double averageWT = responses.stream()
                .mapToInt(FcfsSjfTaskResponse::getWaitingTime)
                .average()
                .orElse(0.0);

        // Return map with all required fields
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("scheduledTasks", responses);
        response.put("ganttChart", ganttChart);
        response.put("averageTAT", roundToOneDecimal(averageTAT));
        response.put("averageWT", roundToOneDecimal(averageWT));
        return response;
    }

    // Priority Scheduling
    public Map<String, Object> schedulePriority(List<CpuTask> tasks) {
        AtomicInteger currentTime = new AtomicInteger(0);
        List<CpuTask> originalTasks = new ArrayList<>(tasks);
        List<PriorityTaskResponse> responses = new ArrayList<>();
        List<GanttChart> ganttChart = new ArrayList<>();

        List<CpuTask> sortedTasks = tasks.stream()
                .sorted(Comparator.comparingInt(CpuTask::getArrivalTime))
                .collect(Collectors.toList());

        // Check for initial idle time (from 0 to first task arrival)
        if (!sortedTasks.isEmpty() && sortedTasks.get(0).getArrivalTime() > 0) {
            ganttChart.add(new GanttChart(0, sortedTasks.get(0).getArrivalTime(), null)); // null for IDLE
            currentTime.set(sortedTasks.get(0).getArrivalTime());
        }

        PriorityQueue<CpuTask> priorityQueue = new PriorityQueue<>((t1, t2) -> {
            if (t1.getPriority() == t2.getPriority()) {
                return Integer.compare(t1.getArrivalTime(), t2.getArrivalTime());
            } else {
                return Integer.compare(t1.getPriority(), t2.getPriority()); // ascending order
            }
        });

        while (!sortedTasks.isEmpty() || !priorityQueue.isEmpty()) {
            while (!sortedTasks.isEmpty() && sortedTasks.get(0).getArrivalTime() <= currentTime.get()) {
                priorityQueue.add(sortedTasks.remove(0));
            }

            if (priorityQueue.isEmpty()) {
                int nextArrival = sortedTasks.get(0).getArrivalTime();
                if (currentTime.get() < nextArrival) {
                    ganttChart.add(new GanttChart(currentTime.get(), nextArrival, null)); // null for IDLE
                    currentTime.set(nextArrival);
                }
            } else {
                CpuTask taskToExecute = priorityQueue.poll();

                int startTime = currentTime.get();
                int completionTime = currentTime.get() + taskToExecute.getBurstTime();
                taskToExecute.setCompletionTime(completionTime);

                // Add to Gantt chart
                ganttChart.add(new GanttChart(startTime, completionTime, (long) taskToExecute.getProcessId()));

                currentTime.set(completionTime);

                int turnaroundTime = completionTime - taskToExecute.getArrivalTime();
                int waitingTime = turnaroundTime - taskToExecute.getBurstTime();

                PriorityTaskResponse response = mapToPriorityTaskResponse(taskToExecute, taskToExecute.getProcessId(), startTime);
                response.setTurnaroundTime(turnaroundTime);
                response.setWaitingTime(waitingTime);
                responses.add(response);
            }
        }

        // Sort responses by processId
        responses.sort(Comparator.comparingLong(PriorityTaskResponse::getId));

        // Calculate averages
        double averageTAT = responses.stream()
                .mapToInt(PriorityTaskResponse::getTurnaroundTime)
                .average()
                .orElse(0.0);

        double averageWT = responses.stream()
                .mapToInt(PriorityTaskResponse::getWaitingTime)
                .average()
                .orElse(0.0);

        // Return map with all required fields
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("scheduledTasks", responses);
        response.put("ganttChart", ganttChart);
        response.put("averageTAT", roundToOneDecimal(averageTAT));
        response.put("averageWT", roundToOneDecimal(averageWT));
        return response;
    }

    private PriorityTaskResponse mapToPriorityTaskResponse(CpuTask task, int id, int startTime) {
        PriorityTaskResponse response = new PriorityTaskResponse();
        response.setId((long) id);
        response.setArrivalTime(task.getArrivalTime());
        response.setBurstTime(task.getBurstTime());
        response.setStartTime(startTime);
        response.setCompletionTime(task.getCompletionTime());
        response.setTurnaroundTime(task.getCompletionTime() - task.getArrivalTime());
        response.setWaitingTime(response.getTurnaroundTime() - task.getBurstTime());
        response.setPriority(task.getPriority());
        response.setRemainingTime(0); // Always 0 for non-preemptive algorithms
        return response;
    }

    // Round Robin Scheduling
    public Map<String, Object> scheduleRR(List<CpuTask> tasks, int quantum) {
        List<RrTaskResponse> result = new ArrayList<>();
        List<CpuTask> taskList = new ArrayList<>(tasks);
        taskList.sort(Comparator.comparingInt(CpuTask::getArrivalTime));
        taskList.forEach(task -> task.setRemainingTime(task.getBurstTime()));

        Queue<CpuTask> queue = new LinkedList<>();
        List<GanttChart> ganttChart = new ArrayList<>();
        int currentTime = 0;

        // Check for initial idle time (from 0 to first task arrival)
        if (!taskList.isEmpty() && taskList.get(0).getArrivalTime() > 0) {
            ganttChart.add(new GanttChart(0, taskList.get(0).getArrivalTime(), null)); // null for IDLE
            currentTime = taskList.get(0).getArrivalTime();
        }

        // Track start times for each task
        Map<Long, Integer> taskStartTimes = new HashMap<>();

        while (!taskList.isEmpty() || !queue.isEmpty()) {
            while (!taskList.isEmpty() && taskList.get(0).getArrivalTime() <= currentTime) {
                queue.offer(taskList.remove(0));
            }

            if (queue.isEmpty()) {
                int nextArrival = taskList.get(0).getArrivalTime();
                if (currentTime < nextArrival) {
                    ganttChart.add(new GanttChart(currentTime, nextArrival, null)); // null for IDLE
                }
                currentTime = nextArrival;
                continue;
            }

            CpuTask task = queue.poll();

            // Record start time if this is the first time the task is executed
            if (!taskStartTimes.containsKey((long) task.getProcessId())) {
                taskStartTimes.put((long) task.getProcessId(), currentTime);
            }

            int startTime = currentTime;
            int executedTime = Math.min(task.getRemainingTime(), quantum);
            currentTime += executedTime;
            task.setRemainingTime(task.getRemainingTime() - executedTime);

            ganttChart.add(new GanttChart(startTime, currentTime, (long) task.getProcessId()));

            while (!taskList.isEmpty() && taskList.get(0).getArrivalTime() <= currentTime) {
                queue.offer(taskList.remove(0));
            }

            if (task.getRemainingTime() > 0) {
                queue.offer(task);
            } else {
                task.setCompletionTime(currentTime);
                task.setTurnaroundTime(task.getCompletionTime() - task.getArrivalTime());
                task.setWaitingTime(task.getTurnaroundTime() - task.getBurstTime());

                result.add(mapToRrTaskResponse(task, taskStartTimes.get((long) task.getProcessId())));
            }
        }

        // Sort result by processId
        result.sort(Comparator.comparingLong(RrTaskResponse::getId));

        // Calculate averages
        double averageTAT = result.stream()
                .mapToInt(RrTaskResponse::getTurnaroundTime)
                .average()
                .orElse(0.0);

        double averageWT = result.stream()
                .mapToInt(RrTaskResponse::getWaitingTime)
                .average()
                .orElse(0.0);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("scheduledTasks", result);
        response.put("ganttChart", ganttChart);
        response.put("averageTAT", roundToTwoDecimals(averageTAT));
        response.put("averageWT", roundToTwoDecimals(averageWT));
        return response;
    }

    private RrTaskResponse mapToRrTaskResponse(CpuTask task, int startTime) {
        RrTaskResponse response = new RrTaskResponse();
        response.setId((long) task.getProcessId());
        response.setArrivalTime(task.getArrivalTime());
        response.setBurstTime(task.getBurstTime());
        response.setStartTime(startTime);
        response.setCompletionTime(task.getCompletionTime());
        response.setTurnaroundTime(task.getTurnaroundTime());
        response.setWaitingTime(task.getWaitingTime());
        response.setRemainingTime(task.getRemainingTime());
        return response;
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double roundToOneDecimal(double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}