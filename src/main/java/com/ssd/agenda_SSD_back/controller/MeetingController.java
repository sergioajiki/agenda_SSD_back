package com.ssd.agenda_SSD_back.controller;

import com.ssd.agenda_SSD_back.dto.MeetingDto;
import com.ssd.agenda_SSD_back.entity.Meeting;
import com.ssd.agenda_SSD_back.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
     * O dono da reunião passou a ser sempre o usuário autenticado (extraído
     * do token JWT) — não confia mais no "userId" que vinha no corpo da
     * requisição, que qualquer chamador podia preencher com o id de outra
     * pessoa. O front já só manda o id do próprio usuário logado mesmo, então
     * isso não muda o comportamento visível, só fecha a brecha de spoofing.
     *
     * @param meetingDto     DTO com os dados da reunião.
     * @param authentication Usuário autenticado, injetado pelo Spring Security a partir do token.
     * @return DTO da reunião criada.
     */
    @PostMapping
    @Operation(summary = "Cadastrar uma reunião", description = "Agenda uma nova reunião no sistema (exige login)")
    public ResponseEntity<MeetingDto> createMeeting(@RequestBody MeetingDto meetingDto, Authentication authentication) {
        Meeting savedMeeting = meetingService.saveMeeting(meetingDto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(MeetingDto.fromEntity(savedMeeting));
    }

    /**
     * Atualiza uma reunião agendada.
     *
     * @param id             ID da reunião a ser atualizada.
     * @param meetingDto     Dto com os dados atualizados.
     * @param authentication Usuário autenticado — substitui o antigo "requestingUserId"
     *                       que vinha como query param direto do cliente, sem prova de identidade.
     * @return DTO da reunião atualizada.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma reunião", description = "Atualiza os dados de uma reunião agendada no sistema (dono ou ADMIN)")
    public ResponseEntity<MeetingDto> updateMeeting(
            @PathVariable Long id,
            @RequestBody MeetingDto meetingDto,
            Authentication authentication
    ) {
        Meeting updatedMeeting = meetingService.updateMeeting(id, meetingDto, authentication.getName());
        return ResponseEntity.ok(MeetingDto.fromEntity(updatedMeeting));
    }

    /**
     * Remove uma reunião existente.
     *
     * @param id             ID da reunião a ser removida.
     * @param authentication Usuário autenticado — mesma troca do endpoint de atualização.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover uma reunião", description = "Remove uma reunião agendada no sistema (dono ou ADMIN)")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable Long id,
            Authentication authentication
    ) {
        meetingService.deleteMeeting(id, authentication.getName());
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
