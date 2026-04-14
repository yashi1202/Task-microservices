package com.smarttask.collab.repository;

import com.smarttask.collab.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    Page<Comment> findByTaskIdOrderByCreatedAtDesc(
            Long taskId, Pageable pageable);

    long countByTaskId(Long taskId);

    Page<Comment> findByAuthorUsernameOrderByCreatedAtDesc(
            String username, Pageable pageable);
}