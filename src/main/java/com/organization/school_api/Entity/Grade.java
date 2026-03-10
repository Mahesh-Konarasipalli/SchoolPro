package com.organization.school_api.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "grade")
@Data
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String Subject;
    private int marks;
    private String semester;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User student;
}