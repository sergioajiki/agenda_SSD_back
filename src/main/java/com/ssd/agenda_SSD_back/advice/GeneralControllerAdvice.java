package com.ssd.agenda_SSD_back.advice;

import com.ssd.agenda_SSD_back.dto.ErrorMessageDto;
import com.ssd.agenda_SSD_back.exception.BusinessRuleException;
import com.ssd.agenda_SSD_back.exception.DuplicateEntryException;
import com.ssd.agenda_SSD_back.exception.InvalidEmailFormatException;
import com.ssd.agenda_SSD_back.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GeneralControllerAdvice {
    private final MessageSource messageSource;

    @Autowired
    public GeneralControllerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleNotFoundException(NotFoundException exception) {
        Problem problem = new Problem(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request Info",
                exception.getLocalizedMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleDuplicateEntryException(DuplicateEntryException exception) {
        Problem problem = new Problem(
                HttpStatus.CONFLICT.value(),
                "Duplicate Entry Info",
                exception.getLocalizedMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleInvalidEmailFormatException(InvalidEmailFormatException exception) {
        Problem problem = new Problem(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Email Format",
                exception.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleBusinessRuleExceptions(BusinessRuleException exception) {
        Problem problem = new Problem(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Business Rule",
                exception.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleNotFoundField(MethodArgumentNotValidException exception) {
        List<ErrorMessageDto> problemList = new ArrayList<>();

        exception.getBindingResult().getFieldErrors().forEach(e-> {
            String detail = messageSource.getMessage(e, LocaleContextHolder.getLocale());
            ErrorMessageDto messageDetail = new ErrorMessageDto(
                    e.getField(),
                    detail
            );
            problemList.add(messageDetail);
        });
        Problem problem = new Problem(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Parameters",
                "Invalid Request Body",
                problemList
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }
}
