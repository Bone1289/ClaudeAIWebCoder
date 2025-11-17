package com.example.demo.application.ports.in;

import com.example.demo.domain.User;

/**
 * Input port for user login/authentication
 */
public interface LoginUseCase {
    User login(String emailOrUsername, String password);
}
