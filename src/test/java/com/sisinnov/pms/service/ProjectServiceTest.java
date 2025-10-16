package com.sisinnov.pms.service;

import com.sisinnov.pms.dto.request.CreateProjectRequest;
import com.sisinnov.pms.dto.response.ProjectResponse;
import com.sisinnov.pms.entity.Project;
import com.sisinnov.pms.exception.BusinessException;
import com.sisinnov.pms.mapper.ProjectMapper;
import com.sisinnov.pms.repository.ProjectRepository;
import com.sisinnov.pms.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private CreateProjectRequest createRequest;
    private Project project;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(3);

        createRequest = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                startDate,
                endDate
        );

        project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setStartDate(startDate);
        project.setEndDate(endDate);

        projectResponse = new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create project successfully")
    void shouldCreateProjectSuccessfully() {
        when(projectRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(projectMapper.toEntity(any(CreateProjectRequest.class))).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(projectResponse);

        ProjectResponse response = projectService.create(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Test Project");
        assertThat(response.description()).isEqualTo("Test Description");

        verify(projectRepository).existsByNameIgnoreCase("Test Project");
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Should throw exception when duplicate project name")
    void shouldThrowExceptionWhenDuplicateName() {
        when(projectRepository.existsByNameIgnoreCase("Test Project")).thenReturn(true);

        assertThatThrownBy(() -> projectService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Project name already exists");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Should throw exception when start date after end date")
    void shouldThrowExceptionWhenStartDateAfterEndDate() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusDays(1);

        CreateProjectRequest invalidRequest = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                startDate,
                endDate
        );

        when(projectRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);

        assertThatThrownBy(() -> projectService.create(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Start date must be before end date");

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Should find all projects")
    void shouldFindAllProjects() {
        Project project2 = new Project();
        project2.setId(UUID.randomUUID());
        project2.setName("Project 2");

        ProjectResponse response2 = new ProjectResponse(
                project2.getId(),
                "Project 2",
                null,
                LocalDate.now(),
                null,
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(projectRepository.findAll()).thenReturn(Arrays.asList(project, project2));
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);
        when(projectMapper.toResponse(project2)).thenReturn(response2);

        List<ProjectResponse> projects = projectService.findAll();

        assertThat(projects).hasSize(2);
        assertThat(projects.get(0).name()).isEqualTo("Test Project");
        assertThat(projects.get(1).name()).isEqualTo("Project 2");

        verify(projectRepository).findAll();
    }

    @Test
    @DisplayName("Should find project by ID")
    void shouldFindProjectById() {
        UUID id = project.getId();

        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        Optional<ProjectResponse> result = projectService.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Test Project");

        verify(projectRepository).findById(id);
    }

    @Test
    @DisplayName("Should return empty when project not found")
    void shouldReturnEmptyWhenProjectNotFound() {
        UUID id = UUID.randomUUID();

        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ProjectResponse> result = projectService.findById(id);

        assertThat(result).isEmpty();

        verify(projectRepository).findById(id);
        verify(projectMapper, never()).toResponse(any(Project.class));
    }
}