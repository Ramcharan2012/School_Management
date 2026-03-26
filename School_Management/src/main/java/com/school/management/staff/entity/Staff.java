package com.school.management.staff.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.management.academic.entity.Department;
import com.school.management.common.entity.BaseEntity;
import com.school.management.common.enums.StaffCategory;
import com.school.management.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Non-Teaching Staff entity.
 * Every staff member is linked to a User account (role = STAFF).
 * Examples: librarian, accountant, security, lab assistant, admin clerk.
 */
@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff extends BaseEntity {

    @Column(name = "staff_id", nullable = false, unique = true)
    private String staffId;   // e.g. STF-2024-001

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_category", nullable = false)
    private StaffCategory staffCategory;

    @Column(name = "designation")
    private String designation;   // e.g. "Senior Librarian"

    @Column(name = "qualification")
    private String qualification;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;  // optional — some staff may not belong to a department
}
