package com.example.demo.application.ports.in;

import java.util.UUID;

/**
 * Input port for deleting an account
 * This defines the contract for the use case
 */
public interface DeleteAccountUseCase {
    boolean deleteAccount(UUID id);
}
