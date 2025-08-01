package com.ssd.agenda_SSD_back.service;


import com.ssd.agenda_SSD_back.dto.MeetingDto;
import com.ssd.agenda_SSD_back.entity.Meeting;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.repository.MeetingRepository;
import com.ssd.agenda_SSD_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetingService {
    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    // Criar função para Salvar meeting
    public Meeting saveMeeting(MeetingDto meetingDto) {
        User user = userRepository.findById(meetingDto.getUserId()).orElseThrow(()-> new RuntimeException("Usuário não encontrado com ID: " + meetingDto.getUserId()));
        Meeting meeting = MeetingDto.toEntity(meetingDto, user);
        Meeting savedMeeting = meetingRepository.save(meeting);
        return savedMeeting;
    }
    // Criar função para Apagar meeting
   public void deleteMeeting(Long id) {
        Meeting existingMeeting = meetingRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Reunião não encontrada com ID: " + id));
        meetingRepository.delete(existingMeeting);
    }

    // Criar função para Encontrar meeting por Id
    public MeetingDto findMeetingById(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Reunião não encontrada com ID: " + id));
        return MeetingDto.fromEntity(meeting);
    }

    // Buscar todas as reuniões
    public List<MeetingDto> findAllMeetingDto(){
        List<Meeting> meetings = meetingRepository.findAll();
        return meetings.stream().map(MeetingDto::fromEntity).toList();
    }

    // Atualizar reuniões
    public Meeting updateMeeting(Long id, MeetingDto meetingDto){
        // Busca reunião existente
        Meeting existingMeeting = meetingRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Reunião não encontrada com ID: " + id));

        User user =  userRepository.findById(meetingDto.getUserId())
                .orElseThrow(()-> new RuntimeException("Usuário não encontrado com ID: " + id));

        Meeting updatedMeeting = MeetingDto.toEntity(meetingDto, user);

        // Validar sobreposição de horários

        // Verificar Mudanças

        // Atualizar os dados da reunião

        existingMeeting.setTitle(updatedMeeting.getTitle());
        existingMeeting.setUpdateDate(updatedMeeting.getUpdateDate());
        existingMeeting.setMeetingDate(updatedMeeting.getMeetingDate());
        existingMeeting.setTimeStart(updatedMeeting.getTimeStart());
        existingMeeting.setTimeEnd(updatedMeeting.getTimeEnd());
        existingMeeting.setHostUser(updatedMeeting.getHostUser());
        return meetingRepository.save(existingMeeting);
    }

    public List<Meeting> findAllMeetings(){
        return meetingRepository.findAll();
    }

}
