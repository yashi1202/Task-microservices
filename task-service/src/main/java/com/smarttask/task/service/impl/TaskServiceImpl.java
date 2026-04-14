package com.smarttask.task.service.impl;

import com.smarttask.task.dto.AssignTaskRequest;
import com.smarttask.task.dto.ApproveTaskRequest;
import com.smarttask.task.dto.CreateTaskRequest;
import com.smarttask.task.dto.TaskResponse;
import com.smarttask.task.dto.UpdateTaskRequest;
import com.smarttask.task.entity.Task;
import com.smarttask.task.enums.Priority;
import com.smarttask.task.enums.TaskStatus;
import com.smarttask.task.event.TaskEvent;
import com.smarttask.task.event.TaskEventPublisher;
import com.smarttask.task.exception.BadRequestException;
import com.smarttask.task.exception.ForbiddenException;
import com.smarttask.task.exception.OptimisticLockException;
import com.smarttask.task.exception.ResourceNotFoundException;
import com.smarttask.task.exception.TaskAlreadyAssignedException;
import com.smarttask.task.exception.TaskNotAssignedException;
import com.smarttask.task.repository.TaskRepository;
import com.smarttask.task.service.TaskService;
import com.smarttask.task.util.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository     taskRepository;
    private final TaskEventPublisher eventPublisher;
    private final TaskMapper         taskMapper;

    // ─── Create ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request,
                                    String performedBy,
                                    String callerRole) {

        // Only ADMIN and MANAGER can assign on creation
        if (request.getAssignedToUsername() != null
                && !isAdminOrManager(callerRole)) {
            throw new ForbiddenException(
                    "Only ADMIN or MANAGER can assign tasks");
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null
                        ? request.getPriority() : Priority.MEDIUM)
                .status(request.getStatus() != null
                        ? request.getStatus() : TaskStatus.OPEN)
                .dueDate(request.getDueDate())
                .assignedToUsername(
                        request.getAssignedToUsername())
                .build();

        Task saved = taskRepository.save(task);

        eventPublisher.publish(TaskEvent.builder()
                .eventType(TaskEvent.EventType.TASK_CREATED)
                .taskId(saved.getId())
                .title(saved.getTitle())
                .status(saved.getStatus().name())
                .priority(saved.getPriority().name())
                .assignedToUsername(
                        saved.getAssignedToUsername())
                .performedBy(performedBy)
                .occurredAt(LocalDateTime.now().toString())
                .build());

        log.info("Task #{} '{}' created by '{}'",
                saved.getId(), saved.getTitle(), performedBy);
        return taskMapper.toResponse(saved);
    }

    // ─── Update ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TaskResponse updateTask(Long id,
                                    UpdateTaskRequest request,
                                    String performedBy,
                                    String callerRole) {

        Task task = findOrThrow(id);

        // Optimistic locking check
        if (!task.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockException();
        }

        String oldStatus   = task.getStatus().name();
        String oldAssignee = task.getAssignedToUsername();

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        // Status change
        if (request.getStatus() != null
                && !request.getStatus().name()
                        .equals(oldStatus)) {
            task.setStatus(request.getStatus());

            eventPublisher.publish(TaskEvent.builder()
                    .eventType(
                            TaskEvent.EventType.TASK_STATUS_CHANGED)
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .oldStatus(oldStatus)
                    .newStatus(request.getStatus().name())
                    .performedBy(performedBy)
                    .occurredAt(LocalDateTime.now().toString())
                    .build());
        }

        // Assignment change — only ADMIN and MANAGER
        if (request.getAssignedToUsername() != null) {
            if (!isAdminOrManager(callerRole)) {
                throw new ForbiddenException(
                        "Only ADMIN or MANAGER can "
                        + "reassign tasks");
            }
            task.setAssignedToUsername(
                    request.getAssignedToUsername());

            eventPublisher.publish(TaskEvent.builder()
                    .eventType(TaskEvent.EventType.TASK_ASSIGNED)
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .oldAssignee(oldAssignee)
                    .newAssignee(
                            request.getAssignedToUsername())
                    .performedBy(performedBy)
                    .occurredAt(LocalDateTime.now().toString())
                    .build());
        }

        Task saved = taskRepository.save(task);

        eventPublisher.publish(TaskEvent.builder()
                .eventType(TaskEvent.EventType.TASK_UPDATED)
                .taskId(saved.getId())
                .title(saved.getTitle())
                .status(saved.getStatus().name())
                .performedBy(performedBy)
                .occurredAt(LocalDateTime.now().toString())
                .build());

        log.info("Task #{} updated by '{}'", id, performedBy);
        return taskMapper.toResponse(saved);
    }

    // ─── Assign ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TaskResponse assignTask(Long id,
                                    AssignTaskRequest request,
                                    String performedBy,
                                    String callerRole) {

        
        /*if (!isAdminOrManager(callerRole)) {
            throw new ForbiddenException(
                    "Only ADMIN or MANAGER can assign tasks");
        }*/

        Task task = findOrThrow(id);

        // Prevent assigning to the same user again
       /* if (request.getUsername().equals(
                task.getAssignedToUsername())) {
            throw new TaskAlreadyAssignedException(
                    request.getUsername());
        }

        String oldAssignee = task.getAssignedToUsername();
        task.setAssignedToUsername(request.getUsername());
        task.setAssignedToFullName(request.getFullName());*/
        if (task.getAssignedToUsername() != null) {
            throw new TaskAlreadyAssignedException(
                    "Task is already assigned to '"
                    + task.getAssignedToUsername()
                    + "'");
        }

        task.setAssignedToUsername(request.getUsername());
        task.setAssignedToFullName(request.getFullName());

        Task saved = taskRepository.save(task);

        /*eventPublisher.publish(TaskEvent.builder()
                .eventType(TaskEvent.EventType.TASK_ASSIGNED)
                .taskId(saved.getId())
                .title(saved.getTitle())
                .oldAssignee(oldAssignee)
                .newAssignee(request.getUsername())
                .assignedToUsername(request.getUsername())
                .assignedToFullName(request.getFullName())
                .performedBy(performedBy)
                .occurredAt(LocalDateTime.now().toString())
                .build());*/
        eventPublisher.publish(TaskEvent.builder()
                .eventType(TaskEvent.EventType.TASK_ASSIGNED)
                .taskId(saved.getId())
                .title(saved.getTitle())
                .newAssignee(request.getUsername())
                .assignedToUsername(request.getUsername())
                .performedBy(performedBy)
                .occurredAt(
                        LocalDateTime.now().toString())
                .build());

        log.info("Task #{} assigned to '{}' by '{}'",
                id, request.getUsername(), performedBy);
        return taskMapper.toResponse(saved);
    }

    // ─── Unassign ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TaskResponse unassignTask(Long id,
                                      String performedBy,
                                      String callerRole) {

        // Only ADMIN and MANAGER can unassign
        if (!isAdminOrManager(callerRole)) {
            throw new ForbiddenException(
                    "Only ADMIN or MANAGER can unassign tasks");
        }

        Task task = findOrThrow(id);

        // Cannot unassign a task with no assignee
        if (task.getAssignedToUsername() == null) {
            throw new TaskNotAssignedException(id);
        }

        String oldAssignee = task.getAssignedToUsername();
        task.setAssignedToUsername(null);
        task.setAssignedToFullName(null);

        Task saved = taskRepository.save(task);

        eventPublisher.publish(TaskEvent.builder()
                .eventType(TaskEvent.EventType.TASK_UNASSIGNED)
                .taskId(saved.getId())
                .title(saved.getTitle())
                .oldAssignee(oldAssignee)
                .performedBy(performedBy)
                .occurredAt(LocalDateTime.now().toString())
                .build());

        log.info("Task #{} unassigned by '{}'",
                id, performedBy);
        return taskMapper.toResponse(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return taskMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> findAll(TaskStatus status,
                                       Priority priority,
                                       String assignedTo,
                                       Pageable pageable) {
        return taskRepository.findByFilters(
                status, priority, assignedTo, pageable)
                .map(taskMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> findMyTasks(String username,
                                           Pageable pageable) {
        return taskRepository
                .findByAssignedToUsername(username, pageable)
                .map(taskMapper::toResponse);
    }

    // ─── Delete ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteTask(Long id,
                            String performedBy,
                            String callerRole) {

        // Only ADMIN and MANAGER can delete
        if (!isAdminOrManager(callerRole)) {
            throw new ForbiddenException(
                    "Only ADMIN or MANAGER can delete tasks");
        }

        Task task = findOrThrow(id);
        taskRepository.delete(task);

        eventPublisher.publish(TaskEvent.builder()
                .eventType(TaskEvent.EventType.TASK_DELETED)
                .taskId(id)
                .title(task.getTitle())
                .performedBy(performedBy)
                .occurredAt(LocalDateTime.now().toString())
                .build());

        log.info("Task #{} deleted by '{}'", id, performedBy);
    }
    
 // ─── Approve Task (ADMIN only) ────────────────────────────────

    @Override
    @Transactional
    public TaskResponse approveTask(Long id,
                                     ApproveTaskRequest request,
                                     String performedBy,
                                     String callerRole) {

        // Only ADMIN can give final approval
        if (!"ROLE_ADMIN".equals(callerRole)) {
            throw new ForbiddenException(
                    "Only ADMIN can give final approval");
        }

        Task task = findOrThrow(id);

        // Task must be DONE before it can be approved
        if (task.getStatus() != TaskStatus.DONE) {
            throw new BadRequestException(
                    "Task must be marked as DONE "
                    + "before it can be approved. "
                    + "Current status: "
                    + task.getStatus().name());
        }

        // Optimistic locking check
        if (!task.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockException();
        }

        String oldStatus = task.getStatus().name();

        // Mark as APPROVED
        task.setStatus(TaskStatus.APPROVED);

        // Store admin review comment in description
        if (request.getReviewComment() != null
                && !request.getReviewComment().isBlank()) {
            String existing = task.getDescription() != null
                    ? task.getDescription() : "";
            task.setDescription(existing
                    + "\n\n[Admin approval by "
                    + performedBy + "]: "
                    + request.getReviewComment());
        }

        Task saved = taskRepository.save(task);

        // Publish TASK_APPROVED event to Kafka
        eventPublisher.publish(TaskEvent.builder()
                .eventType(TaskEvent.EventType.TASK_APPROVED)
                .taskId(saved.getId())
                .title(saved.getTitle())
                .oldStatus(oldStatus)
                .newStatus(TaskStatus.APPROVED.name())
                .performedBy(performedBy)
                .occurredAt(LocalDateTime.now().toString())
                .build());

        log.info("Task #{} approved by admin '{}'",
                id, performedBy);

        return taskMapper.toResponse(saved);
    }
    // ─── Helpers ──────────────────────────────────────────────────────────

    private Task findOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task", "id", id));
    }

    private boolean isAdminOrManager(String role) {
        return "ROLE_ADMIN".equals(role)
                || "ROLE_MANAGER".equals(role);
    }
}