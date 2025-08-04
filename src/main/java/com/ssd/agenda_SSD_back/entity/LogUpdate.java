package com.ssd.agenda_SSD_back.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log")
public class LogUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "action", nullable = false)
    private String action; // Ações: CREATE, UPDATE, DELETE
    @Column(name = "log_date", nullable = false)
    private LocalDateTime logDateTime;
    @Column(name = "change_fields", nullable = true, columnDefinition = "TEXT")
    private String changedFields; // Campos e valores alterados
    @Column(name = "meeting_room", nullable = false)
    private String meetingRoom;

    // ID do usuário (sem vínculo de chave estrangeira, para manter as informações do log após a exclusão do usuário)
    @ManyToOne
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

    public String getMeetingRoom() {
        return meetingRoom;
    }

    public void setMeetingRoom(String meetingRoom) {
        this.meetingRoom = meetingRoom;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public String getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }



}
