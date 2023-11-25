package com.crm.backend.token;

import com.crm.backend.enums.Role;
import com.crm.backend.user.User;
import com.crm.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TokenRepositoryTest {

    @Autowired private TokenRepository underTest;
    @Autowired private UserRepository userRepository;

    @Test
    void itShouldFindAllValidTokenByUser() {
        // given
        User user = User.builder()
                .firstName("Shohrukh")
                .lastName("Yakhyoev")
                .email("s.yakhyoev@gmail.com")
                .age(19)
                .role(Role.CUSTOMER)
                .phoneNumber("+998902302000")
                .password("sasasa90")
                .build();

        userRepository.save(user);

        Token token = Token.builder()
                .token("fadsjhflbq324r1qlefk")
                .expired(false)
                .revoked(false)
                .user(user)
                .build();

        underTest.save(token);

        Long user_id = user.getId();

        // when
        int size = underTest.findAllValidTokenByUser(user_id).size();

        // then
        assertTrue(size > 0);
    }

    @Test
    void itShouldNotFindAllValidTokenByUser() {
        // given
        User user = User.builder()
                .firstName("Shohrukh")
                .lastName("Yakhyoev")
                .email("s.yakhyoev@gmail.com")
                .age(19)
                .role(Role.CUSTOMER)
                .phoneNumber("+998902302000")
                .password("sasasa90")
                .build();

        userRepository.save(user);

        Token token = Token.builder()
                .token("fadsjhflbq324r1qlefk")
                .expired(true)
                .revoked(true)
                .user(user)
                .build();

        underTest.save(token);

        Long user_id = user.getId();

        // when
        int size = underTest.findAllValidTokenByUser(user_id).size();

        // then
        assertEquals(size, 0);
    }

}