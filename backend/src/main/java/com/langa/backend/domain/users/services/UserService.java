package com.langa.backend.domain.users.services;

import com.langa.backend.domain.users.User;

public interface UserService {
    User findOrCreateUserByEmail(String email);
}
