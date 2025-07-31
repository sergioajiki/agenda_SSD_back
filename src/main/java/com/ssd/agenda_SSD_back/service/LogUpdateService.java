package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.repository.LogUpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogUpdateService {
    @Autowired
    private final LogUpdateRepository logUpdateRepository;

    public LogUpdateService(LogUpdateRepository logUpdateRepository){
        this.logUpdateRepository = logUpdateRepository;
    }

    // Salvar Log no BD

    // Buscar todos os logs

    // Buscar os logs por titulo

    // Buscar os logs por período

    // Buscar os logs por usuário
}
