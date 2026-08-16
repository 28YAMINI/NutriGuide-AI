package com.nutriguideai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nutriguideai.enums.MedicalCondition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A medical condition recorded against a user (1:N — a user can have many).
 */
@Entity
@Table(name = "user_medical_conditions", indexes = {
        @Index(name = "idx_cond_user_id", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMedicalCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "medical_condtion", nullable = false, length = 40)
    private MedicalCondition condition;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
