package com.langa.backend.infra.adapters.persistence.users;

import com.langa.backend.domain.users.repositories.TokenRepository;
import com.langa.backend.domain.users.valueobjects.Token;
import com.langa.backend.infra.adapters.persistence.users.mongo.MongoTokenDao;
import com.langa.backend.infra.adapters.persistence.users.mongo.TokenDocument;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TokenRepositoryImpl implements TokenRepository {

    private final MongoTokenDao mongoTokenDao;

    public TokenRepositoryImpl(MongoTokenDao mongoTokenDao) {
        this.mongoTokenDao = mongoTokenDao;
    }

    @Override
    public void revokeByToken(String token) {
        mongoTokenDao.findByToken(token).ifPresent(tokenDocument -> {
            tokenDocument.setRevoked(true);
            mongoTokenDao.save(tokenDocument);
        });
    }

    @Override
    public void revokeAllByUserEmail(String userEmail) {
        mongoTokenDao.findByUserEmail(userEmail).forEach(tokenDocument -> {
            tokenDocument.setRevoked(true);
            mongoTokenDao.save(tokenDocument);
        });
    }

    @Override
    public Token save(Token token) {
        final TokenDocument tokenDocument = TokenDocument.of(token);
        TokenDocument savedToken = mongoTokenDao.save(tokenDocument);
        return savedToken.toToken();
    }

    @Override
    public Optional<Token> findByToken(String token) {
        return mongoTokenDao.findByToken(token)
                .map(TokenDocument::toToken)
                .or(Optional::empty);
    }

    @Override
    public boolean isRevoked(String token) {
        return mongoTokenDao.findByToken(token)
                .map(TokenDocument::toToken)
                .map(Token::isRevoked)
                .orElse(false);
    }
}
