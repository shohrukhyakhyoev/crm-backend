package com.crm.backend.token;

import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Serves as a service layer for requests associated with manipulation over token's data.
 *
 * @author shohrukhyakhyoev
 */
@Service
public class TokenService {

    private final TokenRepository repository;

    public TokenService(TokenRepository repository) {
        this.repository = repository;
    }

    public List<Token> getUserTokens(Long user_id) {
        return repository.findAllValidTokenByUser(user_id);
    }

    public List<Token> getTokens() {
        return repository.findAll();
    }

    public void saveToken(Token token) {
        repository.save(token);
    }

    public List<Token> findAllValidTokenByUser(Long id) {
        return repository.findAllValidTokenByUser(id);
    }

    public void deleteUserTokens(Long id) {
        List<Token> tokens = repository.findAllValidTokenByUser(id);
        repository.deleteAll(tokens);
    }

    public void delete(Token token) {
        repository.delete(token);
    }

}
