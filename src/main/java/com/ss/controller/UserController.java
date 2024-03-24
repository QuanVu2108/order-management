package com.ss.controller;

import com.ss.dto.request.SignInRequest;
import com.ss.dto.request.UserRequest;
import com.ss.dto.response.ServiceResponse;
import com.ss.dto.response.TokenResponse;
import com.ss.dto.response.UserResponse;
import com.ss.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signin")
    public ServiceResponse<TokenResponse> login(@Valid @RequestBody SignInRequest request) {
        String userName = request.getUserName();
        String password = request.getPassword();
        String token = userService.signin(userName, password);
        TokenResponse tokenDTO = new TokenResponse();
        tokenDTO.setToken(token);
        return ServiceResponse.succeed(HttpStatus.OK, tokenDTO);
    }

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

}
