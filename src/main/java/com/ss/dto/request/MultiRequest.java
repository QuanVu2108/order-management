package com.ss.dto.request;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class MultiRequest {

    private Set<Long> productIds;
}
