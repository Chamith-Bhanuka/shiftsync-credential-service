package com.shiftsync.credential_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialDocumentDto {
    private String objectPath;
    private String originalFilename;
    private String employeeId;
    private String employeeName;
    private String documentType;
    private String note;
    private String reviewStatus;
    private String reviewComment;
    private String reviewerId;
    private String signedUrl;
    private String uploadedAt;
}
