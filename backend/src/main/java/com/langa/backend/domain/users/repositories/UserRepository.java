package com.langa.backend.domain.users.repositories;

import com.langa.backend.domain.users.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> save(User user);
    Optional<User> findByEmail(String email);
}
