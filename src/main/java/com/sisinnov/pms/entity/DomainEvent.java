package com.sisinnov.pms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
    name = "domain_events",
    indexes = {
        @Index(name = "idx_domain_events_aggregate", columnList = "aggregate_type, aggregate_id"),
        @Index(name = "idx_domain_events_type", columnList = "event_type"),
        @Index(name = "idx_domain_events_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_domain_events_aggregate_time", columnList = "aggregate_id, occurred_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", nullable = false, columnDefinition = "TEXT")
    private Map<String, Object> eventData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "TEXT")
    private Map<String, Object> metadata;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 1L;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    public enum AggregateType {
        TASK,
        PROJECT,
        USER
    }

    public static DomainEvent forTask(String eventType, UUID taskId, Map<String, Object> data, Map<String, Object> metadata) {
        return DomainEvent.builder()
                .eventType(eventType)
                .aggregateType(AggregateType.TASK)
                .aggregateId(taskId)
                .eventData(data)
                .metadata(metadata)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public static DomainEvent forProject(String eventType, UUID projectId, Map<String, Object> data, Map<String, Object> metadata) {
        return DomainEvent.builder()
                .eventType(eventType)
                .aggregateType(AggregateType.PROJECT)
                .aggregateId(projectId)
                .eventData(data)
                .metadata(metadata)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public static DomainEvent forUser(String eventType, UUID userId, Map<String, Object> data, Map<String, Object> metadata) {
        return DomainEvent.builder()
                .eventType(eventType)
                .aggregateType(AggregateType.USER)
                .aggregateId(userId)
                .eventData(data)
                .metadata(metadata)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}