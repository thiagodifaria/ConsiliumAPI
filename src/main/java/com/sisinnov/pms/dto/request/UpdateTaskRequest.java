package com.sisinnov.pms.dto.request;

import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(
    @Size(min = 5, max = 150, message = "Task title must be between 5 and 150 characters")
    String title,

    String description,

    TaskStatus status,

    TaskPriority priority,

    LocalDate dueDate,

    UUID projectId
) {}