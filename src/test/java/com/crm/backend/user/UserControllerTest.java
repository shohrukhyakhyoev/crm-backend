package com.crm.backend.user;

import com.crm.backend.auth.AuthRequest;
import com.crm.backend.auth.AuthResponse;
import com.crm.backend.enums.Role;
import com.crm.backend.exception.ApiException;
import com.crm.backend.others.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;
    private CustomUser user;
    private ResponseEntity<ApiResponse> res;
    @LocalServerPort
    private int port;
    private String url;

    @BeforeEach
    void setUp() {
        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);

        this.url = "http://localhost:" + this.port + "/api/v1/users";

        this.user =
                new CustomUser(1L, "Shohrukh",
                        "Yakhyoev", "s.y@gmail.com",
                        19, "8516",
                        Role.ADMIN, "shoxrux577");


        // registering user via AuthController
        HttpEntity<CustomUser> request = new HttpEntity<>(this.user, this.headers);
        restTemplate.postForEntity("http://localhost:" + this.port  + "/api/v1/auth/register/", request, ApiResponse.class);

        // authenticating user via AuthController to get tokens!
        AuthRequest authRequest = new AuthRequest(this.user.getEmail(), this.user.getPassword());
        HttpEntity<AuthRequest> authReq = new HttpEntity<>(authRequest, this.headers);
        ResponseEntity<AuthResponse> authResponse =
                restTemplate.postForEntity(
                "http://localhost:" + this.port  + "/api/v1/auth/authenticate/",
                authReq,
                AuthResponse.class);

        this.headers.set("Authorization",
                String.format("Bearer %s",
                Objects.requireNonNull(authResponse.getBody()).getAccessToken()));

    }

    @Test
    void itShouldGetUsers() {
        // given
        String role = "ADMIN";

        // when
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<List> res = restTemplate
                .exchange(String.format("%s/all/?role=%s", url, role), HttpMethod.GET, request, List.class);

        // then
        assertThat(res.getBody()).isNotEqualTo(null);
        assertThat(Objects.requireNonNull(res).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(res.getBody()).size()).isEqualTo(1);
    }


    @Test
    void willThrowWhenGetUsers() {
        // given
        String role = "-";

        // when
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<ApiException> response = restTemplate
                .exchange(String.format("%s/all/?role=%s", url, role), HttpMethod.GET, request, ApiException.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void itShouldGetUser() throws JsonProcessingException {
        // given
        String email = "s.y@gmail.com";
        Long id = 1L;

        // when
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<CustomUser> response = restTemplate
                .exchange(String.format("%s/?email=%s&id=%d", url, email, id), HttpMethod.GET, request, CustomUser.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo(email);
    }


    @Test
    void willThrowIfGetUser() {
        // given
        String email = "email";
        Long id = 1L;

        // when
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<ApiException> response = restTemplate
                .exchange(String.format("%s/?email=%s&id=%d", url, email, id), HttpMethod.GET, request, ApiException.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void itShouldRegisterNewUser() {
        // given
        CustomUser user =
                new CustomUser(2L, "Aziz",
                        "Avazov", "aziz@gmail.com",
                        19, "8516",
                        Role.CUSTOMER, "shoxrux577");

        // when
        HttpEntity<CustomUser> request = new HttpEntity<>(user, this.headers);
        this.res = restTemplate
                .postForEntity(url + "/create/", request, ApiResponse.class);

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
                .postForEntity(url + "/create/", request, ApiException.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getBody())
                .isEqualTo("This email already exists!");

    }


    @Test
    void itShouldDeleteUser() {
        // given
        Long id = user.getId();

        // when
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<ApiResponse> response = restTemplate
                .exchange(this.url + "/delete/" + id + "/", HttpMethod.DELETE, request, ApiResponse.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }


    @Test
    void itShouldEditUser() {
        // given
       user.setEmail("yakhyoev@gmail.com");


        // when
        HttpEntity<CustomUser> request = new HttpEntity<>(user, headers);
        ResponseEntity<CustomUser> response = restTemplate
                .exchange(url + "/edit/" + user.getId() +"/", HttpMethod.PUT, request, CustomUser.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(response.getBody().getEmail()).isEqualTo(user.getEmail());
    }


    @Test
    void willThrowWhenEditUser() {
        // given
        // when
        HttpEntity<CustomUser> request = new HttpEntity<>(user, headers);
        ResponseEntity<CustomUser> response = restTemplate
                .exchange(url + "/edit/" + 3 +"/", HttpMethod.PUT, request, CustomUser.class);

        // then
        assertThat(response.getBody()).isNotEqualTo(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }



}