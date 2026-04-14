package com.smarttask.task.controller;

import com.smarttask.task.dto.AssignTaskRequest;
import com.smarttask.task.dto.ApproveTaskRequest;
import com.smarttask.task.dto.ApiResponse;
import com.smarttask.task.dto.CreateTaskRequest;
import com.smarttask.task.dto.TaskResponse;
import com.smarttask.task.dto.UpdateTaskRequest;
import com.smarttask.task.enums.Priority;
import com.smarttask.task.enums.TaskStatus;
import com.smarttask.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ─── Create task ──────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @RequestHeader("X-Auth-Username") String username,
            @RequestHeader("X-Auth-Role")     String role) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created",
                        taskService.createTask(
                                request, username, role)));
    }

    // ─── Get all tasks with filters ───────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> listTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority   priority,
            @RequestParam(required = false) String     assignedTo,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "10")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String dir) {

        Sort sort = "asc".equalsIgnoreCase(dir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(
                page, Math.min(size, 100), sort);

        return ResponseEntity.ok(ApiResponse.success(
                taskService.findAll(
                        status, priority, assignedTo, pageable)));
    }

    // ─── Get my tasks ─────────────────────────────────────────────────────

    @GetMapping("/my-tasks")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> myTasks(
            @RequestHeader("X-Auth-Username") String username,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                taskService.findMyTasks(username, pageable)));
    }

    // ─── Get task by ID ───────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(taskService.findById(id)));
    }

    // ─── Update task ──────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @RequestHeader("X-Auth-Username") String username,
            @RequestHeader("X-Auth-Role")     String role) {

        return ResponseEntity.ok(ApiResponse.success(
                "Task updated",
                taskService.updateTask(
                        id, request, username, role)));
    }

   

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long id,
            @Valid @RequestBody AssignTaskRequest request,
            @RequestHeader("X-Auth-Username") String username,
            @RequestHeader("X-Auth-Role")     String role) {

        return ResponseEntity.ok(ApiResponse.success(
                "Task assigned to " + request.getUsername(),
                taskService.assignTask(
                        id, request, username, role)));
    }

    // ─── Unassign task (ADMIN / MANAGER only) ─────────────────────────────

    @PatchMapping("/{id}/unassign")
    public ResponseEntity<ApiResponse<TaskResponse>> unassignTask(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Username") String username,
            @RequestHeader("X-Auth-Role")     String role) {

        return ResponseEntity.ok(ApiResponse.success(
                "Task unassigned",
                taskService.unassignTask(id, username, role)));
    }

    // ─── Delete task (ADMIN / MANAGER only) ───────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Username") String username,
            @RequestHeader("X-Auth-Role")     String role) {

        taskService.deleteTask(id, username, role);
        return ResponseEntity.ok(
                ApiResponse.success("Task deleted", null));
    }
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<TaskResponse>>
            approveTask(
                    @PathVariable Long id,
                    @Valid @RequestBody
                            ApproveTaskRequest request,
                    @RequestHeader("X-Auth-Username")
                            String username,
                    @RequestHeader("X-Auth-Role")
                            String role) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Task approved successfully",
                        taskService.approveTask(
                                id, request,
                                username, role)));
    }
    
 
}