package com.ssd.agenda_SSD_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta do cadastro de usuário — a ÚNICA vez que a senha temporária
 * aparece em texto puro. Depois desse momento ela nunca mais é recuperável
 * (nem pelo admin), só existe o hash no banco.
 */
public record UserCreatedDto(
        @Schema(description = "Número de identificação do usuário")
        Long id,
        @Schema(description = "Nome do usuário cadastrado")
        String name,
        @Schema(description = "Email do usuário cadastrado")
        String email,
        @Schema(description = "Senha temporária gerada — repasse ao usuário, ele deve trocá-la no primeiro login")
        String temporaryPassword
) {
}
