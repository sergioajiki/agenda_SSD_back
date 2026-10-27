package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.dto.DisplaySettingsDto;
import com.ssd.agenda_SSD_back.entity.DisplayBanner;
import com.ssd.agenda_SSD_back.exception.BusinessRuleException;
import com.ssd.agenda_SSD_back.repository.DisplayBannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

/**
 * Imagem do banner da tela de recepção. As configurações de quando ela
 * aparece (ativo, duração, intervalo) ficam em DisplaySettingsService.
 */
@Service
public class DisplayBannerService {
    // Mesmo limite do spring.servlet.multipart.max-file-size — repetido aqui
    // pra regra não depender só da configuração do servidor.
    public static final int MAX_BYTES = 1024 * 1024;

    // Uma imagem de 1 MB pode declarar dimensões enormes no cabeçalho (PNG
    // muito comprimido) e estourar a memória de quem a decodificar — a TV
    // inclusive. 8000 px folga bem acima do recomendado (1800x1150).
    private static final int MAX_DIMENSION = 8000;

    private final DisplayBannerRepository displayBannerRepository;
    private final DisplaySettingsService displaySettingsService;

    public DisplayBannerService(DisplayBannerRepository displayBannerRepository,
                                DisplaySettingsService displaySettingsService) {
        this.displayBannerRepository = displayBannerRepository;
        this.displaySettingsService = displaySettingsService;
    }

    @Transactional
    public DisplaySettingsDto upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Selecione uma imagem para enviar.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessRuleException("A imagem passa de 1 MB.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessRuleException("Não foi possível ler o arquivo enviado.");
        }

        // Formato pelos primeiros bytes, não pela extensão nem pelo tipo que o
        // navegador informa — os dois mudam com um simples renomear. SVG fica
        // de fora de propósito: pode carregar script.
        String contentType = detectContentType(bytes);
        if (contentType == null) {
            throw new BusinessRuleException("Formato não aceito. Envie um arquivo PNG ou JPG.");
        }

        int[] size = readDimensions(bytes);
        if (size[0] > MAX_DIMENSION || size[1] > MAX_DIMENSION) {
            throw new BusinessRuleException(
                    "A imagem tem " + size[0] + " x " + size[1] + " px. O máximo é " + MAX_DIMENSION + " px de cada lado.");
        }

        boolean firstImage = !displayBannerRepository.existsById(DisplaySettingsService.SETTINGS_ID);
        DisplayBanner banner = displayBannerRepository.findById(DisplaySettingsService.SETTINGS_ID)
                .orElseGet(DisplayBanner::new);
        banner.setId(DisplaySettingsService.SETTINGS_ID);
        banner.setContent(bytes);
        banner.setContentType(contentType);
        banner.setWidth(size[0]);
        banner.setHeight(size[1]);
        banner.setVersion(System.currentTimeMillis());
        displayBannerRepository.save(banner);

        // Quem envia a primeira imagem quer vê-la na TV — liga junto, sem um
        // passo extra no painel. Numa troca de imagem o estado fica como
        // estava: um banner pausado continua pausado.
        if (firstImage) {
            displaySettingsService.setBannerEnabled(true);
        }

        return displaySettingsService.getSettings();
    }

    @Transactional(readOnly = true)
    public Optional<DisplayBanner> getBanner() {
        return displayBannerRepository.findById(DisplaySettingsService.SETTINGS_ID);
    }

    // Sem imagem não tem o que exibir — desliga junto, senão a configuração
    // ficaria "ativa" apontando pro nada.
    @Transactional
    public DisplaySettingsDto delete() {
        if (displayBannerRepository.existsById(DisplaySettingsService.SETTINGS_ID)) {
            displayBannerRepository.deleteById(DisplaySettingsService.SETTINGS_ID);
        }
        displaySettingsService.setBannerEnabled(false);
        return displaySettingsService.getSettings();
    }

    private static String detectContentType(byte[] b) {
        // PNG: 89 50 4E 47 ("\x89PNG")
        if (b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        // JPG: FF D8 FF
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        return null;
    }

    // Lê só o cabeçalho — não decodifica a imagem inteira (ver MAX_DIMENSION).
    private static int[] readDimensions(byte[] bytes) {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (in != null && readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in, true, true);
                    return new int[]{reader.getWidth(0), reader.getHeight(0)};
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException | RuntimeException e) {
            // cai no erro abaixo
        }
        throw new BusinessRuleException("Não foi possível ler a imagem. O arquivo pode estar corrompido.");
    }
}
