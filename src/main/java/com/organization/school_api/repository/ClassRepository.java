package com.organization.school_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.organization.school_api.Entity.ClassEntity;

public interface ClassRepository extends JpaRepository<ClassEntity,Long>{
    
}
