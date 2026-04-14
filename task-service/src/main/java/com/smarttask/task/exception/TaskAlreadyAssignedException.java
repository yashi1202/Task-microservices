package com.smarttask.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TaskAlreadyAssignedException extends RuntimeException {

    public TaskAlreadyAssignedException(String username) {
        super("Task is already assigned to '" + username + "'");
    }
}