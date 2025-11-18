package com.example.demo.adapter.in.graphql.dto;

import java.time.LocalDateTime;

public record HealthCheckDTO(
        String status,
        LocalDateTime timestamp
) {
}
