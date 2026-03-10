package com.organization.school_api.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

import java.util.List;;

@Entity
@Table(name = "classes")
@Data
public class ClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String className;
    private String teacherName;
    private String roomNumber;

    @OneToMany(mappedBy = "studentClass")
    @ToString.Exclude
    private List<User> students;
}
