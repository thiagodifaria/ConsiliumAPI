package com.sisinnov.pms.dto.response;

import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        UUID projectId,
        String projectName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}