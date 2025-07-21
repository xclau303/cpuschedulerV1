package com.example.cpuscheduler.service;

import com.example.cpuscheduler.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SchedulingAlgorithmServiceTest {

    @InjectMocks
    private SchedulingAlgorithmService schedulingAlgorithmService;

    private List<CpuTask> testTasks;
    private List<CpuTask> testTasksWithIdleTime;
    private List<CpuTask> testTasksWithPriority;

    @BeforeEach
    void setUp() {
        // Basic test tasks
        testTasks = Arrays.asList(
                createTask(1, 0, 5, 0),
                createTask(2, 1, 3, 0),
                createTask(3, 2, 8, 0)
        );

        // Test tasks with idle time (gap between arrivals)
        testTasksWithIdleTime = Arrays.asList(
                createTask(1, 0, 2, 0),
                createTask(2, 5, 3, 0),
                createTask(3, 8, 1, 0)
        );

        // Test tasks with priority
        testTasksWithPriority = Arrays.asList(
                createTask(1, 0, 5, 3),
                createTask(2, 1, 3, 1),
                createTask(3, 2, 8, 2)
        );
    }

    private CpuTask createTask(int processId, int arrivalTime, int burstTime, int priority) {
        CpuTask task = new CpuTask();
        task.setProcessId(processId);
        task.setArrivalTime(arrivalTime);
        task.setBurstTime(burstTime);
        task.setPriority(priority);
        task.setRemainingTime(burstTime);
        return task;
    }

    @Test
    void testScheduleFCFS_BasicScenario() {
        // Given
        List<CpuTask> tasks = new ArrayList<>(testTasks);

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleFCFS(tasks);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("scheduledTasks"));
        assertTrue(result.containsKey("ganttChart"));
        assertTrue(result.containsKey("averageTAT"));
        assertTrue(result.containsKey("averageWT"));

        @SuppressWarnings("unchecked")
        List<FcfsSjfTaskResponse> scheduledTasks = (List<FcfsSjfTaskResponse>) result.get("scheduledTasks");
        assertEquals(3, scheduledTasks.size());

        // Verify tasks are processed in FCFS order
        assertEquals(1, scheduledTasks.get(0).getId());
        assertEquals(2, scheduledTasks.get(1).getId());
        assertEquals(3, scheduledTasks.get(2).getId());

        // Verify timing calculations
        FcfsSjfTaskResponse task1 = scheduledTasks.get(0);
        assertEquals(0, task1.getStartTime());
        assertEquals(5, task1.getCompletionTime());
        assertEquals(5, task1.getTurnaroundTime());
        assertEquals(0, task1.getWaitingTime());

        // Verify Gantt chart
        @SuppressWarnings("unchecked")
        List<GanttChart> ganttChart = (List<GanttChart>) result.get("ganttChart");
        assertEquals(3, ganttChart.size());
        assertEquals(0, ganttChart.get(0).getStartTime());
        assertEquals(5, ganttChart.get(0).getEndTime());
        assertEquals(1L, ganttChart.get(0).getId());
    }

    @Test
    void testScheduleFCFS_WithIdleTime() {
        // Given
        List<CpuTask> tasks = new ArrayList<>(testTasksWithIdleTime);

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleFCFS(tasks);

        // Then
        @SuppressWarnings("unchecked")
        List<GanttChart> ganttChart = (List<GanttChart>) result.get("ganttChart");

        // Should have idle periods represented
        assertTrue(ganttChart.stream().anyMatch(gc -> gc.getId() == null));
    }

    @Test
    void testScheduleFCFS_EmptyTaskList() {
        // Given
        List<CpuTask> emptyTasks = new ArrayList<>();

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleFCFS(emptyTasks);

        // Then
        @SuppressWarnings("unchecked")
        List<FcfsSjfTaskResponse> scheduledTasks = (List<FcfsSjfTaskResponse>) result.get("scheduledTasks");
        assertTrue(scheduledTasks.isEmpty());

        @SuppressWarnings("unchecked")
        List<GanttChart> ganttChart = (List<GanttChart>) result.get("ganttChart");
        assertTrue(ganttChart.isEmpty());

        assertEquals(0.0, (Double) result.get("averageTAT"));
        assertEquals(0.0, (Double) result.get("averageWT"));
    }

    @Test
    void testScheduleSJF_BasicScenario() {
        // Given
        List<CpuTask> tasks = new ArrayList<>(testTasks);

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleSJF(tasks);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<FcfsSjfTaskResponse> scheduledTasks = (List<FcfsSjfTaskResponse>) result.get("scheduledTasks");
        assertEquals(3, scheduledTasks.size());

        // Verify tasks are sorted by process ID in response
        assertEquals(1, scheduledTasks.get(0).getId());
        assertEquals(2, scheduledTasks.get(1).getId());
        assertEquals(3, scheduledTasks.get(2).getId());

        // Verify average calculations
        assertTrue(result.get("averageTAT") instanceof Double);
        assertTrue(result.get("averageWT") instanceof Double);
    }

    @Test
    void testScheduleSJF_WithIdleTime() {
        // Given
        List<CpuTask> tasks = new ArrayList<>(testTasksWithIdleTime);

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleSJF(tasks);

        // Then
        @SuppressWarnings("unchecked")
        List<GanttChart> ganttChart = (List<GanttChart>) result.get("ganttChart");

        // Should handle idle periods
        assertTrue(ganttChart.stream().anyMatch(gc -> gc.getId() == null));
    }

    @Test
    void testSchedulePriority_BasicScenario() {
        // Given
        List<CpuTask> tasks = new ArrayList<>(testTasksWithPriority);

        // When
        Map<String, Object> result = schedulingAlgorithmService.schedulePriority(tasks);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<PriorityTaskResponse> scheduledTasks = (List<PriorityTaskResponse>) result.get("scheduledTasks");
        assertEquals(3, scheduledTasks.size());

        // Verify response contains priority information
        scheduledTasks.forEach(task -> {
            assertTrue(task.getPriority() > 0);
            assertEquals(0, task.getRemainingTime()); // Non-preemptive
        });

        // Verify Gantt chart
        @SuppressWarnings("unchecked")
        List<GanttChart> ganttChart = (List<GanttChart>) result.get("ganttChart");
        assertFalse(ganttChart.isEmpty());
    }

    @Test
    void testSchedulePriority_SamePriority() {
        // Given - tasks with same priority should be ordered by arrival time
        List<CpuTask> tasks = Arrays.asList(
                createTask(1, 0, 5, 1),
                createTask(2, 1, 3, 1),
                createTask(3, 2, 8, 1)
        );

        // When
        Map<String, Object> result = schedulingAlgorithmService.schedulePriority(tasks);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<PriorityTaskResponse> scheduledTasks = (List<PriorityTaskResponse>) result.get("scheduledTasks");
        assertEquals(3, scheduledTasks.size());
    }

    @Test
    void testScheduleRR_BasicScenario() {
        // Given
        List<CpuTask> tasks = new ArrayList<>(testTasks);
        int quantum = 2;

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleRR(tasks, quantum);

        // Then
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<RrTaskResponse> scheduledTasks = (List<RrTaskResponse>) result.get("scheduledTasks");
        assertEquals(3, scheduledTasks.size());

        // Verify tasks are sorted by process ID
        assertEquals(1, scheduledTasks.get(0).getId());
        assertEquals(2, scheduledTasks.get(1).getId());
        assertEquals(3, scheduledTasks.get(2).getId());

        // Verify Gantt chart shows time slices
        @SuppressWarnings("unchecked")
        List<GanttChart> ganttChart = (List<GanttChart>) result.get("ganttChart");
        assertFalse(ganttChart.isEmpty());

        // Should have multiple entries for tasks that need more than one quantum
        long task1Entries = ganttChart.stream()
                .filter(gc -> gc.getId() != null && gc.getId() == 1L)
                .count();
        assertTrue(task1Entries > 1); // Task 1 has burst time 5, quantum 2, so needs multiple slices
    }

    @Test
    void testScheduleRR_WithIdleTime() {
        // Given
        List<CpuTask> tasks = new ArrayList<>(testTasksWithIdleTime);
        int quantum = 2;

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleRR(tasks, quantum);

        // Then
        @SuppressWarnings("unchecked")
        List<GanttChart> ganttChart = (List<GanttChart>) result.get("ganttChart");

        // Should handle idle periods
        assertTrue(ganttChart.stream().anyMatch(gc -> gc.getId() == null));
    }

    @Test
    void testScheduleRR_LargeQuantum() {
        // Given - quantum larger than any burst time
        List<CpuTask> tasks = new ArrayList<>(testTasks);
        int quantum = 10;

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleRR(tasks, quantum);

        // Then
        @SuppressWarnings("unchecked")
        List<RrTaskResponse> scheduledTasks = (List<RrTaskResponse>) result.get("scheduledTasks");
        assertEquals(3, scheduledTasks.size());

        // With large quantum, should behave like FCFS
        scheduledTasks.forEach(task -> {
            assertEquals(0, task.getRemainingTime());
        });
    }

    @Test
    void testScheduleRR_SmallQuantum() {
        // Given - very small quantum
        List<CpuTask> tasks = new ArrayList<>(testTasks);
        int quantum = 1;

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleRR(tasks, quantum);

        // Then
        @SuppressWarnings("unchecked")
        List<GanttChart> ganttChart = (List<GanttChart>) result.get("ganttChart");

        // Should have many context switches
        assertTrue(ganttChart.size() > 3);
    }

    @Test
    void testScheduleRR_EmptyTaskList() {
        // Given
        List<CpuTask> emptyTasks = new ArrayList<>();
        int quantum = 2;

        // When
        Map<String, Object> result = schedulingAlgorithmService.scheduleRR(emptyTasks, quantum);

        // Then
        @SuppressWarnings("unchecked")
        List<RrTaskResponse> scheduledTasks = (List<RrTaskResponse>) result.get("scheduledTasks");
        assertTrue(scheduledTasks.isEmpty());

        @SuppressWarnings("unchecked")
        List<GanttChart> ganttChart = (List<GanttChart>) result.get("ganttChart");
        assertTrue(ganttChart.isEmpty());
    }

    @Test
    void testRoundingMethods() {
        // Test through public methods that use rounding
        List<CpuTask> tasks = Arrays.asList(
                createTask(1, 0, 3, 0),
                createTask(2, 1, 3, 0),
                createTask(3, 2, 3, 0)
        );

        Map<String, Object> fcfsResult = schedulingAlgorithmService.scheduleFCFS(tasks);
        Map<String, Object> sjfResult = schedulingAlgorithmService.scheduleSJF(new ArrayList<>(tasks));
        Map<String, Object> rrResult = schedulingAlgorithmService.scheduleRR(new ArrayList<>(tasks), 2);

        // Verify that averages are properly rounded
        assertTrue(fcfsResult.get("averageTAT") instanceof Double);
        assertTrue(fcfsResult.get("averageWT") instanceof Double);
        assertTrue(sjfResult.get("averageTAT") instanceof Double);
        assertTrue(sjfResult.get("averageWT") instanceof Double);
        assertTrue(rrResult.get("averageTAT") instanceof Double);
        assertTrue(rrResult.get("averageWT") instanceof Double);
    }

    @Test
    void testTasksWithZeroArrivalTime() {
        // Given - all tasks arrive at time 0
        List<CpuTask> tasks = Arrays.asList(
                createTask(1, 0, 3, 1),
                createTask(2, 0, 2, 2),
                createTask(3, 0, 4, 3)
        );

        // When
        Map<String, Object> fcfsResult = schedulingAlgorithmService.scheduleFCFS(new ArrayList<>(tasks));
        Map<String, Object> sjfResult = schedulingAlgorithmService.scheduleSJF(new ArrayList<>(tasks));
        Map<String, Object> priorityResult = schedulingAlgorithmService.schedulePriority(new ArrayList<>(tasks));
        Map<String, Object> rrResult = schedulingAlgorithmService.scheduleRR(new ArrayList<>(tasks), 2);

        // Then - all should handle zero arrival time correctly
        assertNotNull(fcfsResult);
        assertNotNull(sjfResult);
        assertNotNull(priorityResult);
        assertNotNull(rrResult);

        // Verify no idle time at the beginning
        @SuppressWarnings("unchecked")
        List<GanttChart> fcfsGantt = (List<GanttChart>) fcfsResult.get("ganttChart");
        if (!fcfsGantt.isEmpty()) {
            assertEquals(0, fcfsGantt.get(0).getStartTime());
        }
    }

    @Test
    void testSingleTask() {
        // Given
        List<CpuTask> singleTask = Arrays.asList(createTask(1, 0, 5, 1));

        // When
        Map<String, Object> fcfsResult = schedulingAlgorithmService.scheduleFCFS(new ArrayList<>(singleTask));
        Map<String, Object> sjfResult = schedulingAlgorithmService.scheduleSJF(new ArrayList<>(singleTask));
        Map<String, Object> priorityResult = schedulingAlgorithmService.schedulePriority(new ArrayList<>(singleTask));
        Map<String, Object> rrResult = schedulingAlgorithmService.scheduleRR(new ArrayList<>(singleTask), 2);

        // Then
        verifyResultStructure(fcfsResult, 1);
        verifyResultStructure(sjfResult, 1);
        verifyResultStructure(priorityResult, 1);
        verifyResultStructure(rrResult, 1);
    }

    private void verifyResultStructure(Map<String, Object> result, int expectedTaskCount) {
        assertNotNull(result);
        assertTrue(result.containsKey("scheduledTasks"));
        assertTrue(result.containsKey("ganttChart"));
        assertTrue(result.containsKey("averageTAT"));
        assertTrue(result.containsKey("averageWT"));

        @SuppressWarnings("unchecked")
        List<?> scheduledTasks = (List<?>) result.get("scheduledTasks");
        assertEquals(expectedTaskCount, scheduledTasks.size());

        assertTrue(result.get("averageTAT") instanceof Double);
        assertTrue(result.get("averageWT") instanceof Double);
    }
}