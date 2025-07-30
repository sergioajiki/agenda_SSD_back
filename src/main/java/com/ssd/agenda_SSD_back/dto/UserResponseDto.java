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
        Long matricula
) {
    public UserResponseDto(Long id, String name, String email, Long matricula){
        this.id = id;
        this.name = name;
        this.email = email;
        this.matricula = matricula;
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
    public Long matricula() {
        return matricula;
    }

    public static UserResponseDto userResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getMatricula()
        );
    }
}
