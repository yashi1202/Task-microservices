package com.smarttask.notification.service;

import com.smarttask.notification.event.TaskEvent;

public interface NotificationService {

    void processEvent(TaskEvent event);
}