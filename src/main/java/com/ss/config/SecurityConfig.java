package com.ss.config;

import com.ss.security.APIKeyAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.application.auth-token-header.name}")
    private String principalRequestHeader;

    @Value("${spring.application.auth-token}")
    private String principalRequestValue;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader);
        filter.setAuthenticationManager(authentication -> {
            String principal = (String) authentication.getPrincipal();
            if (!principalRequestValue.equals(principal))
                throw new BadCredentialsException("The API key was not found or not the expected value.");

            authentication.setAuthenticated(true);
            return authentication;
        });

        http
//            .cors().disable()
            .csrf().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilter(filter)
            .authorizeRequests()
//            .antMatchers("/v2/api-docs", "/swagger-resources/configuration/ui", "/swagger-resources", "/swagger-resources/configuration/security", "/swagger-ui.html", "/webjars/**").permitAll()
//            .anyRequest()
//            .authenticated()
            .antMatchers("/secure").authenticated()
            .anyRequest().permitAll()
            .and().formLogin().disable();
    }
}