package com.srg.smartexpenseapi.payload.response;

import java.util.List;

public class AdminUserResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;

    public AdminUserResponse(Long id, String username, String email, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public List<String> getRoles() { return roles; }
}
