package com.ssd.agenda_SSD_back.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ssd.agenda_SSD_back.entity.Meeting;
import com.ssd.agenda_SSD_back.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MeetingDto {
    @Schema(description = "Id da tarefa")
    private Long id;

    @Schema(description = "Título da reunião")
    @NotBlank(message = "O título da reunião é obrigatório.")
    private String title;

    @Schema(description = "Data de agendamento da tarefa")
    @NotNull(message = "A data de agendamento é obrigatória.")
    @FutureOrPresent(message = "A data de agendamento deve ser no presente ou no futuro.")
    @JsonFormat(pattern = "yyyy-MM-dd") // Formato para a data de agendamento
    private LocalDate meetingDate;

    @Schema(description = "Horário de início da reunião")
    @NotNull(message = "O horário de início é obrigatório.")
    @JsonFormat(pattern = "HH:mm") // Formato para o horário
    private LocalTime timeStart;

    @Schema(description = "Horário de término da reunião")
    @NotNull(message = "O horário de término é obrigatório.")
    @JsonFormat(pattern = "HH:mm") // Formato para o horário
    private LocalTime timeEnd;

    @Schema(description = "Local da reunião")
    @NotNull(message = "Necessário informar a sala de reunião")
    private String meetingRoom;

    @Schema(description = "ID do usuário responsável pela reunião")
    @NotNull(message = "O ID do usuário responsável é obrigatório.")
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalTime getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(LocalTime timeStart) {
        this.timeStart = timeStart;
    }

    public LocalDate getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(LocalDate meetingDate) {
        this.meetingDate = meetingDate;
    }

    public LocalTime getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(LocalTime timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getMeetingRoom() {
        return meetingRoom;
    }

    public void setMeetingRoom(String meetingRoom) {
        this.meetingRoom = meetingRoom;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public static Meeting toEntity(MeetingDto meetingDto, User user) {
        Meeting meeting = new Meeting();
        meeting.setTitle(meetingDto.getTitle());
        meeting.setMeetingDate(meetingDto.getMeetingDate());
        meeting.setTimeStart(meetingDto.getTimeStart());
        meeting.setTimeEnd(meetingDto.getTimeEnd());
        meeting.setUpdateDate(LocalDateTime.now()); // Define a data de criação/atualização
        meeting.setMeetingRoom(meetingDto.getMeetingRoom());
        meeting.setHostUser(user);
        return meeting;
    }

    public static MeetingDto fromEntity(Meeting meeting) {
        MeetingDto meetingDto = new MeetingDto();
        meetingDto.setId(meeting.getId());
        meetingDto.setTitle(meeting.getTitle());
        meetingDto.setMeetingDate(meeting.getMeetingDate());
        meetingDto.setTimeStart(meeting.getTimeStart());
        meetingDto.setTimeEnd(meeting.getTimeEnd());
        meetingDto.setMeetingRoom(meeting.getMeetingRoom());
        meetingDto.setUserId(meeting.getHostUser().getId());
        return meetingDto;
    }
}
