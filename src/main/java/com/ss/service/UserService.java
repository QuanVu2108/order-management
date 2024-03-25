package com.ss.service;

import com.ss.dto.request.UserRequest;
import com.ss.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    String signin(String username, String password);

    UserResponse create(UserRequest request);

    UserResponse update(UUID id, UserRequest request);

    List<UserResponse> get(String username, String store, String permissionGroup, String position, String email, String fullName);
}
