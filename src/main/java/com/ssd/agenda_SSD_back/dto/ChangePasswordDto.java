package com.ssd.agenda_SSD_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Troca de senha pelo próprio usuário — exige a senha atual como
 * confirmação, pra alguém com uma sessão aberta (ex.: computador
 * compartilhado) não conseguir sequestrar a conta sem saber a senha real.
 */
public class ChangePasswordDto {
    @Schema(description = "Senha atual, exigida para confirmar a troca")
    @NotBlank(message = "A senha atual é obrigatória")
    private String currentPassword;

    @Schema(description = "Nova senha, entre 6 e 10 caracteres")
    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 6, max = 10, message = "A senha deve ter entre 6 e 10 caracteres.")
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
