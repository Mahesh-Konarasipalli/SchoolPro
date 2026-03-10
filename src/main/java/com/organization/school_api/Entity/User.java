package com.organization.school_api.Entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;
    private String fullName;
    private String password;
    private String role;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassEntity studentClass;

    @OneToMany(mappedBy = "student" , cascade= CascadeType.ALL)
    private List<Grade> grades;

    public Double getAverageGrade() {
    if (grades == null || grades.isEmpty()) return 0.0;
    return grades.stream()
                 .mapToInt(Grade::getMarks)
                 .average()
                 .orElse(0.0);
    }
    private String otp;
    private boolean isVerified = false;
    private String resetToken;
    @Column(unique = true, nullable = true) // Nullable because Admins might not have one
    private String studentId;
}
