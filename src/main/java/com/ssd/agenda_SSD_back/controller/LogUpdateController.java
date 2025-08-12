package com.ssd.agenda_SSD_back.controller;

import com.ssd.agenda_SSD_back.dto.LogUpdateDto;
import com.ssd.agenda_SSD_back.entity.LogUpdate;
import com.ssd.agenda_SSD_back.entity.User;
import com.ssd.agenda_SSD_back.service.LogUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Name;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@CrossOrigin
@RestController
@Tag(name = "Logs", description = "Logs de atualização das tarefas")
@RequestMapping("/api/logs")
public final class LogUpdateController {
    private final LogUpdateService logUpdateService;

    public LogUpdateController(LogUpdateService logUpdateService) {
        this.logUpdateService = logUpdateService;
    }

    @GetMapping
    @Operation(summary = "Lista todas as reuniões")
    public List<LogUpdateDto> getAllLogs() {
        return logUpdateService.getAllLogs();
    }

    @GetMapping("/meeting/{meetingId}")
    @Operation(summary = "Exibe as informações de uma reunião")
    public List<LogUpdateDto> getLogsByMeeting(@PathVariable Long meetingId) {
        return logUpdateService.getLogsByMeetingId(meetingId);
    }

    @GetMapping("/period")
    @Operation(summary = "Lista os logs por um período de data e hora")
    public List<LogUpdateDto> getLogsByPeriod(
            @RequestParam LocalDate startDate,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) LocalTime endTime
    ) {
        return logUpdateService.getLogsByPeriod(startDate, startTime, endDate, endTime);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lista os logs por usuário")
    public List<LogUpdateDto> getLogsByUser(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return logUpdateService.getLogsByUser(user);
    }
}

