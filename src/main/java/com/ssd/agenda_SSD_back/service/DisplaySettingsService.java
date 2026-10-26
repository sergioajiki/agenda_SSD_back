package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.dto.DisplaySettingsDto;
import com.ssd.agenda_SSD_back.entity.DisplaySettings;
import com.ssd.agenda_SSD_back.enums.DisplayBannerPosition;
import com.ssd.agenda_SSD_back.enums.DisplayBannerSize;
import com.ssd.agenda_SSD_back.enums.DisplayCarouselMode;
import com.ssd.agenda_SSD_back.exception.BusinessRuleException;
import com.ssd.agenda_SSD_back.repository.DisplayBannerRepository;
import com.ssd.agenda_SSD_back.repository.DisplaySettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class DisplaySettingsService {
    // Linha única — não existe uma tela de "lista de configurações", só esse
    // ajuste. Evita modelar uma tabela chave/valor genérica pra um caso de uso
    // deste tamanho. DisplayBanner usa o mesmo id.
    public static final Long SETTINGS_ID = 1L;

    private static final double MIN_SECONDS_PER_CARD = 2.0;
    private static final double MAX_SECONDS_PER_CARD = 15.0;

    // Teto de 60s de propósito: imagem parada muito tempo é justamente o que
    // o pixel shift do /display tenta evitar (marca na tela da TV).
    private static final int MIN_BANNER_DURATION_SECONDS = 5;
    private static final int MAX_BANNER_DURATION_SECONDS = 60;
    private static final int MIN_BANNER_INTERVAL_MINUTES = 1;
    private static final int MAX_BANNER_INTERVAL_MINUTES = 30;

    private static final boolean DEFAULT_BANNER_ENABLED = false;
    private static final int DEFAULT_BANNER_DURATION_SECONDS = 15;
    private static final int DEFAULT_BANNER_INTERVAL_MINUTES = 2;
    // Tela cheia foi o primeiro formato do banner — base antiga continua igual.
    private static final DisplayBannerSize DEFAULT_BANNER_SIZE = DisplayBannerSize.FULL;
    // Canto que esconde menos: data, relógio e horários continuam à vista.
    private static final DisplayBannerPosition DEFAULT_BANNER_POSITION = DisplayBannerPosition.BOTTOM_RIGHT;

    private final DisplaySettingsRepository displaySettingsRepository;
    private final DisplayBannerRepository displayBannerRepository;

    public DisplaySettingsService(DisplaySettingsRepository displaySettingsRepository,
                                  DisplayBannerRepository displayBannerRepository) {
        this.displaySettingsRepository = displaySettingsRepository;
        this.displayBannerRepository = displayBannerRepository;
    }

    // Nasce sozinha na primeira leitura, com o comportamento que já estava no
    // ar antes desta configuração existir (letreiro, 6s) — não depende de
    // data.sql nem de nenhuma migração manual.
    public DisplaySettingsDto getSettings() {
        DisplaySettings settings = displaySettingsRepository.findById(SETTINGS_ID)
                .orElseGet(this::createDefaultSettings);
        applyDefaults(settings);
        return toDto(settings);
    }

    public DisplaySettingsDto updateSettings(DisplaySettingsDto dto) {
        if (dto.getSecondsPerCard() == null
                || dto.getSecondsPerCard() < MIN_SECONDS_PER_CARD
                || dto.getSecondsPerCard() > MAX_SECONDS_PER_CARD) {
            throw new BusinessRuleException(
                    "A velocidade precisa estar entre " + MIN_SECONDS_PER_CARD + " e " + MAX_SECONDS_PER_CARD + " segundos.");
        }
        if (dto.getMode() == null) {
            throw new BusinessRuleException("O modo de exibição é obrigatório.");
        }

        DisplaySettings settings = displaySettingsRepository.findById(SETTINGS_ID)
                .orElseGet(this::createDefaultSettings);
        applyDefaults(settings);

        // Campos de banner opcionais no PUT (null = mantém): o painel antigo,
        // que só conhece modo e velocidade, continua funcionando sem apagar
        // a configuração do banner.
        int duration = dto.getBannerDurationSeconds() != null
                ? dto.getBannerDurationSeconds()
                : settings.getBannerDurationSeconds();
        if (duration < MIN_BANNER_DURATION_SECONDS || duration > MAX_BANNER_DURATION_SECONDS) {
            throw new BusinessRuleException(
                    "A duração do banner precisa estar entre " + MIN_BANNER_DURATION_SECONDS + " e " + MAX_BANNER_DURATION_SECONDS + " segundos.");
        }

        int interval = dto.getBannerIntervalMinutes() != null
                ? dto.getBannerIntervalMinutes()
                : settings.getBannerIntervalMinutes();
        if (interval < MIN_BANNER_INTERVAL_MINUTES || interval > MAX_BANNER_INTERVAL_MINUTES) {
            throw new BusinessRuleException(
                    "O intervalo do banner precisa estar entre " + MIN_BANNER_INTERVAL_MINUTES + " e " + MAX_BANNER_INTERVAL_MINUTES + " minutos.");
        }

        boolean enabled = dto.getBannerEnabled() != null
                ? dto.getBannerEnabled()
                : settings.getBannerEnabled();
        if (enabled && bannerVersion() == null) {
            throw new BusinessRuleException("Envie uma imagem antes de ativar o banner.");
        }

        settings.setSecondsPerCard(dto.getSecondsPerCard());
        settings.setMode(dto.getMode());
        settings.setBannerEnabled(enabled);
        settings.setBannerDurationSeconds(duration);
        settings.setBannerIntervalMinutes(interval);
        if (dto.getBannerSize() != null) {
            settings.setBannerSize(dto.getBannerSize());
        }
        if (dto.getBannerPosition() != null) {
            settings.setBannerPosition(dto.getBannerPosition());
        }

        return toDto(displaySettingsRepository.save(settings));
    }

    // Chamado quando a imagem é removida (DisplayBannerService.delete).
    public void disableBanner() {
        DisplaySettings settings = displaySettingsRepository.findById(SETTINGS_ID)
                .orElseGet(this::createDefaultSettings);
        applyDefaults(settings);
        settings.setBannerEnabled(false);
        displaySettingsRepository.save(settings);
    }

    private DisplaySettings createDefaultSettings() {
        DisplaySettings settings = new DisplaySettings();
        settings.setId(SETTINGS_ID);
        settings.setSecondsPerCard(6.0);
        settings.setMode(DisplayCarouselMode.MARQUEE);
        applyDefaults(settings);
        return displaySettingsRepository.save(settings);
    }

    // A linha que já existia antes do banner ganha as colunas novas pelo
    // ddl-auto=update, mas com null — trata como padrão aqui, em vez de
    // depender de migração ou de alguém salvar o painel primeiro. Só ajusta o
    // objeto em memória; vai pro banco no próximo PUT.
    private void applyDefaults(DisplaySettings settings) {
        if (settings.getBannerEnabled() == null) {
            settings.setBannerEnabled(DEFAULT_BANNER_ENABLED);
        }
        if (settings.getBannerDurationSeconds() == null) {
            settings.setBannerDurationSeconds(DEFAULT_BANNER_DURATION_SECONDS);
        }
        if (settings.getBannerIntervalMinutes() == null) {
            settings.setBannerIntervalMinutes(DEFAULT_BANNER_INTERVAL_MINUTES);
        }
        if (settings.getBannerSize() == null) {
            settings.setBannerSize(DEFAULT_BANNER_SIZE);
        }
        if (settings.getBannerPosition() == null) {
            settings.setBannerPosition(DEFAULT_BANNER_POSITION);
        }
    }

    private Long bannerVersion() {
        return displayBannerRepository.findVersionById(SETTINGS_ID).orElse(null);
    }

    private DisplaySettingsDto toDto(DisplaySettings settings) {
        return DisplaySettingsDto.fromEntity(settings, bannerVersion());
    }
}
