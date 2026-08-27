package com.shiftsync.credential_service.service;

import com.google.cloud.storage.*;
import com.shiftsync.credential_service.dto.CredentialDocumentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class GcsCredentialService {

    @Value("${gcs.bucket-name:shiftsync-credential-service-823}")
    private String bucketName;

    private final Storage storage;

    public GcsCredentialService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public CredentialDocumentDto uploadCredential(String employeeId, String employeeName,
                                                  String documentType, String note,
                                                  MultipartFile file) throws IOException {
        String cleanOriginalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
        String objectName = String.format("credentials/%s/%d_%s_%s",
                employeeId,
                System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 8),
                cleanOriginalName.replaceAll("[^a-zA-Z0-9._-]", "_"));

        Map<String, String> metadata = new HashMap<>();
        metadata.put("employeeId", employeeId != null ? employeeId : "");
        metadata.put("employeeName", employeeName != null ? employeeName : "");
        metadata.put("documentType", documentType != null ? documentType : "OTHER");
        metadata.put("note", note != null ? note : "");
        metadata.put("reviewStatus", "PENDING");
        metadata.put("originalFilename", cleanOriginalName);
        metadata.put("uploadedAt", Instant.now().toString());

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .setMetadata(metadata)
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());

        return mapToDto(blob);
    }

    public List<CredentialDocumentDto> getCredentialsForEmployee(String employeeId) {
        String prefix = "credentials/" + employeeId + "/";
        List<CredentialDocumentDto> list = new ArrayList<>();

        Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();
        for (Blob blob : blobs) {
            list.add(mapToDto(blob));
        }

        list.sort((a, b) -> Objects.toString(b.getUploadedAt(), "").compareTo(Objects.toString(a.getUploadedAt(), "")));
        return list;
    }

    public List<CredentialDocumentDto> getPendingReviewDocuments() {
        List<CredentialDocumentDto> list = new ArrayList<>();
        Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix("credentials/")).iterateAll();

        for (Blob blob : blobs) {
            Map<String, String> meta = blob.getMetadata();
            String status = meta != null ? meta.get("reviewStatus") : "PENDING";
            if (status == null || "PENDING".equalsIgnoreCase(status)) {
                list.add(mapToDto(blob));
            }
        }

        list.sort((a, b) -> Objects.toString(b.getUploadedAt(), "").compareTo(Objects.toString(a.getUploadedAt(), "")));
        return list;
    }

    public CredentialDocumentDto reviewDocument(String objectPath, String decision, String reviewerId, String comment) {
        BlobId blobId = BlobId.of(bucketName, objectPath);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            throw new RuntimeException("Document not found: " + objectPath);
        }

        Map<String, String> currentMeta = blob.getMetadata();
        Map<String, String> updatedMeta = currentMeta != null ? new HashMap<>(currentMeta) : new HashMap<>();
        updatedMeta.put("reviewStatus", decision != null ? decision : "APPROVED");
        updatedMeta.put("reviewerId", reviewerId != null ? reviewerId : "");
        updatedMeta.put("reviewComment", comment != null ? comment : "");
        updatedMeta.put("reviewedAt", Instant.now().toString());

        Blob updatedBlob = blob.toBuilder().setMetadata(updatedMeta).build().update();
        return mapToDto(updatedBlob);
    }

    public org.springframework.http.ResponseEntity<byte[]> downloadDocument(String objectPath) {
        BlobId blobId = BlobId.of(bucketName, objectPath);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }

        byte[] content = blob.getContent();
        String contentType = blob.getContentType() != null ? blob.getContentType() : "application/octet-stream";

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType(contentType));
        headers.setContentDisposition(org.springframework.http.ContentDisposition.inline().filename(blob.getName()).build());
        headers.setContentLength(content.length);
        headers.setCacheControl("max-age=3600");

        return new org.springframework.http.ResponseEntity<>(content, headers, org.springframework.http.HttpStatus.OK);
    }

    private CredentialDocumentDto mapToDto(Blob blob) {
        Map<String, String> meta = blob.getMetadata();
        String viewUrl;
        try {
            viewUrl = "/api/credentials/credentials/view?objectPath=" + java.net.URLEncoder.encode(blob.getName(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            viewUrl = "/api/credentials/credentials/view?objectPath=" + blob.getName();
        }

        return CredentialDocumentDto.builder()
                .objectPath(blob.getName())
                .originalFilename(meta != null ? meta.getOrDefault("originalFilename", blob.getName()) : blob.getName())
                .employeeId(meta != null ? meta.getOrDefault("employeeId", "") : "")
                .employeeName(meta != null ? meta.getOrDefault("employeeName", "") : "")
                .documentType(meta != null ? meta.getOrDefault("documentType", "OTHER") : "OTHER")
                .note(meta != null ? meta.getOrDefault("note", "") : "")
                .reviewStatus(meta != null ? meta.getOrDefault("reviewStatus", "PENDING") : "PENDING")
                .reviewComment(meta != null ? meta.get("reviewComment") : null)
                .reviewerId(meta != null ? meta.get("reviewerId") : null)
                .signedUrl(viewUrl)
                .uploadedAt(meta != null ? meta.getOrDefault("uploadedAt", blob.getCreateTimeOffsetDateTime().toString()) : "")
                .build();
    }
}
