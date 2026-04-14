package com.smarttask.task.repository;

import com.smarttask.task.entity.Task;
import com.smarttask.task.enums.Priority;
import com.smarttask.task.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Filter by status, priority, assignee
    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:username IS NULL OR t.assignedToUsername = :username)")
    Page<Task> findByFilters(
            @Param("status")   TaskStatus status,
            @Param("priority") Priority priority,
            @Param("username") String username,
            Pageable pageable);

    // Find tasks assigned to a specific user
    Page<Task> findByAssignedToUsername(
            String username, Pageable pageable);

    // Find overdue tasks
    @Query("SELECT t FROM Task t WHERE " +
           "t.dueDate < :today AND t.status <> 'DONE'")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    // Count overdue tasks
    @Query("SELECT COUNT(t) FROM Task t WHERE " +
           "t.dueDate < :today AND t.status <> 'DONE'")
    long countOverdue(@Param("today") LocalDate today);

    // Find tasks by status
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    // Find unassigned tasks
    @Query("SELECT t FROM Task t WHERE " +
           "t.assignedToUsername IS NULL")
    Page<Task> findUnassignedTasks(Pageable pageable);
}