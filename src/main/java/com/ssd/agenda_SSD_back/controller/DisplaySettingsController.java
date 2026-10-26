package com.ssd.agenda_SSD_back.controller;

import com.ssd.agenda_SSD_back.dto.DisplaySettingsDto;
import com.ssd.agenda_SSD_back.entity.DisplayBanner;
import com.ssd.agenda_SSD_back.service.DisplayBannerService;
import com.ssd.agenda_SSD_back.service.DisplaySettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

// Configuração da tela pública de recepção (/display). A leitura é pública —
// a própria tela busca isso sem estar logada, mesmo padrão de GET /api/meeting
// — só a troca exige ADMIN.
@RestController
@Tag(name = "Display", description = "Configuração da tela de exibição pública (recepção)")
@RequestMapping("/api/display-settings")
public class DisplaySettingsController {
    private final DisplaySettingsService displaySettingsService;
    private final DisplayBannerService displayBannerService;

    public DisplaySettingsController(DisplaySettingsService displaySettingsService,
                                     DisplayBannerService displayBannerService) {
        this.displaySettingsService = displaySettingsService;
        this.displayBannerService = displayBannerService;
    }

    @GetMapping
    @Operation(summary = "Ler a configuração da tela de recepção", description = "Modo e velocidade do carrossel de próximos dias e configuração do banner (público)")
    public DisplaySettingsDto getSettings() {
        return displaySettingsService.getSettings();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar a configuração da tela de recepção", description = "Altera modo e velocidade do carrossel e a exibição do banner (somente ADMIN)")
    public DisplaySettingsDto updateSettings(@RequestBody DisplaySettingsDto dto) {
        return displaySettingsService.updateSettings(dto);
    }

    @PostMapping(value = "/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enviar a imagem do banner", description = "PNG ou JPG de até 1 MB, no campo 'file'. Substitui a imagem anterior (somente ADMIN)")
    public DisplaySettingsDto uploadBanner(@RequestParam("file") MultipartFile file) {
        return displayBannerService.upload(file);
    }

    @GetMapping("/banner")
    @Operation(summary = "Baixar a imagem do banner", description = "Público — usado pela tela de recepção. 404 se nenhuma imagem foi enviada")
    public ResponseEntity<byte[]> getBanner(@RequestParam(value = "v", required = false) Long version) {
        Optional<DisplayBanner> found = displayBannerService.getBanner();
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DisplayBanner banner = found.get();

        // A TV pede sempre com ?v=<versão atual>: imagem nova = URL nova, então
        // dá pra deixar o navegador guardar pra sempre sem risco de mostrar a
        // antiga. Sem ?v (ou com uma versão velha), não guarda.
        CacheControl cache = banner.getVersion().equals(version)
                ? CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable()
                : CacheControl.noCache();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(banner.getContentType()))
                .cacheControl(cache)
                .body(banner.getContent());
    }

    @DeleteMapping("/banner")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover a imagem do banner", description = "Apaga a imagem e desativa o banner (somente ADMIN)")
    public DisplaySettingsDto deleteBanner() {
        return displayBannerService.delete();
    }
}
