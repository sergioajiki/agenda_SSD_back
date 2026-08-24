package com.ssd.agenda_SSD_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class BackupResultDto {
    @Schema(description = "Caminho completo do arquivo de backup gerado")
    private String filePath;

    @Schema(description = "Tamanho do arquivo gerado, em bytes")
    private long sizeBytes;

    @Schema(description = "Data/hora em que o backup foi criado")
    private LocalDateTime createdAt;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static BackupResultDto of(Path file) throws java.io.IOException {
        BackupResultDto dto = new BackupResultDto();
        dto.setFilePath(file.toAbsolutePath().toString());
        dto.setSizeBytes(java.nio.file.Files.size(file));
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
}
