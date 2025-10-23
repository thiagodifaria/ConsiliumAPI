package com.sisinnov.pms.controller;

import com.sisinnov.pms.entity.DomainEvent;
import com.sisinnov.pms.service.EventStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
@Tag(name = "Admin - Event Store", description = "Endpoints administrativos para auditoria e Event Sourcing")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final EventStoreService eventStoreService;

    @GetMapping("/aggregate/{aggregateId}")
    @Operation(
            summary = "Busca histórico completo de um aggregate",
            description = "Retorna TODOS os eventos de uma Task ou Project, ordenados cronologicamente. Use para auditoria completa."
    )
    public ResponseEntity<List<DomainEvent>> getAggregateHistory(
            @Parameter(description = "ID do aggregate (Task ou Project)")
            @PathVariable UUID aggregateId
    ) {
        log.info("ADMIN: Fetching complete history for aggregate: {}", aggregateId);

        List<DomainEvent> events = eventStoreService.getAggregateHistory(aggregateId);

        log.debug("ADMIN: Found {} events for aggregate {}", events.size(), aggregateId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/aggregate/{aggregateId}/recent")
    @Operation(
            summary = "Busca histórico recente de um aggregate",
            description = "Retorna os últimos N eventos de uma Task ou Project (default: 10). Use para timelines."
    )
    public ResponseEntity<List<DomainEvent>> getRecentHistory(
            @Parameter(description = "ID do aggregate (Task ou Project)")
            @PathVariable UUID aggregateId,

            @Parameter(description = "Quantidade máxima de eventos a retornar")
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("ADMIN: Fetching {} recent events for aggregate: {}", limit, aggregateId);

        List<DomainEvent> events = eventStoreService.getRecentHistory(aggregateId, limit);

        log.debug("ADMIN: Found {} recent events for aggregate {}", events.size(), aggregateId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/type/{eventType}")
    @Operation(
            summary = "Busca eventos por tipo e período",
            description = "Retorna eventos de um tipo específico (ex: TASK_CREATED) em um intervalo de datas. Use para relatórios."
    )
    public ResponseEntity<List<DomainEvent>> getEventsByType(
            @Parameter(description = "Tipo do evento (TASK_CREATED, TASK_STATUS_CHANGED, PROJECT_CREATED, etc)")
            @PathVariable String eventType,

            @Parameter(description = "Data início (ISO 8601: 2024-01-01T00:00:00)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,

            @Parameter(description = "Data fim (ISO 8601: 2024-12-31T23:59:59)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        log.info("ADMIN: Fetching events of type {} between {} and {}", eventType, start, end);

        List<DomainEvent> events = eventStoreService.getEventsByTypeAndPeriod(eventType, start, end);

        log.debug("ADMIN: Found {} events of type {} in period", events.size(), eventType);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Estatísticas do Event Store",
            description = "Retorna métricas agregadas: total de eventos por tipo. Use para dashboards."
    )
    public ResponseEntity<Map<String, Long>> getEventStats() {
        log.info("ADMIN: Fetching event statistics");

        Map<String, Long> stats = Map.of(
                "TASK_CREATED", eventStoreService.countEventsByType(EventStoreService.EventTypes.TASK_CREATED),
                "TASK_UPDATED", eventStoreService.countEventsByType(EventStoreService.EventTypes.TASK_UPDATED),
                "TASK_STATUS_CHANGED", eventStoreService.countEventsByType(EventStoreService.EventTypes.TASK_STATUS_CHANGED),
                "TASK_DELETED", eventStoreService.countEventsByType(EventStoreService.EventTypes.TASK_DELETED),
                "PROJECT_CREATED", eventStoreService.countEventsByType(EventStoreService.EventTypes.PROJECT_CREATED),
                "PROJECT_UPDATED", eventStoreService.countEventsByType(EventStoreService.EventTypes.PROJECT_UPDATED),
                "PROJECT_DELETED", eventStoreService.countEventsByType(EventStoreService.EventTypes.PROJECT_DELETED)
        );

        log.debug("ADMIN: Event stats: {}", stats);
        return ResponseEntity.ok(stats);
    }
}