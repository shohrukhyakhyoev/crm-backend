package com.crm.backend.request;

import com.crm.backend.enums.RequestStatus;
import com.crm.backend.agent.Agent;
import com.crm.backend.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table
public class Request {
    @Id
    @SequenceGenerator(
            name = "request_sequence",
            sequenceName = "request_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "request_sequence"
    )
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    private Double score;
    private Boolean isScored;

    private String message;
    private LocalDateTime creationTime;
    private LocalDateTime assignedTime;
    private LocalDateTime confirmationTime;
    private LocalDateTime finishTime;

}
