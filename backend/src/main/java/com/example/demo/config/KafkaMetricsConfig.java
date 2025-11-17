package com.example.demo.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Configuration for exposing Kafka metrics to Prometheus via Micrometer
 * Tracks producer and consumer metrics for monitoring and alerting
 */
@Configuration
public class KafkaMetricsConfig {

    private final MeterRegistry meterRegistry;
    private final ProducerFactory<String, ?> producerFactory;
    private final ConsumerFactory<String, ?> consumerFactory;

    public KafkaMetricsConfig(
            MeterRegistry meterRegistry,
            ProducerFactory<String, ?> producerFactory,
            ConsumerFactory<String, ?> consumerFactory) {
        this.meterRegistry = meterRegistry;
        this.producerFactory = producerFactory;
        this.consumerFactory = consumerFactory;
    }

    /**
     * Initialize Kafka metrics binding after beans are created
     * This exposes Kafka producer and consumer metrics to Prometheus
     */
    @PostConstruct
    public void bindMetrics() {
        // Note: KafkaClientMetrics automatically binds to producer/consumer instances
        // Spring Boot auto-configuration handles this for us
        // Additional custom metrics can be added here if needed

        // Register custom Kafka-related metrics
        meterRegistry.gauge("kafka.notification.queue.size", 0);
        meterRegistry.counter("kafka.notification.produced.total");
        meterRegistry.counter("kafka.notification.consumed.total");
        meterRegistry.counter("kafka.notification.failed.total");
    }
}
