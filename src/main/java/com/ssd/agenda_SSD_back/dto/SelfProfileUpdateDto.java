package com.ssd.agenda_SSD_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Dados que o próprio usuário pode alterar em si mesmo (nome/email).
 * Diferente do UserUpdateDto (usado pelo admin em terceiros): não mexe em
 * role nem matrícula, e exige a senha atual como confirmação — quem chama
 * esse endpoint é sempre o dono da conta, resolvido pelo token, não por ID.
 */
public class SelfProfileUpdateDto {
    @Schema(description = "Nome do usuário")
    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @Schema(description = "Novo email do usuário")
    @NotBlank(message = "O email é obrigatório")
    private String email;

    @Schema(description = "Senha atual, exigida para confirmar a alteração")
    @NotBlank(message = "A senha atual é obrigatória")
    private String currentPassword;

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

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}
