package com.ssd.agenda_SSD_back.dto;

import com.ssd.agenda_SSD_back.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

public class LodUpdateDto {
    @Schema(description = "Id do Log")
    private Long id;
    @Schema(description = "Operação que modifica o a reunião")
    private String action;
    @Schema(description = "Campo alterado")
    private String changedFields;
    @Schema(description = "Data da alteração")
    private LocalDateTime logDateTime;

    // ID do usuário (sem vínculo de chave estrangeira, para manter as informações do log após a exclusão do usuário)
    // @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User updatedBy;

    // ID da reunião (sem vínculo de chave estrangeira, para manter as informações do log após a exclusão da reunião)
    @JoinColumn(name = "meeting_id", nullable = true)
    private Long meetingId; // Tarefa associada à alteração

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getLogDateTime() {
        return logDateTime;
    }

    public void setLogDateTime(LocalDateTime logDateTime) {
        this.logDateTime = logDateTime;
    }

    public String getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
