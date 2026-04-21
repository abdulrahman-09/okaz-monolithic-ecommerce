package com.am9.okazx.config;

import com.am9.okazx.model.entity.Address;
import com.am9.okazx.model.entity.User;
import com.am9.okazx.model.enums.UserRole;
import com.am9.okazx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner seedAdmin() {
        return args -> {
            if (userRepository.findByEmail("admin@okazx.com").isEmpty()) {
                Address address = new Address();
                address.setStreet("HQ");
                address.setCity("Cairo");
                address.setGovernorate("Cairo");
                address.setCountry("Egypt");
                address.setZipCode("11511");

                User admin = User.builder()
                        .firstName("Super")
                        .lastName("Admin")
                        .email("admin@okazx.com")
                        .password(passwordEncoder.encode("Admin@1234"))
                        .userRole(UserRole.ADMIN)
                        .address(address)
                        .build();
                userRepository.save(admin);
            }
        };
    }
}
