package com.example.demo.application.ports.in;

/**
 * Input port for deleting an account
 * This defines the contract for the use case
 */
public interface DeleteAccountUseCase {
    boolean deleteAccount(Long id);
}
