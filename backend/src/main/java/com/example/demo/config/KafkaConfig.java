package com.example.demo.config;

import com.example.demo.domain.notification.Notification;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Kafka configuration for async notification processing using Java 21 Virtual Threads
 * Creates topics, producers, and consumers for the notification system
 * Virtual Threads enable massive concurrency for Kafka consumers without thread pool limitations
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:virtualbank-notifications}")
    private String groupId;

    // Topic names
    public static final String NOTIFICATION_TOPIC = "notification-events";
    public static final String EMAIL_TOPIC = "email-events";

    /**
     * Create notification topic with 3 partitions for parallel processing
     */
    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Create email topic with 2 partitions
     */
    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name(EMAIL_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    /**
     * Producer configuration for sending notifications to Kafka
     * Includes monitoring metrics for Prometheus
     */
    @Bean
    @org.springframework.context.annotation.Primary
    public ProducerFactory<String, Notification> notificationProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Prevent duplicates
        // Monitoring configuration
        config.put(ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG, "");
        config.put(ProducerConfig.METRICS_RECORDING_LEVEL_CONFIG, "INFO");
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Notification> notificationKafkaTemplate() {
        return new KafkaTemplate<>(notificationProducerFactory());
    }

    /**
     * Consumer configuration for processing notifications from Kafka
     */
    @Bean
    @org.springframework.context.annotation.Primary
    public ConsumerFactory<String, Notification> notificationConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.demo.domain.notification");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Notification.class.getName());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit for reliability
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Virtual Thread executor for Kafka notification consumers
     * Provides unlimited scalability for handling high-volume notification processing
     */
    @Bean
    public Executor kafkaNotificationExecutor() {
        Executor executor = Executors.newVirtualThreadPerTaskExecutor();
        log.info("Kafka notification executor initialized with Virtual Threads");
        return executor;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Notification> notificationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Notification> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());
        factory.setConcurrency(10); // Increased from 3 - Virtual Threads enable higher concurrency
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE
        );
        // Use Virtual Threads for listener execution
        factory.getContainerProperties().setListenerTaskExecutor(kafkaNotificationExecutor());
        log.info("Notification Kafka listener factory configured with Virtual Threads (concurrency: 10)");
        return factory;
    }

    /**
     * Producer and Consumer for email events (simple string messages)
     */
    @Bean
    public ProducerFactory<String, String> emailProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> emailKafkaTemplate() {
        return new KafkaTemplate<>(emailProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, String> emailConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-email");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Virtual Thread executor for Kafka email consumers
     * Provides unlimited scalability for handling high-volume email processing
     */
    @Bean
    public Executor kafkaEmailExecutor() {
        Executor executor = Executors.newVirtualThreadPerTaskExecutor();
        log.info("Kafka email executor initialized with Virtual Threads");
        return executor;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> emailKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(emailConsumerFactory());
        factory.setConcurrency(5); // Increased from 2 - Virtual Threads enable higher concurrency
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE
        );
        // Use Virtual Threads for listener execution
        factory.getContainerProperties().setListenerTaskExecutor(kafkaEmailExecutor());
        log.info("Email Kafka listener factory configured with Virtual Threads (concurrency: 5)");
        return factory;
    }
}
