package com.ss.dto.response;

import lombok.Data;

@Data
public class TokenResponse {
    private String token;
    private String type = "Bearer";
    private String refreshToken;

    public TokenResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.type = "Bearer";
    }
}
