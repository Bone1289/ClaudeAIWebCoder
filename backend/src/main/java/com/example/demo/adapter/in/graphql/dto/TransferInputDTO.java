package com.example.demo.adapter.in.graphql.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferInputDTO(
        UUID toAccountId,
        BigDecimal amount,
        String description,
        UUID categoryId
) {
}
