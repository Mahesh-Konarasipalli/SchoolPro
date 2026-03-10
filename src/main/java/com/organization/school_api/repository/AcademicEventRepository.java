package com.organization.school_api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.organization.school_api.Entity.AcademicEvent;

@Repository
public interface AcademicEventRepository extends JpaRepository<AcademicEvent, Long>{
    List<AcademicEvent> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate today);
}
