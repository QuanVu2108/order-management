package com.ss.service.Impl;

import com.ss.exception.CustomException;
import com.ss.model.RefreshToken;
import com.ss.model.UserModel;
import com.ss.repository.RefreshTokenRepository;
import com.ss.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ss.enums.Const.REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken findByToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken.isEmpty())
            throw new CustomException("Refresh token was expired. Please make a new signin request", HttpStatus.BAD_REQUEST);
        return refreshToken.get();
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(UserModel user) {
        RefreshToken refreshToken = new RefreshToken();
        List<RefreshToken> existedRefreshTokens = refreshTokenRepository.findByUser(user);
        if (!existedRefreshTokens.isEmpty()) {
            refreshTokenRepository.deleteByUser(user);
        }
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Override
    @Transactional
    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new CustomException("Refresh token was expired. Please make a new signin request", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public void deleteByUser(UserModel user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
