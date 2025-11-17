package com.example.demo.application.ports.in;

import com.example.demo.domain.User;

/**
 * Input port for user registration/sign-up
 */
public interface RegisterUserUseCase {
    User registerUser(String email, String username, String password, String firstName, String lastName);
}
