package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.dto.LogUpdateDto;
import com.ssd.agenda_SSD_back.entity.LogUpdate;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.repository.LogUpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogUpdateService {
    @Autowired
    private final LogUpdateRepository logUpdateRepository;

    public LogUpdateService(LogUpdateRepository logUpdateRepository) {
        this.logUpdateRepository = logUpdateRepository;
    }

    // Salvar Log no BD
    public void saveLog(LogUpdate logUpdate) {
        logUpdateRepository.save(logUpdate);
    }

    // Buscar todos os logs
    public List<LogUpdateDto> getAllLogs() {
        List<LogUpdate> logs = logUpdateRepository.findAll();

        return logs.stream()
                .map(LogUpdateDto::fromEntity)
                .toList();
    }

    // Buscar os logs por titulo
    public List<LogUpdate> getLogsByMeetingId(Long meeting) {
        return logUpdateRepository.findByMeetingId(meeting);
    }

    // Buscar os logs por período
    public List<LogUpdate> getLogsByPeriod(LocalDateTime start, LocalDateTime end) {
        return logUpdateRepository.findByUpdateDateTimeBetween(start, end);
    }

    // Buscar os logs por usuário
    public List<LogUpdate> getLogsByUser(User user) {
        return logUpdateRepository.findByUpdatedBy(user);
    }
}
