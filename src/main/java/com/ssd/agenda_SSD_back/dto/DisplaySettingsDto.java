package com.ssd.agenda_SSD_back.dto;

import com.ssd.agenda_SSD_back.entity.DisplaySettings;
import com.ssd.agenda_SSD_back.enums.DisplayBannerEffect;
import com.ssd.agenda_SSD_back.enums.DisplayBannerPosition;
import com.ssd.agenda_SSD_back.enums.DisplayBannerSize;
import com.ssd.agenda_SSD_back.enums.DisplayCarouselMode;
import io.swagger.v3.oas.annotations.media.Schema;

public class DisplaySettingsDto {
    @Schema(description = "Segundos por item da faixa 'Próximos dias' — no modo letreiro, controla o ritmo do deslize; no modo paginado, quanto tempo cada página fica parada.")
    private Double secondsPerCard;

    @Schema(description = "Como a faixa 'Próximos dias' se movimenta: MARQUEE (letreiro contínuo) ou PAGED (páginas de 3, trocando sozinhas)")
    private DisplayCarouselMode mode;

    @Schema(description = "Se o banner aparece na tela de recepção. Só pode ser true com uma imagem enviada. No PUT, null mantém o valor atual.")
    private Boolean bannerEnabled;

    @Schema(description = "Quantos segundos o banner fica na tela a cada exibição (5 a 60). No PUT, null mantém o valor atual.")
    private Integer bannerDurationSeconds;

    @Schema(description = "Minutos de agenda entre o fim de uma exibição do banner e o início da próxima (1 a 30). No PUT, null mantém o valor atual.")
    private Integer bannerIntervalMinutes;

    @Schema(description = "Quanto da tela o banner cobre: FULL (tela inteira, no lugar da agenda), QUARTER (1/4 da tela, num canto, por cima da agenda) ou NINTH (1/9 da tela, idem). No PUT, null mantém o valor atual.")
    private DisplayBannerSize bannerSize;

    @Schema(description = "Canto do banner nos tamanhos QUARTER e NINTH: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT ou BOTTOM_RIGHT. No PUT, null mantém o valor atual.")
    private DisplayBannerPosition bannerPosition;

    @Schema(description = "Como o banner entra e sai: FADE (transição de opacidade) ou SLIDE_UP (sobe de baixo da tela e desce). No PUT, null mantém o valor atual.")
    private DisplayBannerEffect bannerEffect;

    @Schema(description = "Versão da imagem do banner (momento do upload, epoch ms), ou null se não houver imagem. Somente leitura — ignorado no PUT.", accessMode = Schema.AccessMode.READ_ONLY)
    private Long bannerVersion;

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

    public Boolean getBannerEnabled() {
        return bannerEnabled;
    }

    public void setBannerEnabled(Boolean bannerEnabled) {
        this.bannerEnabled = bannerEnabled;
    }

    public Integer getBannerDurationSeconds() {
        return bannerDurationSeconds;
    }

    public void setBannerDurationSeconds(Integer bannerDurationSeconds) {
        this.bannerDurationSeconds = bannerDurationSeconds;
    }

    public Integer getBannerIntervalMinutes() {
        return bannerIntervalMinutes;
    }

    public void setBannerIntervalMinutes(Integer bannerIntervalMinutes) {
        this.bannerIntervalMinutes = bannerIntervalMinutes;
    }

    public DisplayBannerSize getBannerSize() {
        return bannerSize;
    }

    public void setBannerSize(DisplayBannerSize bannerSize) {
        this.bannerSize = bannerSize;
    }

    public DisplayBannerPosition getBannerPosition() {
        return bannerPosition;
    }

    public void setBannerPosition(DisplayBannerPosition bannerPosition) {
        this.bannerPosition = bannerPosition;
    }

    public DisplayBannerEffect getBannerEffect() {
        return bannerEffect;
    }

    public void setBannerEffect(DisplayBannerEffect bannerEffect) {
        this.bannerEffect = bannerEffect;
    }

    public Long getBannerVersion() {
        return bannerVersion;
    }

    public void setBannerVersion(Long bannerVersion) {
        this.bannerVersion = bannerVersion;
    }

    /**
     * Espera uma entidade já normalizada (sem nulls nos campos de banner —
     * ver DisplaySettingsService.applyDefaults). A versão do banner vem de
     * outra tabela, por isso entra como parâmetro.
     */
    public static DisplaySettingsDto fromEntity(DisplaySettings settings, Long bannerVersion) {
        DisplaySettingsDto dto = new DisplaySettingsDto();
        dto.setSecondsPerCard(settings.getSecondsPerCard());
        dto.setMode(settings.getMode());
        dto.setBannerEnabled(settings.getBannerEnabled());
        dto.setBannerDurationSeconds(settings.getBannerDurationSeconds());
        dto.setBannerIntervalMinutes(settings.getBannerIntervalMinutes());
        dto.setBannerSize(settings.getBannerSize());
        dto.setBannerPosition(settings.getBannerPosition());
        dto.setBannerEffect(settings.getBannerEffect());
        dto.setBannerVersion(bannerVersion);
        return dto;
    }
}
