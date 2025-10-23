package com.sisinnov.pms.event;

import com.sisinnov.pms.entity.Task;
import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskCreatedEvent(
        UUID taskId,
        UUID projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime createdAt
) implements Serializable {

    public static TaskCreatedEvent from(Task task) {
        return new TaskCreatedEvent(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt()
        );
    }
}