package com.example.demo.adapter.in.graphql.dto;

public record UpdateAccountInputDTO(
        String firstName,
        String lastName,
        String nationality,
        String accountType
) {
}
