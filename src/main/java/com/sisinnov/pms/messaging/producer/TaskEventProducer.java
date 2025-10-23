package com.sisinnov.pms.messaging.producer;

import com.sisinnov.pms.config.RabbitMQConfig;
import com.sisinnov.pms.event.TaskCreatedEvent;
import com.sisinnov.pms.event.TaskStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishTaskCreated(TaskCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TASK_EXCHANGE,
                    RabbitMQConfig.TASK_CREATED_KEY,
                    event
            );
            log.info("Event published: TaskCreated for task={}, project={}",
                    event.taskId(), event.projectId());
        } catch (Exception e) {
            log.error("Failed to publish TaskCreatedEvent: event={}, error={}",
                    event, e.getMessage(), e);
        }
    }

    public void publishTaskStatusChanged(TaskStatusChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TASK_EXCHANGE,
                    RabbitMQConfig.TASK_STATUS_CHANGED_KEY,
                    event
            );
            log.info("Event published: TaskStatusChanged for task={}, {} → {}",
                    event.taskId(), event.oldStatus(), event.newStatus());
        } catch (Exception e) {
            log.error("Failed to publish TaskStatusChangedEvent: event={}, error={}",
                    event, e.getMessage(), e);
        }
    }
}