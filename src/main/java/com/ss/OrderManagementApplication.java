package com.ss;

import com.ss.enums.Const;
import com.ss.model.RoleModel;
import com.ss.model.UserModel;
import com.ss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.UUID;

@EnableConfigurationProperties
@SpringBootApplication
@ComponentScan("com.ss.*")
@EnableAsync
@EnableScheduling
public class OrderManagementApplication implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(OrderManagementApplication.class, args);
    }

    @Override
    public void run(String... params) throws Exception {
        if (userRepository.findByUsername("admin") == null) {
            UserModel admin = UserModel.builder()
                    .id(UUID.randomUUID())
                    .username(Const.ADMIN)
                    .fullName(Const.ADMIN)
                    .password(passwordEncoder.encode(Const.DEFAULT_PASSWORD))
                    .roles(Arrays.asList(RoleModel.ROLE_ADMIN))
                    .build();
            userRepository.save(admin);
        }
    }

}
