package com.sisinnov.pms.messaging.consumer;

import com.sisinnov.pms.config.RabbitMQConfig;
import com.sisinnov.pms.event.TaskCreatedEvent;
import com.sisinnov.pms.event.TaskStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventConsumer {


    @RabbitListener(queues = RabbitMQConfig.TASK_CREATED_QUEUE)
    public void handleTaskCreated(TaskCreatedEvent event) {
        try {
            log.info("Processing TaskCreatedEvent: taskId={}, projectId={}, title='{}'",
                    event.taskId(), event.projectId(), event.title());

            Thread.sleep(100);

            log.info("TaskCreatedEvent processed successfully: taskId={}", event.taskId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while processing TaskCreatedEvent: {}", event, e);
            throw new RuntimeException("Processing interrupted", e);
        } catch (Exception e) {
            log.error("Error processing TaskCreatedEvent: {}", event, e);
            throw new RuntimeException("Processing failed", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.TASK_STATUS_CHANGED_QUEUE)
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        try {
            log.info("Processing TaskStatusChangedEvent: taskId={}, {} → {}",
                    event.taskId(), event.oldStatus(), event.newStatus());

            Thread.sleep(50);

            if (event.newStatus().name().equals("COMPLETED")) {
                log.info("Task completed! Triggering completion workflows for task={}",
                        event.taskId());
            }

            log.info("TaskStatusChangedEvent processed successfully: taskId={}", event.taskId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while processing TaskStatusChangedEvent: {}", event, e);
            throw new RuntimeException("Processing interrupted", e);
        } catch (Exception e) {
            log.error("Error processing TaskStatusChangedEvent: {}", event, e);
            throw new RuntimeException("Processing failed", e);
        }
    }
}