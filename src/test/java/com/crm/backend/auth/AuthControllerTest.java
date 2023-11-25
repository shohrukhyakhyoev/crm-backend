package com.crm.backend.auth;

import com.crm.backend.enums.Role;
import com.crm.backend.exception.ApiException;
import com.crm.backend.others.ApiResponse;
import com.crm.backend.user.CustomUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;
    private CustomUser user;
    private ResponseEntity<ApiResponse> res;
    @LocalServerPort
    private int port;
    private String url;


    @BeforeEach
    void setUp() throws JsonProcessingException {
        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);

        this.url = "http://localhost:" + this.port + "/api/v1/auth";

        this.user =
                new CustomUser(1L, "Shohrukh",
                        "Yakhyoev", "s.y@gmail.com",
                        19, "8516",
                        Role.ADMIN, "########");

        // when
        HttpEntity<CustomUser> request = new HttpEntity<>(this.user, this.headers);
        this.res = restTemplate
                .postForEntity(url + "/register/", request, ApiResponse.class);
    }


    @Test
    void itShouldRegisterNewUser() {
        // given & when in setUp()
        // then: checking if adding new student in setUp() was successful
        assertThat(res.getBody()).isNotEqualTo(null);
        assertThat(Objects.requireNonNull(res).getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    void willThrowWhenRegisterNewUser() {
        // given
        CustomUser newUser = new CustomUser(2L, "Avaz",
                "Azizov", "s.y@gmail.com",
                19, "8516",
                Role.AGENT, "########");

        // when
        HttpEntity<CustomUser> request = new HttpEntity<>(newUser, headers);
        ResponseEntity<ApiException> response = restTemplate
                .postForEntity(url + "/register/", request, ApiException.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getBody())
                .isEqualTo("This email already exists!");
    }


    @Test
    void itShouldAuthenticateUser() {
        // given
        AuthRequest data = new AuthRequest("s.y@gmail.com", "########");

        // when
        HttpEntity<AuthRequest> request = new HttpEntity<>(data, headers);
        ResponseEntity<AuthResponse> response = restTemplate
                .postForEntity(url + "/authenticate/", request, AuthResponse.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(Objects.requireNonNull(response).getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    void willThrowWhenAuthenticate() {
        // given
        AuthRequest data = new AuthRequest("aziz@gmail.com", "########");

        // when
        HttpEntity<AuthRequest> request = new HttpEntity<>(data, headers);
        ResponseEntity<ApiException> response = restTemplate
                .postForEntity(url + "/authenticate/", request, ApiException.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(Objects.requireNonNull(response.getBody()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getBody())
                .isEqualTo("User is not found");

    }


}