package com.crm.backend.token;

import com.crm.backend.enums.Role;
import com.crm.backend.user.User;
import com.crm.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock private TokenRepository tokenRepository;
    @Mock  private UserRepository userRepository;
    private TokenService underTest;

    @BeforeEach
    void setUp() {
        underTest = new TokenService(tokenRepository);
    }

    @Test
    void itShouldFindAllValidTokensOfUser() {
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

        tokenRepository.save(token);

        Long user_id = user.getId();

        // when
        underTest.findAllValidTokenByUser(user_id);

        // then
        verify(tokenRepository).findAllValidTokenByUser(user_id);

    }


    @Test
    void itShouldNotFindAllValidTokensOfUser() {
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

        tokenRepository.save(token);

        Long user_id = 3L;

        // when
        underTest.findAllValidTokenByUser(user_id);

        // then
        verify(tokenRepository).findAllValidTokenByUser(user_id);

    }

}