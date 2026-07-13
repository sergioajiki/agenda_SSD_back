package com.ssd.agenda_SSD_back.dto;

import com.ssd.agenda_SSD_back.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDto(
        @Schema(description = "Número de identificação do usuário")
        Long id,
        @Schema(description = "Nome do usuário cadastrado")
        String name,
        @Schema(description = "Email do usuário cadastrado")
        String email,
        @Schema(description = "Matrícula do usuário cadastrado")
        String matricula,
        @Schema(description = "Role do usuário cadastrado (USER ou ADMIN)")
        String role,
        @Schema(description = "Se o usuário está ativo (pode logar) ou foi desativado pelo admin")
        boolean enabled
) {
    public UserResponseDto(Long id, String name, String email, String matricula, String role, boolean enabled){
        this.id = id;
        this.name = name;
        this.email = email;
        this.matricula = matricula;
        this.role = role;
        this.enabled = enabled;
    }
    @Override
    public Long id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String email() {
        return email;
    }

    @Override
    public String matricula() {
        return matricula;
    }

    @Override
    public String role() {
        return role;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    public static UserResponseDto userResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMatricula(),
                user.getRole().name(),
                user.isEnabled()
        );
    }
}
