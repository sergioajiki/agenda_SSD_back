package com.ssd.agenda_SSD_back.dto;

import com.ssd.agenda_SSD_back.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Dados editáveis de um usuário pela tela de gestão de acessos (admin).
 * Não inclui senha — troca de senha é um fluxo separado (ver roadmap de
 * senha temporária / primeiro acesso).
 */
public class UserUpdateDto {
    @Schema(description = "Nome do usuário")
    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @Schema(description = "Email do usuário")
    @NotBlank(message = "O email é obrigatório")
    private String email;

    @Schema(description = "Matrícula do usuário (opcional)")
    private String matricula;

    @Schema(description = "Tipo de acesso do usuário")
    @NotNull(message = "A role é obrigatória")
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
}
