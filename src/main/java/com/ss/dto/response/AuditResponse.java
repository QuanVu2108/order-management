package com.ss.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public abstract class AuditResponse {

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

}
