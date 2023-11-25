package com.crm.backend.user;

import com.crm.backend.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


/**
 * Represents an App User. Basically, app users are distinguished by app roles.
 * There are 3 roles: CUSTOMER, AGENT and ADMIN. Users with AGENT role have
 * 2 additional fields: score & status.
 *
 * @author shohrukhyakhyoev
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "app-user")
public class User implements UserDetails {
    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    private Integer age;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String password;

    /**
     * Returns list of authorities attached to a particular app user so that users access only
     * the features they are allowed to by the application. For example, this method is responsible
     * for customers not having access to records of others customers, which is allowed only for the
     * owner of records.
     *
     * @return  list of authorities it allows app users to access certain features in the application
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(role.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

