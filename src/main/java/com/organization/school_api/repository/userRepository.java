package com.organization.school_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.organization.school_api.Entity.User;

import java.util.List;
import java.util.Optional;

public interface userRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStudentId(String studentId);
    Optional<User> findByResetToken(String resetToken);
    List<User> findByRole(String role);
    
}
