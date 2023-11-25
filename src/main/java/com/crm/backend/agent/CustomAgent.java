package com.crm.backend.agent;

import com.crm.backend.enums.AgentStatus;
import com.crm.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class CustomAgent {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    private String phoneNumber;
    private Role role;
    private String password;
    private AgentStatus status;
    private Double score;
}
