package com.sisinnov.pms.repository;

import com.sisinnov.pms.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    List<Task> findByProjectIdAndDeletedFalse(UUID projectId);

    Long countByProjectIdAndDeletedFalse(UUID projectId);
}