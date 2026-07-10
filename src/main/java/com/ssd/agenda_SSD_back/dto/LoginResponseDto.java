package com.ssd.agenda_SSD_back.dto;
import com.ssd.agenda_SSD_back.entity.User;

public class LoginResponseDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    // Token JWT emitido neste login — o front deve reenviá-lo no header
    // "Authorization: Bearer <token>" em toda chamada autenticada seguinte.
    private String token;

    public LoginResponseDto(Long id, String name, String email, String role, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public static LoginResponseDto fromEntity(User user, String token) {
        return new LoginResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                token
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
