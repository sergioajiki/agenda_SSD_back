package com.ssd.agenda_SSD_back.dto;

import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// Sem campo de senha: o admin não digita mais senha de ninguém — o
// UserService gera uma temporária e devolve na resposta (ver UserCreatedDto),
// pro admin repassar ao usuário, que troca no primeiro login.
public class UserRequestDto {
    @Schema(description = "Nome do usuário")
    @NotBlank(message = "O nome é obrigatório")
    private String name;
    @Schema(description = "Email do usuário")
    @NotBlank(message = "O email é obrigatório")
    private String email;
    @Schema(description = "Matrícula do usuário")
    private String matricula;
    @Schema(description = "Tipo acesso do usuário")
    private UserRole role;

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

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public static User toEntity(UserRequestDto userRequestDto) {
        User user = new User();
        user.setName(userRequestDto.getName());
        user.setEmail(userRequestDto.getEmail());
        user.setMatricula(userRequestDto.getMatricula());
        user.setRole(userRequestDto.getRole());
        return user;
    }
}
