package com.example.demo.application.ports.in;

/**
 * Input port for deleting a user
 * This defines the contract for the use case
 */
public interface DeleteUserUseCase {
    boolean deleteUser(Long id);
}
