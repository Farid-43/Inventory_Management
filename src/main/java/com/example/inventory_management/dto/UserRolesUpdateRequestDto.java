package com.example.inventory_management.dto;

import java.util.Set;

public class UserRolesUpdateRequestDto {

    private Set<String> roles;

    public UserRolesUpdateRequestDto() {
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
