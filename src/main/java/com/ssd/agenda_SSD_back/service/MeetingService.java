package com.ssd.agenda_SSD_back.service;


import com.ssd.agenda_SSD_back.dto.MeetingDto;
import com.ssd.agenda_SSD_back.entity.Meeting;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.repository.MeetingRepository;
import com.ssd.agenda_SSD_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;

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

    // Criar função para Encortrar meeting por Id

    // Buscar todas as reuniões

    // Atualizar reuniões

}
