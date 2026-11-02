package com.modernricemill.api;

import com.modernricemill.dto.ProfileUpdateRequest;
import com.modernricemill.security.CustomUserDetails;
import com.modernricemill.security.CustomUserDetailsService;
import com.modernricemill.service.UserService;
import com.modernricemill.service.UserService.MapProfile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;

    public UserApiController(UserService userService, CustomUserDetailsService userDetailsService, SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal CustomUserDetails principal) {
        MapProfile p = userService.getProfile(principal.getUser());
        return toJson(p);
    }

    @PatchMapping("/me")
    public Map<String, Object> updateMe(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ProfileUpdateRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        MapProfile p = userService.updateProfile(principal.getUser(), body);
        UserDetails refreshed = userDetailsService.loadUserByUsername(p.email());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                refreshed, null, refreshed.getAuthorities());
        var existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null) {
            auth.setDetails(existing.getDetails());
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
        return toJson(p);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal CustomUserDetails principal) {
        userService.deleteMyAccount(principal.getUser());
        return ResponseEntity.noContent().build();
    }

    private static Map<String, Object> toJson(MapProfile p) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("id", p.id());
        body.put("name", p.name());
        body.put("email", p.email());
        body.put("phone", p.phone() != null ? p.phone() : "");
        body.put("role", p.role());
        return body;
    }
}
