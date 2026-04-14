package com.smarttask.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TaskNotAssignedException extends RuntimeException {

    public TaskNotAssignedException(Long taskId) {
        super("Task #" + taskId + " is not assigned to anyone");
    }
}