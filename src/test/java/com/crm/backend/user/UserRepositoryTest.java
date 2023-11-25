package com.crm.backend.user;

import com.crm.backend.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository underTest;
    private User user;

    @BeforeEach
    void setUp() {
        this.user = User.builder()
                .firstName("Shohrukh")
                .lastName("Yakhyoev")
                .email("s.yakhyoev@gmail.com")
                .age(19)
                .role(Role.CUSTOMER)
                .phoneNumber("+998902302000")
                .password("sasasa90")
                .build();

        underTest.save(this.user);

    }

    @AfterEach
    void takeDown() {
        underTest.deleteAll();
    }

    @Test
    void itShouldFindEmail() {
        // given
        // when
        boolean exists = underTest.findByEmail(user.getEmail()).isPresent();

        // then
        assertTrue(exists);
    }

    @Test
    void itShouldNotFindEmail() {
        // given
        String email = "aziz@gmail.com";

        // when
        boolean doesntExist = underTest.findByEmail(email).isEmpty();

        // then
        assertTrue(doesntExist);
    }

    @Test
    void itShouldFindByRole() {
        // given
        // when
        boolean exists = !(underTest.findByRole(Role.CUSTOMER).isEmpty());

        // then
        assertTrue(exists);
    }

    @Test
    void isShouldNotFindByRole() {
        // given
        // when
        boolean doesntExist = underTest.findByRole(Role.AGENT).isEmpty();

        // then
        assertTrue(doesntExist);
    }

}