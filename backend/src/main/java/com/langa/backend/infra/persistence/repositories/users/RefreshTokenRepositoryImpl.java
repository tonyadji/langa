package com.langa.backend.infra.persistence.repositories.users;

import com.langa.backend.domain.users.RefreshToken;
import com.langa.backend.domain.users.repositories.RefreshTokenRepository;
import com.langa.backend.infra.persistence.repositories.users.mongo.MongoRefreshTokenDao;
import com.langa.backend.infra.persistence.repositories.users.mongo.RefreshTokenDocument;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final MongoRefreshTokenDao mongoRefreshTokenDao;

    public RefreshTokenRepositoryImpl(MongoRefreshTokenDao mongoRefreshTokenDao) {
        this.mongoRefreshTokenDao = mongoRefreshTokenDao;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        final RefreshTokenDocument refreshTokenDocument = RefreshTokenDocument.of(token);
        RefreshTokenDocument savedToken = mongoRefreshTokenDao.save(refreshTokenDocument);
        return savedToken.toRefreshToken();
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return mongoRefreshTokenDao.findByToken(token)
                .map(RefreshTokenDocument::toRefreshToken)
                .or(() -> Optional.empty());
    }

    @Override
    public void revokeByToken(String token) {
        mongoRefreshTokenDao.findByToken(token).ifPresent(refreshTokenDocument-> {
            refreshTokenDocument.setRevoked(true);
            mongoRefreshTokenDao.save(refreshTokenDocument);
        });
    }

    @Override
    public void revokeAllByUserEmail(String userEmail) {
        mongoRefreshTokenDao.deleteByUserEmail(userEmail);
    }
}
