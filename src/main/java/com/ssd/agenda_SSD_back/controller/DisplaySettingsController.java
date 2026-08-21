package com.ssd.agenda_SSD_back.controller;

import com.ssd.agenda_SSD_back.dto.DisplaySettingsDto;
import com.ssd.agenda_SSD_back.service.DisplaySettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Configuração da tela pública de recepção (/display). A leitura é pública —
// a própria tela busca isso sem estar logada, mesmo padrão de GET /api/meeting
// — só a troca exige ADMIN.
@RestController
@Tag(name = "Display", description = "Configuração da tela de exibição pública (recepção)")
@RequestMapping("/api/display-settings")
public class DisplaySettingsController {
    private final DisplaySettingsService displaySettingsService;

    public DisplaySettingsController(DisplaySettingsService displaySettingsService) {
        this.displaySettingsService = displaySettingsService;
    }

    @GetMapping
    @Operation(summary = "Ler a configuração da tela de recepção", description = "Modo e velocidade do carrossel de próximos dias (público)")
    public DisplaySettingsDto getSettings() {
        return displaySettingsService.getSettings();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar a configuração da tela de recepção", description = "Altera modo e velocidade do carrossel (somente ADMIN)")
    public DisplaySettingsDto updateSettings(@RequestBody DisplaySettingsDto dto) {
        return displaySettingsService.updateSettings(dto);
    }
}
