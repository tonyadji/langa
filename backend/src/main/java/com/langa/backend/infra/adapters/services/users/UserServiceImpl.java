package com.langa.backend.infra.adapters.services.users;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.UserService;
import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OutboxEventService outboxEventService;

    public UserServiceImpl(UserRepository userRepository, OutboxEventService outboxEventService) {
        this.userRepository = userRepository;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    @Override
    public User findOrCreateUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = User.createInvited(email);
                    userRepository.save(user);
                    return user;
                });
    }

    @Override
    public Optional<User> findByExternalIdentity(String provider, String subject) {
        return userRepository.findByExternalIdentity(provider, subject);
    }

    @Override
    public User provisionExternalUser(ExternalIdentity identity) {
        return findByExternalIdentity(identity.provider(), identity.subject())
                .orElseGet(() -> {
                    try {
                        return linkOrCreate(identity);
                    } catch (DuplicateKeyException e) {
                        // Concurrent first requests of the same user: another one already provisioned it.
                        return findByExternalIdentity(identity.provider(), identity.subject()).orElseThrow(() -> e);
                    }
                });
    }

    private User linkOrCreate(ExternalIdentity identity) {
        final User user = userRepository.findByEmailIgnoreCase(identity.email())
                .map(existing -> {
                    log.info("Linking existing user {} to its {} identity", existing.getId(), identity.provider());
                    // Refused by the domain if the user is already linked to another identity
                    existing.linkExternalIdentity(identity);
                    return existing;
                })
                .orElseGet(() -> {
                    final User created = User.createFromExternalIdentity(identity);
                    log.info("Provisioning new user {} from {} identity", created.getId(), identity.provider());
                    return created;
                });

        final User saved = userRepository.save(user)
                .orElseThrow(() -> new UserException("User could not be saved", null, Errors.USER_NOT_FOUND));
        user.getEvents().forEach(outboxEventService::storeOutboxEvent);
        user.clearEvents();
        return saved;
    }
}
