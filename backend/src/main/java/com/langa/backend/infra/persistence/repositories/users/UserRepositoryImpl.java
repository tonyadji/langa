package com.langa.backend.infra.persistence.repositories.users;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.infra.persistence.repositories.users.mongo.MongoUserDao;
import com.langa.backend.infra.persistence.repositories.users.mongo.UserDocument;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final MongoUserDao mongoUserDao;

    public UserRepositoryImpl(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    @Override
    public Optional<User> save(User user) {
        final UserDocument userDocument = UserDocument.of(user);
        return Optional.of(mongoUserDao.save(userDocument).toUser());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return mongoUserDao.findByEmail(email)
                .map(UserDocument::toUser);
    }

    @Override
    public Optional<User> findByFistConnectionToken(String token) {
        return mongoUserDao.findByFirstConnectionToken(token)
                .map(UserDocument::toUser);
    }
}
