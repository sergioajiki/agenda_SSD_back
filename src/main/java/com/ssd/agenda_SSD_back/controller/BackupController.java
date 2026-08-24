package com.ssd.agenda_SSD_back.controller;

import com.ssd.agenda_SSD_back.dto.BackupRequestDto;
import com.ssd.agenda_SSD_back.dto.BackupResultDto;
import com.ssd.agenda_SSD_back.service.DatabaseBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

// Backup é ferramenta de admin — a classe inteira exige a role ADMIN, mesmo
// padrão do LogUpdateController.
@RestController
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Backup", description = "Backup do banco de dados (somente ADMIN)")
@RequestMapping("/api/backup")
public class BackupController {
    private final DatabaseBackupService backupService;

    public BackupController(DatabaseBackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    @Operation(
            summary = "Cria um backup avulso do banco agora",
            description = "Além do backup agendado (semanal, ver backup.cron), permite gerar um backup fora do prazo — opcionalmente numa pasta escolhida. Não afeta a limpeza dos backups agendados."
    )
    public BackupResultDto createManualBackup(@RequestBody(required = false) BackupRequestDto request) {
        String directory = (request != null) ? request.getDirectory() : null;
        try {
            Path created = backupService.manualBackup(directory);
            return BackupResultDto.of(created);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao criar o backup: " + e.getMessage(), e);
        }
    }
}
