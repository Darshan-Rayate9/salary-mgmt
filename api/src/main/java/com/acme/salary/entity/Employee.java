package com.acme.salary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employees_employee_code", columnNames = "employee_code"),
                @UniqueConstraint(name = "uk_employees_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_employees_department", columnList = "department"),
                @Index(name = "idx_employees_country", columnList = "country"),
                @Index(name = "idx_employees_level", columnList = "level"),
                @Index(name = "idx_employees_status", columnList = "employment_status")
        }
)
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, updatable = false, length = 20)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(nullable = false, length = 20)
    private String level;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Convert(converter = LocalDateStringConverter.class)
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE) // managed by @PrePersist, never set from outside
    private Instant createdAt;

    @Convert(converter = InstantStringConverter.class)
    @Column(name = "updated_at", nullable = false)
    @Setter(AccessLevel.NONE) // managed by @PrePersist/@PreUpdate, never set from outside
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.employmentStatus == null) {
            this.employmentStatus = EmploymentStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
