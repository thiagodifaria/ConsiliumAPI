package com.sisinnov.pms.mapper;

import com.sisinnov.pms.dto.request.CreateTaskRequest;
import com.sisinnov.pms.dto.response.TaskResponse;
import com.sisinnov.pms.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "project", ignore = true)
    Task toEntity(CreateTaskRequest request);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    TaskResponse toResponse(Task task);
}