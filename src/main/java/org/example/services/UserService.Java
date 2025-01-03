package org.example.services;

import org.example.repositories.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public boolean registerUser(String username, String password) {
        return userRepository.registerUser(username, password);
    }

    public boolean authenticateUser(String username, String password) {
        return userRepository.authenticateUser(username, password);
    }
}