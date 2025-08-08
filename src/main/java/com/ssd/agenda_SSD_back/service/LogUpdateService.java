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
    public List<LogUpdateDto> getLogsByMeetingId(Long meeting) {

        List<LogUpdate> log = logUpdateRepository.findByMeetingId(meeting);
        return log.stream()
                .map(LogUpdateDto::fromEntity)
                .toList();
    }

    // Buscar os logs por período
    public List<LogUpdateDto> getLogsByPeriod(LocalDateTime start, LocalDateTime end) {
        List<LogUpdate> logs = logUpdateRepository.findByLogDateTimeBetween(start, end);

        return logs.stream()
                .map(LogUpdateDto::fromEntity)
                .toList();
    }

    // Buscar os logs por usuário
    public List<LogUpdateDto> getLogsByUser(User user) {
        List<LogUpdate> logs = logUpdateRepository.findByUpdatedBy(user);
        return logs.stream()
                .map(LogUpdateDto::fromEntity)
                .toList();
    }
}
