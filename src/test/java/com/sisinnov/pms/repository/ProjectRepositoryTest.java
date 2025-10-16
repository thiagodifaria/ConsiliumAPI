package com.sisinnov.pms.repository;

import com.sisinnov.pms.config.TestJpaAuditingConfig;
import com.sisinnov.pms.entity.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestJpaAuditingConfig.class)
@ActiveProfiles("test")
@DisplayName("ProjectRepository Tests")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    private Project project;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();

        project = new Project();
        project.setName("Test Project");
        project.setDescription("Description");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusMonths(3));

        projectRepository.save(project);
    }

    @Test
    @DisplayName("Should find project by name ignore case")
    void shouldFindByNameIgnoreCase() {
        Optional<Project> found = projectRepository.findByNameIgnoreCase("test project");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Project");
    }

    @Test
    @DisplayName("Should find project by exact case")
    void shouldFindByExactCase() {
        Optional<Project> found = projectRepository.findByNameIgnoreCase("Test Project");

        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("Should find project by uppercase")
    void shouldFindByUppercase() {
        Optional<Project> found = projectRepository.findByNameIgnoreCase("TEST PROJECT");

        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("Should return empty when project not found")
    void shouldReturnEmptyWhenProjectNotFound() {
        Optional<Project> found = projectRepository.findByNameIgnoreCase("Non Existent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check existence by name case insensitive")
    void shouldCheckExistenceByName() {
        boolean exists = projectRepository.existsByNameIgnoreCase("test project");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when name does not exist")
    void shouldReturnFalseWhenNameDoesNotExist() {
        boolean exists = projectRepository.existsByNameIgnoreCase("Non Existent");

        assertThat(exists).isFalse();
    }
}