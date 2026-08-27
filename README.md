# ShiftSync — Credential & Compliance Service

**Student Name:** Chamith Bhanuka Widanapathirana  
**Student ID / Number:** 241711051  
**Slack Handle:** Chamith Bhanuka  
**GCP Project ID:** project-a58ee7a4-4913-4af2-a6d  
**Course:** ITS 2130 — Enterprise Cloud Architecture  

---

## Description

Microservice responsible for managing employee certifications, medical clearances, and compliance documents in the ShiftSync platform. Integrates securely with Google Cloud Storage (GCS) for encrypted document storage and streaming, and records compliance verification statuses in PostgreSQL.

---

## Key Features

- **Document Upload & Storage**: Stores employee compliance documents in private Google Cloud Storage buckets (`project-a58ee7a4-4913-4af2-a6d-credentials`).
- **Secure Document Streaming**: Features dedicated streaming proxy endpoint (`/credentials/view?objectPath=...`) utilizing internal Compute Engine IAM service account credentials to stream documents directly to browsers without requiring public bucket access.
- **Manager Compliance Reviews**: Workflow for managers to review, approve, or reject employee documents with reviewer feedback notes.
- **Relational Persistence**: Tracks document metadata, classification, employee ID, and review status in Cloud SQL PostgreSQL.

---

## Technology Stack

- Java 25
- Spring Boot 3.x
- Spring Data JPA / Hibernate
- Cloud SQL PostgreSQL
- Google Cloud Storage (GCS) Client SDK
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config Client
