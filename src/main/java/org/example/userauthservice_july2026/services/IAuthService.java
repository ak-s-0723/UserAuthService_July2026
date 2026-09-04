package org.example.userauthservice_july2026.services;

import org.example.userauthservice_july2026.models.User;

public interface IAuthService {

    User signup(String email, String password, String name, String phoneNumber);

    User login(String email, String password);
}
