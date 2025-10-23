package com.sisinnov.pms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_EXCHANGE = "task.exchange";
    public static final String AUDIT_EXCHANGE = "audit.exchange";

    public static final String TASK_CREATED_QUEUE = "task.created.queue";
    public static final String TASK_STATUS_CHANGED_QUEUE = "task.status.changed.queue";
    public static final String AUDIT_LOG_QUEUE = "audit.log.queue";

    public static final String TASK_CREATED_KEY = "task.created";
    public static final String TASK_STATUS_CHANGED_KEY = "task.status.changed";
    public static final String AUDIT_LOG_KEY = "audit.log";

    public static final String TASK_DLX = "task.dlx";
    public static final String TASK_DLQ = "task.dlq";

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(TASK_EXCHANGE);
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE);
    }

    @Bean
    public DirectExchange taskDeadLetterExchange() {
        return new DirectExchange(TASK_DLX);
    }

    @Bean
    public Queue taskCreatedQueue() {
        return QueueBuilder.durable(TASK_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", TASK_DLX)
                .withArgument("x-dead-letter-routing-key", "task.created.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    public Queue taskStatusChangedQueue() {
        return QueueBuilder.durable(TASK_STATUS_CHANGED_QUEUE)
                .withArgument("x-dead-letter-exchange", TASK_DLX)
                .withArgument("x-dead-letter-routing-key", "task.status.changed.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    public Queue auditLogQueue() {
        return QueueBuilder.durable(AUDIT_LOG_QUEUE)
                .build();
    }

    @Bean
    public Queue taskDeadLetterQueue() {
        return QueueBuilder.durable(TASK_DLQ).build();
    }

    @Bean
    public Binding taskCreatedBinding() {
        return BindingBuilder
                .bind(taskCreatedQueue())
                .to(taskExchange())
                .with(TASK_CREATED_KEY);
    }

    @Bean
    public Binding taskStatusChangedBinding() {
        return BindingBuilder
                .bind(taskStatusChangedQueue())
                .to(taskExchange())
                .with(TASK_STATUS_CHANGED_KEY);
    }

    @Bean
    public Binding auditLogBinding() {
        return BindingBuilder
                .bind(auditLogQueue())
                .to(auditExchange())
                .with("audit.*");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(taskDeadLetterQueue())
                .to(taskDeadLetterExchange())
                .with("#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}