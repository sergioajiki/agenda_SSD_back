package com.ssd.agenda_SSD_back.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class BackupRequestDto {
    @Schema(description = "Pasta de destino do backup avulso — se vazio/omitido, usa a mesma pasta do backup agendado (backup.dir)")
    private String directory;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}
