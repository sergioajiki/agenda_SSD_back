package com.ssd.agenda_SSD_back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Imagem do banner da tela de recepção (/display). Linha única (id sempre 1L),
 * mesmo esquema de DisplaySettings.
 *
 * Fica no banco, e não numa pasta no disco, pra entrar no backup do H2
 * (DatabaseBackupService) sem nenhum ajuste. Fica numa tabela separada de
 * DisplaySettings porque a TV lê as configurações a cada 30s — com a imagem
 * na mesma linha, cada leitura carregaria até 1 MB à toa.
 */
@Entity
@Table(name = "display_banner")
public class DisplayBanner {
    @Id
    private Long id;

    @Lob
    @Column(length = 1_048_576)
    private byte[] content;

    // "image/png" ou "image/jpeg" — devolvido como Content-Type na leitura.
    private String contentType;

    private Integer width;

    private Integer height;

    // Momento do upload (epoch ms). A TV usa isso na URL (?v=) pra saber
    // quando baixar de novo, sem precisar comparar a imagem em si.
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
