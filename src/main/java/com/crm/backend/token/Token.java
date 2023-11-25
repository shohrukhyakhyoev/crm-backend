package com.crm.backend.token;

import com.crm.backend.user.User;
import jakarta.persistence.*;
import lombok.*;


/**
 * Represents Token class.
 *
 * @author shohrukhyakhyoev
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {

    @Id
    @SequenceGenerator(
            name = "token_sequence",
            sequenceName = "token_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "token_sequence"
    )
    public Long id;

    @Column(unique = true)
    public String token;

    @Getter
    public boolean revoked;

    @Getter
    public boolean expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

}