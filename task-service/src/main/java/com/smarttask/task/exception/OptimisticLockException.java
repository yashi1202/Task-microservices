package com.smarttask.task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException() {
        super("This task was modified by another user. "
                + "Please refresh and try again.");
    }
}