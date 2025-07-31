package com.ssd.agenda_SSD_back.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name="log")
public class LogUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="action", nullable = false)
    private String action; // Ações: CREATE, UPDATE, DELETE
    @Column(name="log_date", nullable = false)
    private LocalDateTime logDateTime;
    @Column(name="change_fields",nullable = true, columnDefinition = "TEXT")
    private String changedFields; // Campos e valores alterados
    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name="user", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }
}
