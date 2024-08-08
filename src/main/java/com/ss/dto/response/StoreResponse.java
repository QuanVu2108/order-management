package com.ss.dto.response;


import lombok.Data;

import java.util.UUID;

@Data
public class StoreResponse {
    private UUID id;

    private String name;

    private String phoneNumber;

    private String address;

    private String description;
}
