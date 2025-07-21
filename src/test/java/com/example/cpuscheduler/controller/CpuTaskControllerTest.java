//package com.example.cpuscheduler.controller;
//
//import com.example.cpuscheduler.model.CpuTask;
//import com.example.cpuscheduler.service.CpuTaskController;
//import com.example.cpuscheduler.service.CpuTaskService;
//import com.example.cpuscheduler.service.SchedulingAlgorithmService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(MockitoExtension.class)
//public class CpuTaskControllerTest {
//
//    @Mock
//    private CpuTaskService cpuTaskService;
//
//    @Mock
//    private SchedulingAlgorithmService schedulingAlgorithmService;
//
//    @InjectMocks
//    private CpuTaskController cpuTaskController;
//
//    private MockMvc mockMvc;
//
//    @Test
//    public void testAddTask() throws Exception {
//        CpuTask task = new CpuTask("Task1", 1, 5, 0);
//        when(cpuTaskService.addTask(any(CpuTask.class))).thenReturn(task);
//
//        mockMvc = MockMvcBuilders.standaloneSetup(cpuTaskController).build();
//
//        mockMvc.perform(post("/scheduler/add-task")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"name\":\"Task1\",\"priority\":1,\"burstTime\":5,\"arrivalTime\":0}"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Task1"))
//                .andExpect(jsonPath("$.priority").value(1))
//                .andExpect(jsonPath("$.burstTime").value(5));
//    }
//
//    @Test
//    public void testGetAllTasks() throws Exception {
//        List<CpuTask> tasks = Arrays.asList(
//                new CpuTask("Task1", 1, 5, 0),
//                new CpuTask("Task2", 2, 3, 1)
//        );
//        when(cpuTaskService.getAllTasks()).thenReturn(tasks);
//
//        mockMvc = MockMvcBuilders.standaloneSetup(cpuTaskController).build();
//
//        mockMvc.perform(get("/scheduler/tasks"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].name").value("Task1"))
//                .andExpect(jsonPath("$[1].name").value("Task2"));
//    }
//
//    @Test
//    public void testScheduleFCFS() throws Exception {
//        List<CpuTask> tasks = Arrays.asList(
//                new CpuTask("Task1", 1, 5, 0),
//                new CpuTask("Task2", 2, 3, 1)
//        );
//        when(cpuTaskService.getAllTasks()).thenReturn(tasks);
//        when(schedulingAlgorithmService.scheduleFCFS(tasks)).thenReturn(tasks);
//
//        mockMvc = MockMvcBuilders.standaloneSetup(cpuTaskController).build();
//
//        mockMvc.perform(get("/scheduler/tasks/fcfs"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].name").value("Task1"))
//                .andExpect(jsonPath("$[1].name").value("Task2"));
//    }
//}