package com.crm.backend.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


/**
 * Represents response sent by authenticate method in AuthController.
 *
 * @author shohrukhyakhyoev
 * */
@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private Long id;
    private String email;
    private String role;
}