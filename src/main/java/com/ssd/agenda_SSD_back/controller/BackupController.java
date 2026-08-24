package com.ssd.agenda_SSD_back.controller;

import com.ssd.agenda_SSD_back.dto.BackupRequestDto;
import com.ssd.agenda_SSD_back.dto.BackupResultDto;
import com.ssd.agenda_SSD_back.service.DatabaseBackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
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

    @PostMapping("/download")
    @Operation(
            summary = "Gera um backup fresco e devolve o arquivo direto pra download",
            description = "Diferente de POST /api/backup, este não fica salvo no servidor — o arquivo vai só na resposta, pra quem chamou guardar onde quiser (ex.: o próprio computador do admin)."
    )
    public ResponseEntity<byte[]> downloadBackup() {
        Path tempFile = null;
        try {
            tempFile = backupService.createTempBackup();
            byte[] content = Files.readAllBytes(tempFile);

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + tempFile.getFileName() + "\"")
                    .body(content);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar o backup para download: " + e.getMessage(), e);
        } finally {
            // Lido em memória (arquivo pequeno) — a pasta temporária não
            // precisa continuar existindo depois disso.
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                    Files.deleteIfExists(tempFile.getParent());
                } catch (IOException ignored) {
                    // Não impede a resposta de ir — só sobra um arquivo temporário
                    // órfão, o SO limpa isso sozinho eventualmente.
                }
            }
        }
    }
}
