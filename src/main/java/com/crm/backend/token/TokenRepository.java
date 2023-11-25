package com.crm.backend.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Serves as a data layer for manipulation over token data.
 * It allows application to contact with database with the help of
 * functions together with the specified SQL Query. To accomplish it
 * interface extends JpaRepository class. By this we use Spring Data
 * JPA Framework to map objects in database table. It is called ORM.
 *
 * @author shohrukhyakhyoev
 */
@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query(value = """
    select t from Token t inner join User u on t.user.id = u.id where u.id = :id and (t.expired = false or t.revoked = false)"""
    )
    List<Token> findAllValidTokenByUser(Long id);
    Optional<Token> findByToken(String jwt);
}
