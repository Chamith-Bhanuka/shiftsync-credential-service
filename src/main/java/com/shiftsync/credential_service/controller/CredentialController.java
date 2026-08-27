package com.shiftsync.credential_service.controller;

import com.shiftsync.credential_service.dto.CredentialDocumentDto;
import com.shiftsync.credential_service.dto.DocumentReviewRequest;
import com.shiftsync.credential_service.service.GcsCredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/credentials")
public class CredentialController {

    private final GcsCredentialService credentialService;

    public CredentialController(GcsCredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping("/upload")
    public ResponseEntity<CredentialDocumentDto> uploadCredential(
            @RequestParam("employeeId") String employeeId,
            @RequestParam(value = "employeeName", required = false) String employeeName,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam("file") MultipartFile file) throws IOException {

        CredentialDocumentDto dto = credentialService.uploadCredential(employeeId, employeeName, documentType, note, file);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<CredentialDocumentDto>> getCredentialsForEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(credentialService.getCredentialsForEmployee(employeeId));
    }

    @GetMapping("/pending-review")
    public ResponseEntity<List<CredentialDocumentDto>> getPendingReviewDocuments() {
        return ResponseEntity.ok(credentialService.getPendingReviewDocuments());
    }

    @GetMapping("/view")
    public ResponseEntity<byte[]> viewDocument(@RequestParam("objectPath") String objectPath) {
        return credentialService.downloadDocument(objectPath);
    }

    @PutMapping("/review")
    public ResponseEntity<CredentialDocumentDto> reviewDocument(@RequestBody DocumentReviewRequest request) {
        CredentialDocumentDto updated = credentialService.reviewDocument(
                request.getObjectPath(),
                request.getDecision(),
                request.getReviewerId(),
                request.getComment()
        );
        return ResponseEntity.ok(updated);
    }
}
