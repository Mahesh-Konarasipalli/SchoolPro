package com.organization.school_api.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "attendance")
@Data
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which student is this for?
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Which class were they supposed to be in?
    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity studentClass;

    // The date of the attendance
    @Column(nullable = false)
    private LocalDate date;

    // "PRESENT", "ABSENT", or "LATE"
    @Column(nullable = false)
    private String status;

}