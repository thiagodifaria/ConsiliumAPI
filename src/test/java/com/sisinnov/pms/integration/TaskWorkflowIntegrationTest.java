package com.sisinnov.pms.integration;

import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Task Workflow Integration Test - E2E")
class TaskWorkflowIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl;
    private String token;
    private String projectId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        baseUrl = "http://localhost:" + port + "/api/v1";

        String username = "testuser_" + System.currentTimeMillis();

        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "username": "%s",
                        "email": "%s@test.com",
                        "password": "Test@1234"
                    }
                    """.formatted(username, username))
        .when()
                .post(baseUrl + "/auth/register")
        .then()
                .statusCode(201);

        token = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "username": "%s",
                        "password": "Test@1234"
                    }
                    """.formatted(username))
        .when()
                .post(baseUrl + "/auth/login")
        .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("type", equalTo("Bearer"))
                .body("username", equalTo(username))
                .extract()
                .path("token");

        projectId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "Integration Test Project %s",
                        "description": "Project for E2E testing",
                        "startDate": "%s",
                        "endDate": "%s"
                    }
                    """.formatted(System.currentTimeMillis(), LocalDate.now(), LocalDate.now().plusMonths(3)))
        .when()
                .post(baseUrl + "/projects")
        .then()
                .statusCode(201)
                .header("Location", matchesPattern(".*/api/v1/projects/[a-f0-9-]+"))
                .body("id", notNullValue())
                .body("name", containsString("Integration Test Project"))
                .body("taskCount", equalTo(0))
                .extract()
                .path("id");
    }

    @Test
    @DisplayName("Should complete full task workflow successfully")
    void shouldCompleteFullTaskWorkflow() {
        String task1Id = createTask("Task 1 - TODO", TaskStatus.TODO, TaskPriority.HIGH);
        String task2Id = createTask("Task 2 - DOING", TaskStatus.DOING, TaskPriority.MEDIUM);
        String task3Id = createTask("Task 3 - TODO", TaskStatus.TODO, TaskPriority.LOW);

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("projectId", projectId)
        .when()
                .get(baseUrl + "/tasks")
        .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("findAll { it.projectId == '%s' }".formatted(projectId), hasSize(3));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "status": "DONE"
                    }
                    """)
        .when()
                .put(baseUrl + "/tasks/" + task1Id + "/status")
        .then()
                .statusCode(200)
                .body("id", equalTo(task1Id))
                .body("status", equalTo("DONE"))
                .body("title", equalTo("Task 1 - TODO"));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("status", "DONE")
        .when()
                .get(baseUrl + "/tasks")
        .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("find { it.id == '%s' }.status".formatted(task1Id), equalTo("DONE"));

        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .delete(baseUrl + "/tasks/" + task2Id)
        .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("projectId", projectId)
        .when()
                .get(baseUrl + "/tasks")
        .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("findAll { it.id == '%s' }".formatted(task2Id), hasSize(0));
    }

    @Test
    @DisplayName("Should filter tasks by multiple criteria")
    void shouldFilterTasksByMultipleCriteria() {
        createTask("High Priority TODO", TaskStatus.TODO, TaskPriority.HIGH);
        createTask("Medium Priority TODO", TaskStatus.TODO, TaskPriority.MEDIUM);
        createTask("High Priority DONE", TaskStatus.DONE, TaskPriority.HIGH);

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("status", "TODO")
        .when()
                .get(baseUrl + "/tasks")
        .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(2)))
                .body("findAll { it.status != 'TODO' }", hasSize(0));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("priority", "HIGH")
        .when()
                .get(baseUrl + "/tasks")
        .then()
                .statusCode(200)
                .body("findAll { it.priority != 'HIGH' }", hasSize(0));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("status", "TODO")
                .queryParam("priority", "HIGH")
        .when()
                .get(baseUrl + "/tasks")
        .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)))
                .body("find { it.title == 'High Priority TODO' }.status", equalTo("TODO"))
                .body("find { it.title == 'High Priority TODO' }.priority", equalTo("HIGH"));

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("projectId", projectId)
        .when()
                .get(baseUrl + "/tasks")
        .then()
                .statusCode(200)
                .body("$", hasSize(3))
                .body("findAll { it.projectId == '%s' }".formatted(projectId), hasSize(3));
    }

    @Test
    @DisplayName("Should reject unauthorized requests")
    void shouldRejectUnauthorizedRequests() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Unauthorized Task",
                        "status": "TODO",
                        "priority": "HIGH",
                        "projectId": "%s"
                    }
                    """.formatted(projectId))
        .when()
                .post(baseUrl + "/tasks")
        .then()
                .statusCode(401);

        given()
        .when()
                .get(baseUrl + "/tasks")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Should validate business rules")
    void shouldValidateBusinessRules() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Task with invalid project",
                        "status": "TODO",
                        "priority": "HIGH",
                        "projectId": "00000000-0000-0000-0000-000000000000"
                    }
                    """)
        .when()
                .post(baseUrl + "/tasks")
        .then()
                .statusCode(404)
                .body("message", containsString("not found"));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "status": "DONE"
                    }
                    """)
        .when()
                .put(baseUrl + "/tasks/00000000-0000-0000-0000-000000000000/status")
        .then()
                .statusCode(404);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "",
                        "projectId": "%s"
                    }
                    """.formatted(projectId))
        .when()
                .post(baseUrl + "/tasks")
        .then()
                .statusCode(400);
    }

    private String createTask(String title, TaskStatus status, TaskPriority priority) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "%s",
                        "description": "Integration test task",
                        "status": "%s",
                        "priority": "%s",
                        "dueDate": "%s",
                        "projectId": "%s"
                    }
                    """.formatted(title, status, priority, LocalDate.now().plusDays(7), projectId))
        .when()
                .post(baseUrl + "/tasks")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", equalTo(title))
                .body("status", equalTo(status.toString()))
                .body("priority", equalTo(priority.toString()))
                .body("projectId", equalTo(projectId))
                .extract()
                .path("id");
    }
}