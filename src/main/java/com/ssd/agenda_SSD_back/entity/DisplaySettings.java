package com.ssd.agenda_SSD_back.entity;

import com.ssd.agenda_SSD_back.enums.DisplayCarouselMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Configuração da tela pública de recepção (/display) — como a faixa
 * "Próximos dias" se move e a que velocidade. Linha única na tabela (id
 * sempre 1L, ver DisplaySettingsService); não existe tela de cadastro de
 * "configurações" no plural, só esse ajuste.
 */
@Entity
@Table(name = "display_settings")
public class DisplaySettings {
    @Id
    private Long id;

    private Double secondsPerCard;

    @Enumerated(EnumType.STRING)
    private DisplayCarouselMode mode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
