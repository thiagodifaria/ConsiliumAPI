package com.sisinnov.pms.repository;

import com.sisinnov.pms.config.TestJpaAuditingConfig;
import com.sisinnov.pms.entity.Project;
import com.sisinnov.pms.entity.Task;
import com.sisinnov.pms.enums.TaskPriority;
import com.sisinnov.pms.enums.TaskStatus;
import com.sisinnov.pms.repository.specification.TaskSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestJpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("TaskRepository Tests")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Project project;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();

        project = new Project();
        project.setName("Test Project");
        project.setStartDate(LocalDate.now());
        project = projectRepository.save(project);

        task1 = createTask("Task 1", TaskStatus.TODO, TaskPriority.HIGH, false);
        task2 = createTask("Task 2", TaskStatus.DOING, TaskPriority.MEDIUM, false);
        task3 = createTask("Task 3", TaskStatus.TODO, TaskPriority.LOW, true);

        taskRepository.saveAll(List.of(task1, task2, task3));
    }

    @Test
    @DisplayName("Should find tasks by project ID and not deleted")
    void shouldFindTasksByProjectId() {
        List<Task> tasks = taskRepository.findByProjectIdAndDeletedFalse(project.getId());

        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(Task::getTitle).containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    @DisplayName("Should not return deleted tasks")
    void shouldNotReturnDeletedTasks() {
        List<Task> tasks = taskRepository.findByProjectIdAndDeletedFalse(project.getId());

        assertThat(tasks).noneMatch(Task::getDeleted);
        assertThat(tasks).extracting(Task::getTitle).doesNotContain("Task 3");
    }

    @Test
    @DisplayName("Should filter by status using specification")
    void shouldFilterByStatus() {
        Specification<Task> spec = TaskSpecification.notDeleted()
                .and(TaskSpecification.withStatus(TaskStatus.TODO));

        List<Task> tasks = taskRepository.findAll(spec);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task 1");
        assertThat(tasks.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    @DisplayName("Should filter by priority using specification")
    void shouldFilterByPriority() {
        Specification<Task> spec = TaskSpecification.notDeleted()
                .and(TaskSpecification.withPriority(TaskPriority.MEDIUM));

        List<Task> tasks = taskRepository.findAll(spec);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task 2");
        assertThat(tasks.get(0).getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    @DisplayName("Should combine filters with specification")
    void shouldCombineFiltersWithSpecification() {
        Specification<Task> spec = TaskSpecification.notDeleted()
                .and(TaskSpecification.withStatus(TaskStatus.TODO))
                .and(TaskSpecification.withPriority(TaskPriority.HIGH));

        List<Task> tasks = taskRepository.findAll(spec);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    @DisplayName("Should filter by project ID using specification")
    void shouldFilterByProjectIdWithSpecification() {
        Specification<Task> spec = TaskSpecification.notDeleted()
                .and(TaskSpecification.withProjectId(project.getId()));

        List<Task> tasks = taskRepository.findAll(spec);

        assertThat(tasks).hasSize(2);
        assertThat(tasks).allMatch(task -> task.getProject().getId().equals(project.getId()));
    }

    @Test
    @DisplayName("Should count active tasks by project")
    void shouldCountActiveTasksByProject() {
        Long count = taskRepository.countByProjectIdAndDeletedFalse(project.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should filter by due date before")
    void shouldFilterByDueDateBefore() {
        LocalDate futureDate = LocalDate.now().plusDays(10);
        task1.setDueDate(LocalDate.now().plusDays(5));
        task2.setDueDate(LocalDate.now().plusDays(15));
        taskRepository.saveAll(List.of(task1, task2));

        Specification<Task> spec = TaskSpecification.notDeleted()
                .and(TaskSpecification.dueDateBefore(futureDate));

        List<Task> tasks = taskRepository.findAll(spec);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    @DisplayName("Should return empty when no tasks match filters")
    void shouldReturnEmptyWhenNoTasksMatchFilters() {
        Specification<Task> spec = TaskSpecification.notDeleted()
                .and(TaskSpecification.withStatus(TaskStatus.DONE));

        List<Task> tasks = taskRepository.findAll(spec);

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("Should exclude deleted tasks by default with notDeleted specification")
    void shouldExcludeDeletedTasksByDefault() {
        Specification<Task> spec = TaskSpecification.notDeleted();

        List<Task> tasks = taskRepository.findAll(spec);

        assertThat(tasks).hasSize(2);
        assertThat(tasks).noneMatch(Task::getDeleted);
    }

    private Task createTask(String title, TaskStatus status, TaskPriority priority, boolean deleted) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description");
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setProject(project);
        task.setDeleted(deleted);
        return task;
    }
}