package org.example.userauthservice_july2026.services;

import org.antlr.v4.runtime.misc.Pair;
import org.example.userauthservice_july2026.models.User;

public interface IAuthService {

    User signup(String email, String password, String name, String phoneNumber);

    Pair<User,String> login(String email, String password);

    Boolean validateToken(String token);
}
