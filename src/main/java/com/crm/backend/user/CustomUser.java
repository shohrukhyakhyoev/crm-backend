package com.crm.backend.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.crm.backend.enums.Role;


/**
 * Represents a Custom App User. Used to in serializing
 * and deserializing App User objects.
 *
 * @author shohrukhyakhyoev
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomUser {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    private String phoneNumber;
    private Role role;
    private String password;
}
