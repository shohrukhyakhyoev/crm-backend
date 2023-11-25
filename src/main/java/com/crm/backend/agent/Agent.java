package com.crm.backend.agent;

import com.crm.backend.enums.AgentStatus;
import com.crm.backend.user.User;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table
public class Agent extends User {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

    @Enumerated(EnumType.STRING)
    private AgentStatus status;
    private Double score;



}
