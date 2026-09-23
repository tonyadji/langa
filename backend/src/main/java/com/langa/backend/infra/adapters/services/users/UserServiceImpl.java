package com.langa.backend.infra.adapters.services.users;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public User provisionExternalUser(String externalId, String email) {
        return userRepository.findByExternalId(externalId)
                .orElseGet(() -> {
                    try {
                        return linkOrCreate(externalId, email);
                    } catch (DuplicateKeyException e) {
                        // Concurrent first requests of the same user: another one already provisioned it.
                        return userRepository.findByExternalId(externalId).orElseThrow(() -> e);
                    }
                });
    }

    private User linkOrCreate(String externalId, String email) {
        final User user = userRepository.findByEmailIgnoreCase(email)
                .map(existing -> {
                    if (existing.isLinked()) {
                        log.warn("Email {} is already linked to another external identity", email);
                        throw new UserException("Email already linked to another identity", null, Errors.USER_ILLEGAL_STATUS);
                    }
                    log.info("Linking existing user {} to its external identity", existing.getEmail());
                    existing.linkExternalIdentity(externalId);
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("Provisioning new user {} from external identity", email);
                    return User.createFromExternalIdentity(externalId, email.toLowerCase());
                });

        final User saved = userRepository.save(user)
                .orElseThrow(() -> new UserException("User could not be saved", null, Errors.USER_NOT_FOUND));
        user.getEvents().forEach(outboxEventService::storeOutboxEvent);
        user.clearEvents();
        return saved;
    }
}
