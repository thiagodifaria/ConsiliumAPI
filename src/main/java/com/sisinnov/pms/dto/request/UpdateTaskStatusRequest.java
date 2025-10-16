package com.sisinnov.pms.dto.request;

import com.sisinnov.pms.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
    @NotNull(message = "Task status is required")
    TaskStatus status
) {}