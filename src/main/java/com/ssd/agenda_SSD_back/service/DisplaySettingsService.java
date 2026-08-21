package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.dto.DisplaySettingsDto;
import com.ssd.agenda_SSD_back.entity.DisplaySettings;
import com.ssd.agenda_SSD_back.enums.DisplayCarouselMode;
import com.ssd.agenda_SSD_back.exception.BusinessRuleException;
import com.ssd.agenda_SSD_back.repository.DisplaySettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class DisplaySettingsService {
    // Linha única — não existe uma tela de "lista de configurações", só esse
    // ajuste. Evita modelar uma tabela chave/valor genérica pra um caso de uso
    // deste tamanho.
    private static final Long SETTINGS_ID = 1L;

    private static final double MIN_SECONDS_PER_CARD = 2.0;
    private static final double MAX_SECONDS_PER_CARD = 15.0;

    private final DisplaySettingsRepository displaySettingsRepository;

    public DisplaySettingsService(DisplaySettingsRepository displaySettingsRepository) {
        this.displaySettingsRepository = displaySettingsRepository;
    }

    // Nasce sozinha na primeira leitura, com o comportamento que já estava no
    // ar antes desta configuração existir (letreiro, 6s) — não depende de
    // data.sql nem de nenhuma migração manual.
    public DisplaySettingsDto getSettings() {
        DisplaySettings settings = displaySettingsRepository.findById(SETTINGS_ID)
                .orElseGet(this::createDefaultSettings);
        return DisplaySettingsDto.fromEntity(settings);
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
        settings.setSecondsPerCard(dto.getSecondsPerCard());
        settings.setMode(dto.getMode());

        return DisplaySettingsDto.fromEntity(displaySettingsRepository.save(settings));
    }

    private DisplaySettings createDefaultSettings() {
        DisplaySettings settings = new DisplaySettings();
        settings.setId(SETTINGS_ID);
        settings.setSecondsPerCard(6.0);
        settings.setMode(DisplayCarouselMode.MARQUEE);
        return displaySettingsRepository.save(settings);
    }
}
