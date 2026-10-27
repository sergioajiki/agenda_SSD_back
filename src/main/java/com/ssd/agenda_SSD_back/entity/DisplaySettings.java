package com.ssd.agenda_SSD_back.entity;

import com.ssd.agenda_SSD_back.enums.DisplayBannerEffect;
import com.ssd.agenda_SSD_back.enums.DisplayBannerPosition;
import com.ssd.agenda_SSD_back.enums.DisplayBannerSize;
import com.ssd.agenda_SSD_back.enums.DisplayCarouselMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    // Banner (imagem em DisplayBanner). Colunas criadas pelo ddl-auto=update
    // depois que a linha já existia — numa base antiga chegam null, e quem
    // trata isso como valor padrão é o DisplaySettingsService.
    private Boolean bannerEnabled;

    private Integer bannerDurationSeconds;

    private Integer bannerIntervalMinutes;

    // Texto comum (VARCHAR), e não o ENUM nativo que o Hibernate cria no H2
    // por padrão: o ddl-auto=update não acrescenta valores a um ENUM já
    // criado, então uma opção nova (ex.: NINTH) quebraria a gravação numa
    // base existente. Com o mapeamento em VARCHAR, o próprio ddl-auto=update
    // converte uma coluna que já era ENUM (alter column ... varchar), mantendo os valores.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private DisplayBannerSize bannerSize;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private DisplayBannerPosition bannerPosition;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private DisplayBannerEffect bannerEffect;

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
}
