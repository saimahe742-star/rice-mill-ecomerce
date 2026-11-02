package com.modernricemill.service;

import com.modernricemill.entity.Role;
import com.modernricemill.entity.User;
import com.modernricemill.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerAuthService {

    public static String syntheticEmailForPhone(String phoneDigits) {
        return "customer." + phoneDigits + "@sridevi.local";
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Returns 10 digits or null if invalid. */
    public String normalizePhone(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() > 10) {
            digits = digits.substring(digits.length() - 10);
        }
        return digits.length() == 10 ? digits : null;
    }

    @Transactional
    public User findOrCreateCustomer(String rawPhone, String optionalName) {
        String phone = normalizePhone(rawPhone);
        if (phone == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid 10-digit mobile number");
        }
        Optional<User> byPhone = userRepository.findByPhone(phone);
        if (byPhone.isPresent()) {
            User user = byPhone.get();
            if (user.getRole() != Role.ROLE_USER) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This number cannot be used here");
            }
            if (StringUtils.hasText(optionalName)) {
                user.setName(optionalName.trim());
                userRepository.save(user);
            }
            return user;
        }
        String email = syntheticEmailForPhone(phone);
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            if (user.getRole() != Role.ROLE_USER) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This number cannot be used here");
            }
            user.setPhone(phone);
            if (StringUtils.hasText(optionalName)) {
                user.setName(optionalName.trim());
            }
            return userRepository.save(user);
        }
        User u = new User();
        u.setPhone(phone);
        u.setEmail(email);
        u.setName(StringUtils.hasText(optionalName) ? optionalName.trim() : "Customer");
        u.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        u.setRole(Role.ROLE_USER);
        return userRepository.save(u);
    }
}
