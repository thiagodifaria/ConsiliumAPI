package com.sisinnov.pms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sisinnov.pms.dto.request.CreateProjectRequest;
import com.sisinnov.pms.dto.request.CreateTaskRequest;
import com.sisinnov.pms.dto.request.RegisterRequest;
import com.sisinnov.pms.dto.request.UpdateTaskStatusRequest;
import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;
import com.sisinnov.pms.repository.ProjectRepository;
import com.sisinnov.pms.repository.TaskRepository;
import com.sisinnov.pms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TaskController Integration Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;
    private String projectId;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest registerRequest = new RegisterRequest(
                "testuser",
                "test@email.com",
                "password123"
        );

        MvcResult authResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn();

        String authResponse = authResult.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(authResponse).get("token").asText();

        CreateProjectRequest projectRequest = new CreateProjectRequest(
                "Test Project",
                "Description",
                LocalDate.now(),
                null
        );

        MvcResult projectResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andReturn();

        String projectResponse = projectResult.getResponse().getContentAsString();
        projectId = objectMapper.readTree(projectResponse).get("id").asText();
    }

    @Test
    @DisplayName("Should create task with valid project")
    void shouldCreateTaskWithValidProject() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "Test Task",
                "Task Description",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(7),
                UUID.fromString(projectId)
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.projectId").value(projectId));
    }

    @Test
    @DisplayName("Should return 404 when project not found on task creation")
    void shouldReturn404WhenProjectNotFoundOnTaskCreation() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "Test Task",
                "Description",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                LocalDate.now(),
                UUID.fromString("00000000-0000-0000-0000-000000000000")
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should filter tasks by status")
    void shouldFilterTasksByStatus() throws Exception {
        createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH);
        createTask("Task 2", TaskStatus.DOING, TaskPriority.MEDIUM);
        createTask("Task 3", TaskStatus.TODO, TaskPriority.LOW);

        mockMvc.perform(get("/api/v1/tasks?status=TODO")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", everyItem(is("TODO"))));
    }

    @Test
    @DisplayName("Should filter tasks by priority")
    void shouldFilterTasksByPriority() throws Exception {
        createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH);
        createTask("Task 2", TaskStatus.DOING, TaskPriority.HIGH);
        createTask("Task 3", TaskStatus.TODO, TaskPriority.LOW);

        mockMvc.perform(get("/api/v1/tasks?priority=HIGH")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].priority", everyItem(is("HIGH"))));
    }

    @Test
    @DisplayName("Should filter tasks by project ID")
    void shouldFilterTasksByProjectId() throws Exception {
        createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH);
        createTask("Task 2", TaskStatus.DOING, TaskPriority.MEDIUM);

        mockMvc.perform(get("/api/v1/tasks?projectId=" + projectId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].projectId", everyItem(is(projectId))));
    }

    @Test
    @DisplayName("Should combine multiple filters")
    void shouldCombineMultipleFilters() throws Exception {
        createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH);
        createTask("Task 2", TaskStatus.TODO, TaskPriority.LOW);
        createTask("Task 3", TaskStatus.DOING, TaskPriority.HIGH);

        mockMvc.perform(get("/api/v1/tasks?status=TODO&priority=HIGH")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Task 1"));
    }

    @Test
    @DisplayName("Should update task status")
    void shouldUpdateTaskStatus() throws Exception {
        String taskId = createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH);

        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.DOING);

        mockMvc.perform(put("/api/v1/tasks/" + taskId + "/status")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOING"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent task")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.DONE);

        mockMvc.perform(put("/api/v1/tasks/00000000-0000-0000-0000-000000000000/status")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should soft delete task")
    void shouldSoftDeleteTask() throws Exception {
        String taskId = createTask("Task to Delete", TaskStatus.TODO, TaskPriority.LOW);

        mockMvc.perform(delete("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should not return deleted tasks in list")
    void shouldNotReturnDeletedTasksInList() throws Exception {
        String taskId1 = createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH);
        createTask("Task 2", TaskStatus.TODO, TaskPriority.MEDIUM);

        mockMvc.perform(delete("/api/v1/tasks/" + taskId1)
                .header("Authorization", "Bearer " + jwtToken));

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Task 2"));
    }

    private String createTask(String title, TaskStatus status, TaskPriority priority) throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                title,
                "Description",
                status,
                priority,
                LocalDate.now().plusDays(7),
                UUID.fromString(projectId)
        );

        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }
}