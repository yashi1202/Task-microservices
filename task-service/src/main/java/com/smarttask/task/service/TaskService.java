package com.smarttask.task.service;

import com.smarttask.task.dto.AssignTaskRequest;
import com.smarttask.task.dto.ApproveTaskRequest;
import com.smarttask.task.dto.CreateTaskRequest;
import com.smarttask.task.dto.TaskResponse;
import com.smarttask.task.dto.UpdateTaskRequest;
import com.smarttask.task.enums.Priority;
import com.smarttask.task.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request,
                             String performedBy,
                             String callerRole);

    TaskResponse updateTask(Long id,
                             UpdateTaskRequest request,
                             String performedBy,
                             String callerRole);

    TaskResponse assignTask(Long id,
                             AssignTaskRequest request,
                             String performedBy,
                             String callerRole);

    TaskResponse unassignTask(Long id,
                               String performedBy,
                               String callerRole);

    TaskResponse findById(Long id);

    Page<TaskResponse> findAll(TaskStatus status,
                                Priority priority,
                                String assignedTo,
                                Pageable pageable);

    Page<TaskResponse> findMyTasks(String username,
                                    Pageable pageable);

    void deleteTask(Long id,
                    String performedBy,
                    String callerRole);
    TaskResponse approveTask(Long id,
            ApproveTaskRequest request,
            String performedBy,
            String callerRole);
}