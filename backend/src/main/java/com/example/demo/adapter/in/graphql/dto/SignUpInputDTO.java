package com.example.demo.adapter.in.graphql.dto;

public record SignUpInputDTO(
        String email,
        String username,
        String password,
        String firstName,
        String lastName
) {
}
