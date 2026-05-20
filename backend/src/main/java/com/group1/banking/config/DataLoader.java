package com.group1.banking.config;

import com.group1.banking.entity.Customer;
import com.group1.banking.entity.User;
import com.group1.banking.enums.CustomerType;
import com.group1.banking.enums.RoleName;
import com.group1.banking.repository.CustomerRepository;
import com.group1.banking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner seedTestUsers(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ── Admin user ──────────────────────────────────────────────────
            if (userRepository.findByUsername("admin@bank.com").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin@bank.com");
                admin.setPasswordHash(passwordEncoder.encode("Admin1234!"));
                admin.setRoles(List.of(RoleName.ADMIN));
                admin.setActive(true);
                userRepository.save(admin);
                System.out.println("[DataLoader] Admin seeded  →  email: admin@bank.com  /  password: Admin1234!");
            }

            // ── Customer user ───────────────────────────────────────────────
            if (userRepository.findByUsername("customer@bank.com").isEmpty()) {
                // 1. Customer record
                Customer customer = new Customer();
                customer.setName("Test Customer");
                customer.setAddress("123 Main Street");
                customer.setType(CustomerType.PERSON);
                customer.setDateOfBirth(LocalDate.of(1990, 6, 15));
                customer.setKycVerified(true);
                Customer saved = customerRepository.save(customer);

                // 2. Linked user
                User user = new User();
                user.setUsername("customer@bank.com");
                user.setPasswordHash(passwordEncoder.encode("Customer1234!"));
                user.setRoles(List.of(RoleName.CUSTOMER));
                user.setCustomerId(saved.getCustomerId());
                user.setActive(true);
                userRepository.save(user);
                System.out.println("[DataLoader] Customer seeded  →  email: customer@bank.com  /  password: Customer1234!  /  customerId: " + saved.getCustomerId());
            }
        };
    }
}
