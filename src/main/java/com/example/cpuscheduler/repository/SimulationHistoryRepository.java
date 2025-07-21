package com.example.cpuscheduler.repository;

import com.example.cpuscheduler.model.SimulationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SimulationHistoryRepository extends JpaRepository<SimulationHistory, Long> {

    // Find all simulations for a specific session ordered by timestamp descending
    @Query("SELECT s FROM SimulationHistory s WHERE s.sessionId = :sessionId ORDER BY s.timestamp DESC")
    List<SimulationHistory> findBySessionIdOrderByTimestampDesc(@Param("sessionId") String sessionId);

    // Find simulations by algorithm and session
    @Query("SELECT s FROM SimulationHistory s WHERE s.algorithm = :algorithm AND s.sessionId = :sessionId ORDER BY s.timestamp DESC")
    List<SimulationHistory> findByAlgorithmAndSessionIdOrderByTimestampDesc(@Param("algorithm") String algorithm, @Param("sessionId") String sessionId);

    // Find most recent N simulations for a specific session
    @Query("SELECT s FROM SimulationHistory s WHERE s.sessionId = :sessionId ORDER BY s.timestamp DESC")
    List<SimulationHistory> findRecentSimulationsBySessionId(@Param("sessionId") String sessionId, org.springframework.data.domain.Pageable pageable);

    // Cleanup query for old simulations
    @Transactional
    @Modifying
    @Query("DELETE FROM SimulationHistory s WHERE s.timestamp < :cutoffDate")
    void deleteOldSimulations(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Count simulations for a specific session
    @Query("SELECT COUNT(s) FROM SimulationHistory s WHERE s.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);
}
