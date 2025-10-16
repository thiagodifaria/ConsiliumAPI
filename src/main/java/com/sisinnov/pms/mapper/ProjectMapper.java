package com.sisinnov.pms.mapper;

import com.sisinnov.pms.dto.request.CreateProjectRequest;
import com.sisinnov.pms.dto.response.ProjectResponse;
import com.sisinnov.pms.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    Project toEntity(CreateProjectRequest request);

    @Mapping(target = "taskCount", expression = "java(project.getTasks() != null ? project.getTasks().size() : 0)")
    ProjectResponse toResponse(Project project);
}