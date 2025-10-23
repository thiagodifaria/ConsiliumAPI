package com.sisinnov.pms.service;

import com.sisinnov.pms.dto.request.CreateTaskRequest;
import com.sisinnov.pms.dto.response.TaskResponse;
import com.sisinnov.pms.entity.Project;
import com.sisinnov.pms.entity.Task;
import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;
import com.sisinnov.pms.exception.BusinessException;
import com.sisinnov.pms.exception.ResourceNotFoundException;
import com.sisinnov.pms.mapper.TaskMapper;
import com.sisinnov.pms.repository.ProjectRepository;
import com.sisinnov.pms.repository.TaskRepository;
import com.sisinnov.pms.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TaskService Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private com.sisinnov.pms.messaging.producer.TaskEventProducer taskEventProducer;

    @Mock
    private com.sisinnov.pms.service.EventStoreService eventStoreService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private CreateTaskRequest createRequest;
    private Task task;
    private Project project;
    private TaskResponse taskResponse;
    private UUID projectId;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        project = new Project();
        project.setId(projectId);
        project.setName("Test Project");

        createRequest = new CreateTaskRequest(
                "Test Task",
                "Test Description",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(7),
                projectId
        );

        task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setProject(project);
        task.setDeleted(false);

        taskResponse = new TaskResponse(
                taskId,
                "Test Task",
                "Test Description",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(7),
                projectId,
                "Test Project",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create task when project exists")
    void shouldCreateTaskWhenProjectExists() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskMapper.toEntity(any(CreateTaskRequest.class))).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(eventStoreService.saveTaskEvent(anyString(), any(UUID.class), any(), any())).thenReturn(null);
        doNothing().when(taskEventProducer).publishTaskCreated(any());

        TaskResponse response = taskService.create(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Test Task");
        assertThat(response.projectId()).isEqualTo(projectId);

        verify(projectRepository).findById(projectId);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Should throw exception when project not found")
    void shouldThrowExceptionWhenProjectNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should filter tasks by status")
    void shouldFilterTasksByStatus() {
        when(taskRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        List<TaskResponse> tasks = taskService.findAll(TaskStatus.TODO, null, null);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).status()).isEqualTo(TaskStatus.TODO);

        verify(taskRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should filter tasks by priority")
    void shouldFilterTasksByPriority() {
        when(taskRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        List<TaskResponse> tasks = taskService.findAll(null, TaskPriority.HIGH, null);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).priority()).isEqualTo(TaskPriority.HIGH);

        verify(taskRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should combine multiple filters")
    void shouldCombineMultipleFilters() {
        when(taskRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        List<TaskResponse> tasks = taskService.findAll(TaskStatus.TODO, TaskPriority.HIGH, projectId);

        assertThat(tasks).hasSize(1);

        verify(taskRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should update task status")
    void shouldUpdateTaskStatus() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(taskResponse);
        when(eventStoreService.saveTaskEvent(anyString(), any(UUID.class), any(), any())).thenReturn(null);
        doNothing().when(taskEventProducer).publishTaskStatusChanged(any());

        TaskResponse response = taskService.updateStatus(taskId, TaskStatus.DOING);

        assertThat(response).isNotNull();

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("Should not update deleted task")
    void shouldNotUpdateDeletedTask() {
        task.setDeleted(true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.updateStatus(taskId, TaskStatus.DOING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot update status of deleted task");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should soft delete task")
    void shouldSoftDeleteTask() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.delete(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);
        assertThat(task.getDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when task not found for deletion")
    void shouldThrowExceptionWhenTaskNotFoundForDeletion() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task");

        verify(taskRepository, never()).save(any(Task.class));
    }
}