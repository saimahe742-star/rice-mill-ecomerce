package com.modernricemill.api;

import com.modernricemill.dto.CustomerLoginRequest;
import com.modernricemill.entity.User;
import com.modernricemill.security.CustomUserDetails;
import com.modernricemill.service.CustomerAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final CustomerAuthService customerAuthService;
    private final SecurityContextRepository securityContextRepository;

    public AuthApiController(
            CustomerAuthService customerAuthService,
            SecurityContextRepository securityContextRepository) {
        this.customerAuthService = customerAuthService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        User u = principal.getUser();
        String phone = u.getPhone() != null ? u.getPhone() : "";
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("authenticated", true);
        body.put("email", u.getEmail());
        body.put("name", u.getName());
        body.put("role", u.getRole().name());
        body.put("phone", phone);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/customer/login")
    public ResponseEntity<Map<String, Object>> customerLogin(
            @Valid @RequestBody CustomerLoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response) {
        User user = customerAuthService.findOrCreateCustomer(req.getPhone(), req.getName());
        CustomUserDetails details = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(details, null,
                details.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        String phone = user.getPhone() != null ? user.getPhone() : "";
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("authenticated", true);
        body.put("phone", phone);
        body.put("name", user.getName());
        body.put("message", "Signed in");
        return ResponseEntity.ok(body);
    }
}
