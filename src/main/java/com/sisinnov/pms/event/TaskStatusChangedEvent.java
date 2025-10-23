package com.sisinnov.pms.event;

import com.sisinnov.pms.entity.Task;
import com.sisinnov.pms.enums.TaskStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskStatusChangedEvent(
        UUID taskId,
        UUID projectId,
        TaskStatus oldStatus,
        TaskStatus newStatus,
        LocalDateTime changedAt
) implements Serializable {

    public static TaskStatusChangedEvent of(
            UUID taskId,
            UUID projectId,
            TaskStatus oldStatus,
            TaskStatus newStatus
    ) {
        return new TaskStatusChangedEvent(
                taskId,
                projectId,
                oldStatus,
                newStatus,
                LocalDateTime.now()
        );
    }

    public static TaskStatusChangedEvent from(Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        return new TaskStatusChangedEvent(
                task.getId(),
                task.getProject().getId(),
                oldStatus,
                newStatus,
                LocalDateTime.now()
        );
    }
}