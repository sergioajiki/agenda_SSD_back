package com.ssd.agenda_SSD_back.service;


import com.ssd.agenda_SSD_back.entity.Meeting;
import com.ssd.agenda_SSD_back.repository.MeetingRepository;
import com.ssd.agenda_SSD_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeetingService {
    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    // Criar função para Salvar meeting

    // Criar função para Apagar meeting

    // Criar função para Encortrar meeting por Id

    // Buscar todas as reuniões

    // Atualizar reuniões

}
