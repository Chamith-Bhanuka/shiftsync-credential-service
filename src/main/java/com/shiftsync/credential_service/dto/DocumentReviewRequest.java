package com.shiftsync.credential_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReviewRequest {
    private String objectPath;
    private String decision; // APPROVED or REJECTED
    private String reviewerId;
    private String comment;
}
