package com.ss.service;

import com.ss.model.RefreshToken;
import com.ss.model.UserModel;

public interface RefreshTokenService {
    RefreshToken findByToken(String token);

    RefreshToken createRefreshToken(UserModel user);

    void verifyExpiration(RefreshToken token);

    void deleteByUser(UserModel user);
}
