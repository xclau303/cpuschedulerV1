package com.example.cpuscheduler.service;

import com.example.cpuscheduler.model.SimulationHistory;
import com.example.cpuscheduler.repository.SimulationHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationHistoryServiceTest {

    @Mock
    private SimulationHistoryRepository repository;

    @InjectMocks
    private SimulationHistoryService simulationHistoryService;

    private SimulationHistory testHistory;
    private List<Object> testGanttChart;
    private List<Object> testResults;
    private String testSessionId;

    @BeforeEach
    void setUp() {
        testSessionId = "test-session-123";

        // Create test data
        testHistory = new SimulationHistory();
        testHistory.setId(1L);
        testHistory.setAlgorithm("FCFS");
        testHistory.setArrivalTimes("0,1,2");
        testHistory.setBurstTimes("5,3,8");
        testHistory.setPriorities("1,2,3");
        testHistory.setQuantum(2);
        testHistory.setAverageTAT(8.5);
        testHistory.setAverageWT(4.2);
        testHistory.setGanttChart("[{\"startTime\":0,\"endTime\":5,\"processId\":1}]");
        testHistory.setResults("[{\"id\":1,\"completionTime\":5}]");
        testHistory.setSessionId(testSessionId);
        testHistory.setTimestamp(LocalDateTime.now());

        testGanttChart = Arrays.asList(
                Map.of("startTime", 0, "endTime", 5, "processId", 1),
                Map.of("startTime", 5, "endTime", 8, "processId", 2)
        );

        testResults = Arrays.asList(
                Map.of("id", 1, "completionTime", 5),
                Map.of("id", 2, "completionTime", 8)
        );
    }

    @Test
    void testSaveSimulationHistory_Success() {
        // Given
        String algorithm = "FCFS";
        String arrivalTimes = "0,1,2";
        String burstTimes = "5,3,8";
        String priorities = "1,2,3";
        Integer quantum = 2;
        Double averageTAT = 8.5;
        Double averageWT = 4.2;

        List<SimulationHistory> userEntries = Arrays.asList(testHistory);

        when(repository.save(any(SimulationHistory.class))).thenReturn(testHistory);
        when(repository.findBySessionIdOrderByTimestampDesc(testSessionId)).thenReturn(userEntries);
        doNothing().when(repository).deleteOldSimulations(any(LocalDateTime.class));

        // When
        SimulationHistory result = simulationHistoryService.saveSimulationHistory(
                algorithm, arrivalTimes, burstTimes, priorities, quantum,
                averageTAT, averageWT, testGanttChart, testResults, testSessionId
        );

        // Then
        assertNotNull(result);
        assertEquals(testHistory.getId(), result.getId());
        assertEquals(algorithm, result.getAlgorithm());
        assertEquals(arrivalTimes, result.getArrivalTimes());
        assertEquals(burstTimes, result.getBurstTimes());
        assertEquals(priorities, result.getPriorities());
        assertEquals(quantum, result.getQuantum());
        assertEquals(averageTAT, result.getAverageTAT());
        assertEquals(averageWT, result.getAverageWT());
        assertEquals(testSessionId, result.getSessionId());

        verify(repository, times(1)).save(any(SimulationHistory.class));
        verify(repository, times(1)).deleteOldSimulations(any(LocalDateTime.class));
        verify(repository, times(1)).findBySessionIdOrderByTimestampDesc(testSessionId);
    }

    @Test
    void testSaveSimulationHistory_ExceedsMaxEntries() {
        // Given
        String algorithm = "FCFS";
        String arrivalTimes = "0,1,2";
        String burstTimes = "5,3,8";
        String priorities = "1,2,3";
        Integer quantum = 2;
        Double averageTAT = 8.5;
        Double averageWT = 4.2;

        // Create 12 entries (exceeds MAX_ENTRIES of 10)
        List<SimulationHistory> userEntries = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            SimulationHistory entry = new SimulationHistory();
            entry.setId((long) i);
            entry.setSessionId(testSessionId);
            userEntries.add(entry);
        }

        when(repository.save(any(SimulationHistory.class))).thenReturn(testHistory);
        when(repository.findBySessionIdOrderByTimestampDesc(testSessionId)).thenReturn(userEntries);
        doNothing().when(repository).deleteOldSimulations(any(LocalDateTime.class));
        doNothing().when(repository).deleteById(anyLong());

        // When
        SimulationHistory result = simulationHistoryService.saveSimulationHistory(
                algorithm, arrivalTimes, burstTimes, priorities, quantum,
                averageTAT, averageWT, testGanttChart, testResults, testSessionId
        );

        // Then
        assertNotNull(result);
        verify(repository, times(1)).save(any(SimulationHistory.class));
        verify(repository, times(2)).deleteById(anyLong()); // Should delete 2 excess entries
    }

    @Test
    void testGetAllSimulationHistory() {
        // Given
        List<SimulationHistory> expectedHistories = Arrays.asList(testHistory);
        when(repository.findBySessionIdOrderByTimestampDesc(testSessionId)).thenReturn(expectedHistories);

        // When
        List<SimulationHistory> result = simulationHistoryService.getAllSimulationHistory(testSessionId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testHistory.getId(), result.get(0).getId());
        assertEquals(testSessionId, result.get(0).getSessionId());
        verify(repository, times(1)).findBySessionIdOrderByTimestampDesc(testSessionId);
    }

    @Test
    void testGetSimulationHistoryById_Found() {
        // Given
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.of(testHistory));

        // When
        Optional<SimulationHistory> result = simulationHistoryService.getSimulationHistoryById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testHistory.getId(), result.get().getId());
        verify(repository, times(1)).findById(id);
    }

    @Test
    void testGetSimulationHistoryById_NotFound() {
        // Given
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<SimulationHistory> result = simulationHistoryService.getSimulationHistoryById(id);

        // Then
        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(id);
    }

    @Test
    void testGetSimulationHistoryByAlgorithm() {
        // Given
        String algorithm = "FCFS";
        List<SimulationHistory> expectedHistories = Arrays.asList(testHistory);
        when(repository.findByAlgorithmAndSessionIdOrderByTimestampDesc(algorithm, testSessionId))
                .thenReturn(expectedHistories);

        // When
        List<SimulationHistory> result = simulationHistoryService.getSimulationHistoryByAlgorithm(algorithm, testSessionId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(algorithm, result.get(0).getAlgorithm());
        assertEquals(testSessionId, result.get(0).getSessionId());
        verify(repository, times(1)).findByAlgorithmAndSessionIdOrderByTimestampDesc(algorithm, testSessionId);
    }

    @Test
    void testGetSimulationHistoryByAlgorithm_EmptyResult() {
        // Given
        String algorithm = "NonExistentAlgorithm";
        when(repository.findByAlgorithmAndSessionIdOrderByTimestampDesc(algorithm, testSessionId))
                .thenReturn(new ArrayList<>());

        // When
        List<SimulationHistory> result = simulationHistoryService.getSimulationHistoryByAlgorithm(algorithm, testSessionId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findByAlgorithmAndSessionIdOrderByTimestampDesc(algorithm, testSessionId);
    }

    @Test
    void testGetRecentSimulations() {
        // Given
        int limit = 5;
        List<SimulationHistory> expectedHistories = Arrays.asList(testHistory);
        when(repository.findRecentSimulationsBySessionId(eq(testSessionId), any(Pageable.class)))
                .thenReturn(expectedHistories);

        // When
        List<SimulationHistory> result = simulationHistoryService.getRecentSimulations(limit, testSessionId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testHistory.getId(), result.get(0).getId());
        verify(repository, times(1)).findRecentSimulationsBySessionId(eq(testSessionId), any(Pageable.class));
    }

    @Test
    void testGetRecentSimulations_ZeroLimit() {
        // Given
        int limit = 0;

        // When
        List<SimulationHistory> result = simulationHistoryService.getRecentSimulations(limit, testSessionId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, never()).findRecentSimulationsBySessionId(anyString(), any(Pageable.class));
    }

    @Test
    void testDeleteSimulationHistory() {
        // Given
        Long id = 1L;
        doNothing().when(repository).deleteById(id);

        // When
        simulationHistoryService.deleteSimulationHistory(id);

        // Then
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteAllSimulationHistory() {
        // Given
        List<SimulationHistory> userEntries = Arrays.asList(testHistory);
        when(repository.findBySessionIdOrderByTimestampDesc(testSessionId)).thenReturn(userEntries);
        doNothing().when(repository).deleteById(anyLong());

        // When
        simulationHistoryService.deleteAllSimulationHistory(testSessionId);

        // Then
        verify(repository, times(1)).findBySessionIdOrderByTimestampDesc(testSessionId);
        verify(repository, times(1)).deleteById(testHistory.getId());
    }

    @Test
    void testCleanupOldSimulations() {
        // Given
        doNothing().when(repository).deleteOldSimulations(any(LocalDateTime.class));

        // When
        simulationHistoryService.cleanupOldSimulations();

        // Then
        verify(repository, times(1)).deleteOldSimulations(any(LocalDateTime.class));
    }

    @Test
    void testParseJsonString_Success() {
        // Given
        String jsonString = "{\"id\":1,\"name\":\"test\"}";

        // When
        Object result = simulationHistoryService.parseJsonString(jsonString, Map.class);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals(1, resultMap.get("id"));
        assertEquals("test", resultMap.get("name"));
    }

    @Test
    void testParseJsonString_InvalidJson() {
        // Given
        String invalidJsonString = "invalid json";

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            simulationHistoryService.parseJsonString(invalidJsonString, Map.class);
        });

        assertEquals("Error parsing JSON string", exception.getMessage());
    }

    @Test
    void testSaveSimulationHistory_WithNullValues() {
        // Given
        String algorithm = "SJF";
        String arrivalTimes = "0,1,2";
        String burstTimes = "3,4,5";
        String priorities = null;
        Integer quantum = null;
        Double averageTAT = 5.0;
        Double averageWT = 2.0;

        SimulationHistory expectedHistory = new SimulationHistory();
        expectedHistory.setId(2L);
        expectedHistory.setAlgorithm(algorithm);
        expectedHistory.setArrivalTimes(arrivalTimes);
        expectedHistory.setBurstTimes(burstTimes);
        expectedHistory.setPriorities(priorities);
        expectedHistory.setQuantum(quantum);
        expectedHistory.setAverageTAT(averageTAT);
        expectedHistory.setAverageWT(averageWT);
        expectedHistory.setSessionId(testSessionId);

        when(repository.save(any(SimulationHistory.class))).thenReturn(expectedHistory);
        when(repository.findBySessionIdOrderByTimestampDesc(testSessionId)).thenReturn(Arrays.asList(expectedHistory));
        doNothing().when(repository).deleteOldSimulations(any(LocalDateTime.class));

        // When
        SimulationHistory result = simulationHistoryService.saveSimulationHistory(
                algorithm, arrivalTimes, burstTimes, priorities, quantum,
                averageTAT, averageWT, testGanttChart, testResults, testSessionId
        );

        // Then
        assertNotNull(result);
        assertEquals(algorithm, result.getAlgorithm());
        assertEquals(arrivalTimes, result.getArrivalTimes());
        assertEquals(burstTimes, result.getBurstTimes());
        assertNull(result.getPriorities());
        assertNull(result.getQuantum());
        assertEquals(averageTAT, result.getAverageTAT());
        assertEquals(averageWT, result.getAverageWT());
        assertEquals(testSessionId, result.getSessionId());

        verify(repository, times(1)).save(any(SimulationHistory.class));
    }

    @Test
    void testSaveSimulationHistory_WithEmptyStrings() {
        // Given
        String algorithm = "Priority";
        String arrivalTimes = "";
        String burstTimes = "";
        String priorities = "";
        Integer quantum = 0;
        Double averageTAT = 0.0;
        Double averageWT = 0.0;

        SimulationHistory expectedHistory = new SimulationHistory();
        expectedHistory.setId(3L);
        expectedHistory.setAlgorithm(algorithm);
        expectedHistory.setArrivalTimes(arrivalTimes);
        expectedHistory.setBurstTimes(burstTimes);
        expectedHistory.setPriorities(priorities);
        expectedHistory.setQuantum(quantum);
        expectedHistory.setAverageTAT(averageTAT);
        expectedHistory.setAverageWT(averageWT);
        expectedHistory.setSessionId(testSessionId);

        when(repository.save(any(SimulationHistory.class))).thenReturn(expectedHistory);
        when(repository.findBySessionIdOrderByTimestampDesc(testSessionId)).thenReturn(Arrays.asList(expectedHistory));
        doNothing().when(repository).deleteOldSimulations(any(LocalDateTime.class));

        // When
        SimulationHistory result = simulationHistoryService.saveSimulationHistory(
                algorithm, arrivalTimes, burstTimes, priorities, quantum,
                averageTAT, averageWT, new ArrayList<>(), new ArrayList<>(), testSessionId
        );

        // Then
        assertNotNull(result);
        assertEquals(algorithm, result.getAlgorithm());
        assertEquals("", result.getArrivalTimes());
        assertEquals("", result.getBurstTimes());
        assertEquals("", result.getPriorities());
        assertEquals(0, result.getQuantum());
        assertEquals(0.0, result.getAverageTAT());
        assertEquals(0.0, result.getAverageWT());
        assertEquals(testSessionId, result.getSessionId());

        verify(repository, times(1)).save(any(SimulationHistory.class));
    }

    @Test
    void testMultipleSessionsIsolation() {
        // Given
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";

        SimulationHistory history1 = createHistoryForSession("FCFS", 1L, sessionId1);
        SimulationHistory history2 = createHistoryForSession("SJF", 2L, sessionId2);

        when(repository.findBySessionIdOrderByTimestampDesc(sessionId1)).thenReturn(Arrays.asList(history1));
        when(repository.findBySessionIdOrderByTimestampDesc(sessionId2)).thenReturn(Arrays.asList(history2));

        // When
        List<SimulationHistory> result1 = simulationHistoryService.getAllSimulationHistory(sessionId1);
        List<SimulationHistory> result2 = simulationHistoryService.getAllSimulationHistory(sessionId2);

        // Then
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(sessionId1, result1.get(0).getSessionId());
        assertEquals(sessionId2, result2.get(0).getSessionId());
        assertEquals("FCFS", result1.get(0).getAlgorithm());
        assertEquals("SJF", result2.get(0).getAlgorithm());
    }

    @Test
    void testGetRecentSimulations_LargeLimit() {
        // Given
        int limit = 100;
        List<SimulationHistory> histories = Arrays.asList(
                createHistoryForSession("FCFS", 1L, testSessionId),
                createHistoryForSession("SJF", 2L, testSessionId),
                createHistoryForSession("RR", 3L, testSessionId)
        );

        when(repository.findRecentSimulationsBySessionId(eq(testSessionId), any(Pageable.class)))
                .thenReturn(histories);

        // When
        List<SimulationHistory> result = simulationHistoryService.getRecentSimulations(limit, testSessionId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(repository, times(1)).findRecentSimulationsBySessionId(eq(testSessionId), any(Pageable.class));
    }

    @Test
    void testParseJsonString_DifferentClasses() {
        // Given
        String listJson = "[{\"id\":1},{\"id\":2}]";
        String mapJson = "{\"key\":\"value\",\"number\":42}";
        String stringJson = "\"simple string\"";

        // When & Then
        Object listResult = simulationHistoryService.parseJsonString(listJson, List.class);
        assertNotNull(listResult);
        assertTrue(listResult instanceof List);

        Object mapResult = simulationHistoryService.parseJsonString(mapJson, Map.class);
        assertNotNull(mapResult);
        assertTrue(mapResult instanceof Map);

        Object stringResult = simulationHistoryService.parseJsonString(stringJson, String.class);
        assertNotNull(stringResult);
        assertTrue(stringResult instanceof String);
        assertEquals("simple string", stringResult);
    }

    @Test
    void testConstructorWithRepository() {
        // Given
        SimulationHistoryRepository mockRepo = mock(SimulationHistoryRepository.class);

        // When
        SimulationHistoryService service = new SimulationHistoryService(mockRepo);

        // Then
        assertNotNull(service);
        when(mockRepo.findBySessionIdOrderByTimestampDesc(testSessionId)).thenReturn(new ArrayList<>());
        List<SimulationHistory> result = service.getAllSimulationHistory(testSessionId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private SimulationHistory createHistoryForSession(String algorithm, Long id, String sessionId) {
        SimulationHistory history = new SimulationHistory();
        history.setId(id);
        history.setAlgorithm(algorithm);
        history.setArrivalTimes("0,1,2");
        history.setBurstTimes("3,4,5");
        history.setPriorities("1,2,3");
        history.setQuantum(2);
        history.setAverageTAT(6.0);
        history.setAverageWT(3.0);
        history.setGanttChart("[{}]");
        history.setResults("[{}]");
        history.setSessionId(sessionId);
        history.setTimestamp(LocalDateTime.now());
        return history;
    }
}