package com.modernricemill.dto;

import jakarta.validation.constraints.NotBlank;

public class CustomerLoginRequest {

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String name;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
