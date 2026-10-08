package com.railfreight.check.axle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AxleHistoryRepository extends JpaRepository<AxleHistory, Long> {

    List<AxleHistory> findByAxleIdOrderByEventTimeDesc(String axleId);
}
