package com.example.demo.adapter.in.graphql.dto;

public record CreateAccountInputDTO(
        String firstName,
        String lastName,
        String nationality,
        String accountType
) {
}
