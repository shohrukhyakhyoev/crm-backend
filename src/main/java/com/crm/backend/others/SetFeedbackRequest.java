package com.crm.backend.others;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SetFeedbackRequest {
    private Long requestId;
    private String customerEmail;
    private Long customerId;
    Double score;
}
