package com.organization.school_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.organization.school_api.Entity.Grade;

public interface gradeRepository extends JpaRepository<Grade, Long>{
	List<Grade> findByStudentId(Long studentId);
}
