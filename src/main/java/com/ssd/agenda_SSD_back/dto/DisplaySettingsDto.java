package com.ssd.agenda_SSD_back.dto;

import com.ssd.agenda_SSD_back.entity.DisplaySettings;
import com.ssd.agenda_SSD_back.enums.DisplayCarouselMode;
import io.swagger.v3.oas.annotations.media.Schema;

public class DisplaySettingsDto {
    @Schema(description = "Segundos por item da faixa 'Próximos dias' — no modo letreiro, controla o ritmo do deslize; no modo paginado, quanto tempo cada página fica parada.")
    private Double secondsPerCard;

    @Schema(description = "Como a faixa 'Próximos dias' se movimenta: MARQUEE (letreiro contínuo) ou PAGED (páginas de 3, trocando sozinhas)")
    private DisplayCarouselMode mode;

    public Double getSecondsPerCard() {
        return secondsPerCard;
    }

    public void setSecondsPerCard(Double secondsPerCard) {
        this.secondsPerCard = secondsPerCard;
    }

    public DisplayCarouselMode getMode() {
        return mode;
    }

    public void setMode(DisplayCarouselMode mode) {
        this.mode = mode;
    }

    public static DisplaySettingsDto fromEntity(DisplaySettings settings) {
        DisplaySettingsDto dto = new DisplaySettingsDto();
        dto.setSecondsPerCard(settings.getSecondsPerCard());
        dto.setMode(settings.getMode());
        return dto;
    }
}
