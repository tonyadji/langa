package com.langa.backend.infra.services.users;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public User findOrCreateUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = User.createNew(email, UUID.randomUUID().toString());
                    user.buildFirstConnectionToken();
                    userRepository.save(user);
                    return user;
                });
    }
}
