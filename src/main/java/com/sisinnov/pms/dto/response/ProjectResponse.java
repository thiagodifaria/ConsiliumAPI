package com.sisinnov.pms.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        int taskCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}