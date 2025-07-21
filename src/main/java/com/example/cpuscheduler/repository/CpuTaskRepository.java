package com.example.cpuscheduler.repository;

import com.example.cpuscheduler.model.CpuTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CpuTaskRepository extends JpaRepository<CpuTask, Long> {
}
