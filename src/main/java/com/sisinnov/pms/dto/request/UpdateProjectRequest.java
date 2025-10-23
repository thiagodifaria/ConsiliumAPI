package com.sisinnov.pms.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProjectRequest(
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    String name,

    String description,

    LocalDate startDate,

    LocalDate endDate
) {}