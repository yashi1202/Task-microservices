package com.smarttask.collab.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments",
       indexes = @Index(name = "idx_comment_task_id",
               columnList = "taskId"))
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference by ID only — no cross-service FK
    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private String authorUsername;

    @Column(nullable = false)
    private String authorFullName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}