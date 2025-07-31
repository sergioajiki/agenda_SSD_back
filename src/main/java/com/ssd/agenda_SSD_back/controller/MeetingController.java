package com.ssd.agenda_SSD_back.controller;

import com.ssd.agenda_SSD_back.dto.MeetingDto;
import com.ssd.agenda_SSD_back.entity.Meeting;
import com.ssd.agenda_SSD_back.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Tag(name = "Meetings", description = "Gerenciamento de Reuniões")
@RequestMapping("/api/meeting")
public class MeetingController {
    @Autowired
    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    /**
     * Cria uma nova reunião.
     *
     * @param meetingDto DTO com os dados da reunião.
     * @return DTO da reunião criada.
     */
    @PostMapping
    @Operation(summary = "Cadastrar uma reunião", description = "Agenda uma nova reunião no sistema")
    public ResponseEntity<MeetingDto> createMeeting(@RequestBody MeetingDto meetingDto) {
        Meeting savedMeeting = meetingService.saveMeeting(meetingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(MeetingDto.fromEntity(savedMeeting));
    }
}
