package com.ss.controller;

import com.ss.dto.pagination.PageCriteria;
import com.ss.dto.pagination.PageResponse;
import com.ss.dto.request.SignInRequest;
import com.ss.dto.request.TokenRefreshRequest;
import com.ss.dto.request.UserRequest;
import com.ss.dto.response.MessageResponse;
import com.ss.dto.response.ServiceResponse;
import com.ss.dto.response.TokenResponse;
import com.ss.dto.response.UserResponse;
import com.ss.model.UserModel;
import com.ss.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ServiceResponse<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, userService.create(request));
    }

    @PutMapping("{id}")
    public ServiceResponse<UserResponse> update(
            @PathVariable @Valid UUID id,
            @Valid @RequestBody UserRequest request) {
        return ServiceResponse.succeed(HttpStatus.OK, userService.update(id, request));
    }

    @GetMapping("/get-me")
    public ServiceResponse<UserModel> getUserInfo() {
        return ServiceResponse.succeed(HttpStatus.OK, userService.getUserInfo());
    }

    @GetMapping
    public PageResponse<UserResponse> search(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "store", required = false) String store,
            @RequestParam(name = "permissionGroup", required = false) String permissionGroup,
            @RequestParam(name = "position", required = false) String position,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "fullName", required = false) String fullName,
            @Valid PageCriteria pageCriteria) {
        return PageResponse.succeed(HttpStatus.OK, userService.search(username, store, permissionGroup, position, email, fullName, pageCriteria));
    }

    @PostMapping("/signin")
    public ServiceResponse<TokenResponse> login(@Valid @RequestBody SignInRequest request) {
        String userName = request.getUserName();
        String password = request.getPassword();
        TokenResponse tokenResponse = userService.signin(userName, password);
        return ServiceResponse.succeed(HttpStatus.OK, tokenResponse);
    }

    @PostMapping("/refresh-token")
    public ServiceResponse<TokenResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        return ServiceResponse.succeed(HttpStatus.OK, userService.refreshToken(requestRefreshToken));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        userService.logout();
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }

}
