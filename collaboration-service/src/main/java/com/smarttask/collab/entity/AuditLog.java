package com.smarttask.collab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs",
       indexes = @Index(name = "idx_audit_task_id",
               columnList = "taskId"))
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    private String taskTitle;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String performedBy;

    private String oldValue;
    private String newValue;
    private String description;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}