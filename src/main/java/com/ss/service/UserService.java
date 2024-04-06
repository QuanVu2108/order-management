package com.ss.service;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.UserRequest;
import com.ss.dto.response.TokenResponse;
import com.ss.dto.response.UserResponse;
import com.ss.model.UserModel;
import com.ss.repository.query.UserQuery;

import java.util.List;
import java.util.UUID;

public interface UserService {

    TokenResponse signin(String username, String password);

    UserResponse create(UserRequest request);

    UserResponse update(UUID id, UserRequest request);

    PageResponse<UserResponse> search(String username, String store, String permissionGroup, String position, String email, String fullName, PageCriteria pageCriteria);

    TokenResponse refreshToken(String requestRefreshToken);

    void logout();

    UserModel getUserInfo();

    List<UserModel> searchList(UserQuery query);

    List<UserModel> findUsersByKeyword(String keyword);
}
