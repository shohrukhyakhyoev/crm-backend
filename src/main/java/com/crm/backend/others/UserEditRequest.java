package com.crm.backend.others;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEditRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    private String phoneNumber;

}
