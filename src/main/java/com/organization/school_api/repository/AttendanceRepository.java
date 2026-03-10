package com.organization.school_api.repository; // Change to match your actual package name

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.organization.school_api.Entity.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    // Find all attendance records for a specific class on a specific day
    List<Attendance> findByStudentClassIdAndDate(Long classId, LocalDate date);
    
    // Find a specific student's attendance on a specific day (prevents double-marking)
    Optional<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);
    
    // Find all attendance records for one student (for their report card later!)
    List<Attendance> findByStudentIdOrderByDateDesc(Long studentId);
}