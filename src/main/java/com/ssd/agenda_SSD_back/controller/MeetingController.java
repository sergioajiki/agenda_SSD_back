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

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Atualiza uma reunião agendada.
     *
     * @param id         ID da reunião a ser atualizada.
     * @param meetingDto Dto com os dados atualizados.
     * @return DTO da reunião atualizada.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma reunião", description = "Atualiza os dados de uma reunião agendada no sistema")
    public ResponseEntity<MeetingDto> updateMeeting(@PathVariable Long id, @RequestBody MeetingDto meetingDto) {
        Meeting updatedMeeting = meetingService.updateMeeting(id, meetingDto);
        return ResponseEntity.ok(MeetingDto.fromEntity(updatedMeeting));
    }

    /**
     * Remove uma reunião existente.
     *
     * @param id ID da reunião a ser removida.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover uma reunião", description = "Remove uma reunião agendada no sistema")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna todas as reuniões.
     *
     * @return Lista de DTOs das reuniões.
     */
    @GetMapping
    @Operation(summary = "Listar todas as reuniões", description = "Retorna uma lista com todas as reuniões agendadas")
    public ResponseEntity<List<MeetingDto>> getAllMeetings() {
        List<MeetingDto> meetings = meetingService.findAllMeetings()
                .stream()
                .map(MeetingDto::fromEntity) //Converte Meeting para MeetingDto
                .collect(Collectors.toList());
        return ResponseEntity.ok(meetings);
    }

    /**
     * Retorna uma reunião pelo ID.
     *
     * @param id ID da reunião.
     * @return DTO da reunião encontrada.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar uma reunião pelo ID", description = "Retorna os dados de uma reunião agendada")
    public ResponseEntity<MeetingDto> getMeetingById(@PathVariable Long id) {
        MeetingDto meetingDto = meetingService.findMeetingById(id);
        return ResponseEntity.ok(meetingDto);
    }

}
