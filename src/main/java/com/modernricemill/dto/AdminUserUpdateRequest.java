package com.modernricemill.dto;

import com.modernricemill.entity.Role;
import jakarta.validation.constraints.Size;

public class AdminUserUpdateRequest {

    @Size(max = 120)
    private String name;

    @Size(max = 180)
    private String email;

    private Role role;

    @Size(min = 6, max = 100, message = "Password must be 6–100 characters")
    private String newPassword;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public boolean isEmpty() {
        return (name == null || name.isBlank())
                && (email == null || email.isBlank())
                && role == null
                && (newPassword == null || newPassword.isBlank());
    }
}
