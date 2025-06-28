package com.YathzoBalo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String displayName;
    private Long userId;

    public JwtResponse(String token, String username, String email, String displayName, Long userId) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.userId = userId;
    }
}
