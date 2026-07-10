package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.dto.MeetingDto;
import com.ssd.agenda_SSD_back.entity.LogUpdate;
import com.ssd.agenda_SSD_back.entity.Meeting;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.enums.UserRole;
import com.ssd.agenda_SSD_back.exception.BusinessRuleException;
import com.ssd.agenda_SSD_back.exception.NotFoundException;
import com.ssd.agenda_SSD_back.exception.ScheduleOverlapException;
import com.ssd.agenda_SSD_back.repository.LogUpdateRepository;
import com.ssd.agenda_SSD_back.repository.MeetingRepository;
import com.ssd.agenda_SSD_back.repository.UserRepository;
import com.ssd.agenda_SSD_back.util.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MeetingService {
    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogUpdateRepository logUpdateRepository;

    // Criar função para Salvar meeting
    public Meeting saveMeeting(MeetingDto meetingDto, String requestingUserEmail) {
        // Validação: hora inicial < hora final
        if (!meetingDto.getTimeStart().isBefore(meetingDto.getTimeEnd())) {
            throw new BusinessRuleException("O horário de início deve ser antes do horário de término");
        }

        // O dono da reunião é sempre o usuário autenticado — o "userId" que
        // vem no DTO é ignorado pra criação, evitando que alguém agende em
        // nome de outra pessoa só preenchendo esse campo no corpo da requisição.
        User user = userRepository.findByEmail(requestingUserEmail)
                .orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado: " + requestingUserEmail));
        Meeting meeting = MeetingDto.toEntity(meetingDto, user);

        //Valida sobreposição
        boolean hasConflit = meetingRepository.existOverlappingMeeting(
                meeting.getMeetingDate(),
                meeting.getMeetingRoom(),
                meeting.getTimeStart(),
                meeting.getTimeEnd()
        );

        if (hasConflit) {
            throw new ScheduleOverlapException("Já existe uma reunião nesta sala e horário");
        }
        Meeting savedMeeting = meetingRepository.save(meeting);

        // Registrar log de criação
        registerLog("CREATE", null, savedMeeting.getId(), savedMeeting.getMeetingRoom(), user);

        return savedMeeting;
    }

    // Criar função para Apagar meeting
    public void deleteMeeting(Long id, String requestingUserEmail) {
        Meeting existingMeeting = meetingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunião não encontrada com ID: " + id));
        // Verifica permissão
        verificarPermissao(existingMeeting, requestingUserEmail);
        // Registra Log de exclusão
        registerLog("DELETE", null, existingMeeting.getId(), existingMeeting.getMeetingRoom(), existingMeeting.getHostUser());
        meetingRepository.delete(existingMeeting);
    }

    // Criar função para Encontrar meeting por Id
    public MeetingDto findMeetingById(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunião não encontrada com ID: " + id));
        return MeetingDto.fromEntity(meeting);
    }

    // Buscar todas as reuniões
    public List<MeetingDto> findAllMeetingDto() {
        List<Meeting> meetings = meetingRepository.findAll();
        return meetings.stream().map(MeetingDto::fromEntity).toList();
    }

    // Atualizar reuniões
    public Meeting updateMeeting(Long id, MeetingDto meetingDto, String requestingUserEmail) {
        // Validação: hora inicial < hora final
        if (!meetingDto.getTimeStart().isBefore(meetingDto.getTimeEnd())) {
            throw new BusinessRuleException("O horário de início deve ser antes do horário de término");
        }

        // Busca reunião existente
        Meeting existingMeeting = meetingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunião não encontrada com ID: " + id));

        //  Apenas criador ou ADMIN pode alterar
        verificarPermissao(existingMeeting, requestingUserEmail);

        // O dono da reunião não muda numa edição — o front não manda mais
        // "userId" no corpo (o dono só é decidido na criação, a partir do
        // token). Antes esse valor era usado pra resolver o usuário aqui, o
        // que também tinha o efeito colateral de trocar o dono da reunião
        // silenciosamente quando um ADMIN editava a reunião de outra pessoa.
        Meeting updatedMeeting = MeetingDto.toEntity(meetingDto, existingMeeting.getHostUser());
        // Ajustar ID para comparação correta e consistência
        updatedMeeting.setId(existingMeeting.getId());

        // Validar sobreposição de horários
        boolean hasConflit = meetingRepository.existsOverlappingMeetingExcludingId(
                updatedMeeting.getMeetingDate(),
                updatedMeeting.getMeetingRoom(),
                updatedMeeting.getTimeStart(),
                updatedMeeting.getTimeEnd(),
                existingMeeting.getId()
        );
        if (hasConflit) {
            throw new ScheduleOverlapException("já existe uma reunião nesta sala e horário");
        }

        // Verificar Mudanças
        Map<String, String> changes = LogUtils.getChangeFields(existingMeeting, updatedMeeting);

        // Atualizar os dados da reunião
        existingMeeting.setTitle(updatedMeeting.getTitle());
        existingMeeting.setUpdateDate(updatedMeeting.getUpdateDate());
        existingMeeting.setMeetingDate(updatedMeeting.getMeetingDate());
        existingMeeting.setTimeStart(updatedMeeting.getTimeStart());
        existingMeeting.setTimeEnd(updatedMeeting.getTimeEnd());
        existingMeeting.setMeetingRoom(updatedMeeting.getMeetingRoom());
        existingMeeting.setHostUser(updatedMeeting.getHostUser());

        // Registra Log de atualização
        registerLog("UPDATE", changes, existingMeeting.getId(), existingMeeting.getMeetingRoom(), existingMeeting.getHostUser());

        return meetingRepository.save(existingMeeting);
    }

    public List<Meeting> findAllMeetings() {
        return meetingRepository.findAll();
    }

    private void registerLog(String action, Map<String, String> changes, Long meetingId, String meetingRoom, User user) {
        LogUpdate log = new LogUpdate();
        log.setAction(action);
        log.setChangedFields(changes != null ? changes.toString() : null);
        log.setMeetingId(meetingId);
        log.setMeetingRoom(meetingRoom);
        log.setUpdatedBy(user);
        log.setLogDateTime(LocalDateTime.now());

        logUpdateRepository.save(log);
    }

    private void verificarPermissao(Meeting meeting, String requestingUserEmail) {
        // O email vem do token JWT (Authentication.getName()), não mais de um
        // parâmetro que o próprio cliente informava — por isso dá pra confiar
        // nele pra decidir permissão.
        User requestingUser = userRepository.findByEmail(requestingUserEmail)
                .orElseThrow(() -> new NotFoundException("Usuário autenticado não encontrado: " + requestingUserEmail));

        boolean isOwner = meeting.getHostUser().getId().equals(requestingUser.getId());
        boolean isAdmin = requestingUser.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new BusinessRuleException("Somente o criador da reunião ou um administrador pode executar esta ação.");
        }
    }

}
