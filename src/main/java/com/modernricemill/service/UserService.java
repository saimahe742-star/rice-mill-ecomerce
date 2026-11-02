package com.modernricemill.service;

import com.modernricemill.dto.AdminUserUpdateRequest;
import com.modernricemill.dto.ProfileUpdateRequest;
import com.modernricemill.dto.RegisterRequest;
import com.modernricemill.entity.Role;
import com.modernricemill.entity.User;
import com.modernricemill.repository.FeedbackRepository;
import com.modernricemill.repository.OrderRepository;
import com.modernricemill.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            OrderRepository orderRepository,
            FeedbackRepository feedbackRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.feedbackRepository = feedbackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional(readOnly = true)
    public MapProfile getProfile(User principalUser) {
        User u = userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new MapProfile(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole().name());
    }

    @Transactional
    public MapProfile updateProfile(User principalUser, ProfileUpdateRequest req) {
        boolean hasName = StringUtils.hasText(req.getName());
        boolean hasNewPw = StringUtils.hasText(req.getNewPassword());
        if (!hasName && !hasNewPw) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide name and/or new password to update");
        }
        User u = userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (hasName) {
            u.setName(req.getName().trim());
        }
        if (hasNewPw) {
            if (!StringUtils.hasText(req.getCurrentPassword())
                    || !passwordEncoder.matches(req.getCurrentPassword(), u.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
            }
            u.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }
        userRepository.save(u);
        return new MapProfile(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole().name());
    }

    @Transactional
    public void deleteMyAccount(User principalUser) {
        User u = userRepository.findById(principalUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getRole() == Role.ROLE_ADMIN && userRepository.countByRole(Role.ROLE_ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete the last admin account");
        }
        assertNoOrdersOrFeedback(u);
        userRepository.delete(u);
    }

    @Transactional(readOnly = true)
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public MapProfile adminUpdateUser(Long id, AdminUserUpdateRequest req) {
        if (req.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No fields to update");
        }
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (StringUtils.hasText(req.getName())) {
            u.setName(req.getName().trim());
        }
        if (StringUtils.hasText(req.getEmail())) {
            String em = req.getEmail().trim().toLowerCase();
            if (!EMAIL.matcher(em).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid email required");
            }
            if (userRepository.existsByEmailAndIdNot(em, u.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
            }
            u.setEmail(em);
        }
        if (req.getRole() != null) {
            ensureAdminInvariant(u, req.getRole());
            u.setRole(req.getRole());
        }
        if (StringUtils.hasText(req.getNewPassword())) {
            u.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }
        userRepository.save(u);
        return new MapProfile(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole().name());
    }

    @Transactional
    public void adminDeleteUser(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getRole() == Role.ROLE_ADMIN && userRepository.countByRole(Role.ROLE_ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete the last admin user");
        }
        assertNoOrdersOrFeedback(u);
        userRepository.delete(u);
    }

    private void assertNoOrdersOrFeedback(User u) {
        if (orderRepository.countByUser(u) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cannot delete user with existing orders");
        }
        if (feedbackRepository.countByUser(u) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Cannot delete user with feedback records");
        }
    }

    private void ensureAdminInvariant(User target, Role newRole) {
        if (target.getRole() != Role.ROLE_ADMIN || newRole == Role.ROLE_ADMIN) {
            return;
        }
        if (userRepository.countByRole(Role.ROLE_ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot remove the last admin user");
        }
    }

    public record MapProfile(Long id, String name, String email, String phone, String role) {}
}
