package com.sisinnov.pms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sisinnov.pms.dto.request.CreateProjectRequest;
import com.sisinnov.pms.dto.request.RegisterRequest;
import com.sisinnov.pms.repository.ProjectRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProjectController Integration Tests")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        projectRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest registerRequest = new RegisterRequest(
                "testuser",
                "test@email.com",
                "password123"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("Should create project with valid data")
    void shouldCreateProjectWithValidData() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Test Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(3)
        );

        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    @DisplayName("Should return 400 when invalid project data")
    void shouldReturn400WhenInvalidProjectData() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
                "",
                "Description",
                LocalDate.now(),
                null
        );

        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when duplicate project name")
    void shouldReturn400WhenDuplicateProjectName() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
                "Duplicate Project",
                "Description",
                LocalDate.now(),
                LocalDate.now().plusMonths(3)
        );

        mockMvc.perform(post("/api/v1/projects")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project name already exists: Duplicate Project"));
    }

    @Test
    @DisplayName("Should list all projects")
    void shouldListAllProjects() throws Exception {
        CreateProjectRequest request1 = new CreateProjectRequest(
                "Project 1",
                "Description 1",
                LocalDate.now(),
                null
        );

        CreateProjectRequest request2 = new CreateProjectRequest(
                "Project 2",
                "Description 2",
                LocalDate.now(),
                null
        );

        mockMvc.perform(post("/api/v1/projects")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        mockMvc.perform(post("/api/v1/projects")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/v1/projects")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should find project by ID")
    void shouldFindProjectById() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Description",
                LocalDate.now(),
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/projects")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        String projectId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/v1/projects/" + projectId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    @DisplayName("Should return 404 when project not found")
    void shouldReturn404WhenProjectNotFound() throws Exception {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/api/v1/projects/" + nonExistentId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when no authentication")
    void shouldReturn401WhenNoAuthentication() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
                "Test Project",
                "Description",
                LocalDate.now(),
                null
        );

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}