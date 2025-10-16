package com.sisinnov.pms.service;

import com.sisinnov.pms.dto.request.CreateTaskRequest;
import com.sisinnov.pms.dto.response.TaskResponse;
import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse create(CreateTaskRequest request);

    List<TaskResponse> findAll(TaskStatus status, TaskPriority priority, UUID projectId);

    TaskResponse updateStatus(UUID id, TaskStatus newStatus);

    void delete(UUID id);
}