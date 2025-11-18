package com.example.demo.adapter.in.graphql.dto;

public record AuthResponseDTO(
        String token,
        UserDTO user
) {
}
