package com.sisinnov.pms.service;

import com.sisinnov.pms.entity.DomainEvent;
import com.sisinnov.pms.repository.DomainEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventStoreService {

    private final DomainEventRepository eventRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public DomainEvent saveTaskEvent(
            String eventType,
            UUID taskId,
            Map<String, Object> eventData,
            Map<String, Object> metadata
    ) {
        log.debug("EVENT_STORE: Saving {} for task: {}", eventType, taskId);

        Long currentVersion = eventRepository.findLatestVersion(taskId);
        Long nextVersion = currentVersion + 1;

        DomainEvent event = DomainEvent.builder()
                .eventType(eventType)
                .aggregateType(DomainEvent.AggregateType.TASK)
                .aggregateId(taskId)
                .eventData(eventData)
                .metadata(enrichMetadata(metadata))
                .version(nextVersion)
                .occurredAt(LocalDateTime.now())
                .build();

        event = eventRepository.save(event);

        log.info("EVENT_STORE: Saved {} v{} for task: {}", eventType, nextVersion, taskId);
        return event;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public DomainEvent saveProjectEvent(
            String eventType,
            UUID projectId,
            Map<String, Object> eventData,
            Map<String, Object> metadata
    ) {
        log.debug("EVENT_STORE: Saving {} for project: {}", eventType, projectId);

        Long currentVersion = eventRepository.findLatestVersion(projectId);
        Long nextVersion = currentVersion + 1;

        DomainEvent event = DomainEvent.builder()
                .eventType(eventType)
                .aggregateType(DomainEvent.AggregateType.PROJECT)
                .aggregateId(projectId)
                .eventData(eventData)
                .metadata(enrichMetadata(metadata))
                .version(nextVersion)
                .occurredAt(LocalDateTime.now())
                .build();

        event = eventRepository.save(event);

        log.info("EVENT_STORE: Saved {} v{} for project: {}", eventType, nextVersion, projectId);
        return event;
    }

    @Transactional(readOnly = true)
    public List<DomainEvent> getAggregateHistory(UUID aggregateId) {
        log.debug("EVENT_STORE: Fetching history for aggregate: {}", aggregateId);
        return eventRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId);
    }

    @Transactional(readOnly = true)
    public List<DomainEvent> getRecentHistory(UUID aggregateId, int limit) {
        log.debug("EVENT_STORE: Fetching {} recent events for aggregate: {}", limit, aggregateId);
        return eventRepository.findRecentByAggregateId(aggregateId, limit);
    }

    @Transactional(readOnly = true)
    public List<DomainEvent> getEventsByTypeAndPeriod(
            String eventType,
            LocalDateTime start,
            LocalDateTime end
    ) {
        log.debug("EVENT_STORE: Fetching {} events between {} and {}", eventType, start, end);
        return eventRepository.findByEventTypeAndPeriod(eventType, start, end);
    }

    @Transactional(readOnly = true)
    public long countEventsByType(String eventType) {
        return eventRepository.countByEventType(eventType);
    }

    private Map<String, Object> enrichMetadata(Map<String, Object> metadata) {
        Map<String, Object> enriched = new HashMap<>(metadata != null ? metadata : new HashMap<>());

        enriched.putIfAbsent("timestamp", LocalDateTime.now().toString());

        enriched.putIfAbsent("appVersion", "2.0.0");


        return enriched;
    }

    public static class EventTypes {
        public static final String TASK_CREATED = "TASK_CREATED";
        public static final String TASK_UPDATED = "TASK_UPDATED";
        public static final String TASK_STATUS_CHANGED = "TASK_STATUS_CHANGED";
        public static final String TASK_DELETED = "TASK_DELETED";

        public static final String PROJECT_CREATED = "PROJECT_CREATED";
        public static final String PROJECT_UPDATED = "PROJECT_UPDATED";
        public static final String PROJECT_DELETED = "PROJECT_DELETED";

        public static final String USER_REGISTERED = "USER_REGISTERED";
        public static final String USER_LOGGED_IN = "USER_LOGGED_IN";
        public static final String USER_LOGGED_OUT = "USER_LOGGED_OUT";
    }
}