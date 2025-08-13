package com.ssd.agenda_SSD_back.service;

import com.ssd.agenda_SSD_back.dto.LogUpdateDto;
import com.ssd.agenda_SSD_back.entity.LogUpdate;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.repository.LogUpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public List<LogUpdateDto> getLogsByPeriod(
            LocalDate startDate, LocalTime startTime,
            LocalDate endDate, LocalTime endTime
    ) {
        // Se endDate não for informado, usar startDate
        LocalDate safeEndDate = (endDate != null) ? endDate : startDate;

        // Se hora não for informada, usar início ou fim do dia
        LocalTime safeStartTime = (startTime != null) ? startTime : LocalTime.MIN;
        LocalTime safeEndTime = (endTime != null) ? endTime : LocalTime.MAX;

        LocalDateTime startDateTime = LocalDateTime.of(startDate, safeStartTime);
        LocalDateTime endDateTime = LocalDateTime.of(safeEndDate, safeEndTime);

        List<LogUpdate> logs = logUpdateRepository.findByLogDateTimeBetween(startDateTime, endDateTime);

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
