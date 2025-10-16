package com.sisinnov.pms.dto.request;

import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(
    @NotBlank(message = "Task title is required")
    @Size(min = 5, max = 150, message = "Task title must be between 5 and 150 characters")
    String title,

    String description,

    @NotNull(message = "Task status is required")
    TaskStatus status,

    @NotNull(message = "Task priority is required")
    TaskPriority priority,

    LocalDate dueDate,

    @NotNull(message = "Project ID is required")
    UUID projectId
) {}