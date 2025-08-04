package com.ssd.agenda_SSD_back.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssd.agenda_SSD_back.dto.ErrorMessageDto;

import java.util.List;

@JsonInclude
public record Problem(
        int status,
        String message,
        String detail,
        List<ErrorMessageDto> errors
) {
}
