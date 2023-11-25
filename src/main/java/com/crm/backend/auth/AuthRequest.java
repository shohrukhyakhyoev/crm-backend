package com.crm.backend.auth;

import lombok.*;


/**
 * Represents request passed to authenticate method in AuthController.
 * It has 2 attributes: email and password needed to authorize the user
 * in the authenticate method of AuthController.
 *
 * @author shohrukhyakhyoev
 * */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    private String email;
    String password;
}