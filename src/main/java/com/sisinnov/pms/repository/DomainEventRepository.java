package com.sisinnov.pms.repository;

import com.sisinnov.pms.entity.DomainEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DomainEventRepository extends JpaRepository<DomainEvent, UUID> {

    List<DomainEvent> findByAggregateIdOrderByOccurredAtAsc(UUID aggregateId);

    List<DomainEvent> findByAggregateIdAndEventTypeOrderByOccurredAtAsc(
            UUID aggregateId,
            String eventType
    );

    List<DomainEvent> findByAggregateTypeOrderByOccurredAtDesc(
            DomainEvent.AggregateType aggregateType
    );

    List<DomainEvent> findByOccurredAtBetweenOrderByOccurredAtAsc(
            LocalDateTime start,
            LocalDateTime end
    );

    List<DomainEvent> findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(
            UUID aggregateId,
            Long afterVersion
    );

    long countByEventType(String eventType);

    @Query("SELECT e FROM DomainEvent e WHERE e.aggregateId = :aggregateId " +
           "ORDER BY e.occurredAt DESC LIMIT :limit")
    List<DomainEvent> findRecentByAggregateId(
            @Param("aggregateId") UUID aggregateId,
            @Param("limit") int limit
    );

    @Query("SELECT e FROM DomainEvent e WHERE e.eventType = :eventType " +
           "AND e.occurredAt BETWEEN :start AND :end " +
           "ORDER BY e.occurredAt ASC")
    List<DomainEvent> findByEventTypeAndPeriod(
            @Param("eventType") String eventType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(MAX(e.version), 0) FROM DomainEvent e " +
           "WHERE e.aggregateId = :aggregateId")
    Long findLatestVersion(@Param("aggregateId") UUID aggregateId);
}