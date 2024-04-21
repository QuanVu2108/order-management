package com.ss.security;

import com.ss.exception.ExceptionResponse;
import com.ss.exception.http.InvalidInputError;
import com.ss.exception.http.NotFoundError;
import com.ss.model.UserModel;
import com.ss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetails implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final UserModel user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ExceptionResponse(NotFoundError.USER_NOT_FOUND.getMessage(),  NotFoundError.USER_NOT_FOUND);
        }

        return org.springframework.security.core.userdetails.User//
                .withUsername(username)//
                .password(user.getPassword())//
                .authorities(user.getRoles())//
                .accountExpired(false)//
                .accountLocked(false)//
                .credentialsExpired(false)//
                .disabled(false)//
                .build();
    }

}
