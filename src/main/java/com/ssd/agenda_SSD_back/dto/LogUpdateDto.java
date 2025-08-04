package com.ssd.agenda_SSD_back.dto;

import com.ssd.agenda_SSD_back.entity.LogUpdate;
import com.ssd.agenda_SSD_back.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class LogUpdateDto {
    @Schema(description = "Id do Log")
    private Long id;
    @Schema(description = "Operação que modifica o a reunião")
    private String action;
    @Schema(description = "Campo alterado")
    private String changedFields;
    @Schema(description = "Data da alteração")
    private LocalDateTime logDateTime;
    @Schema(description = "Usuário responsável pela atualização")
    private UpdatedByDto updatedBy;
    @Schema(description = "Id da reunião")
    private Long meetingId;

    public LogUpdateDto(Long id, Long meetingId, LocalDateTime logDateTime, UpdatedByDto updatedBy, String changedFields, String action) {
        this.id = id;
        this.meetingId = meetingId;
        this.logDateTime = logDateTime;
        this.updatedBy = updatedBy;
        this.changedFields = changedFields;
        this.action = action;
    }

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

    public LocalDateTime getLogDateTime() {
        return logDateTime;
    }

    public void setLogDateTime(LocalDateTime logDateTime) {
        this.logDateTime = logDateTime;
    }

    public UpdatedByDto getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UpdatedByDto updatedBy) {
        this.updatedBy = updatedBy;
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

    public static LogUpdateDto fromEntity(LogUpdate log) {
        return new LogUpdateDto(
                log.getId(),
                log.getMeetingId() != null ? log.getMeetingId() : null,
                log.getLogDateTime(),
                new UpdatedByDto(log.getUpdatedBy().getId(), log.getUpdatedBy().getName()),
                log.getChangedFields(),
                log.getAction()
        );
    }

}
