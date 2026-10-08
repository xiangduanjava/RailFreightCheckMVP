package com.railfreight.check.axle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AxleRepository extends JpaRepository<Axle, String> {

    List<Axle> findByAxleIdStartingWith(String prefix);
}
