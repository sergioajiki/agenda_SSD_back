package com.ssd.agenda_SSD_back.advice;

import com.ssd.agenda_SSD_back.dto.ErrorMessageDto;
import com.ssd.agenda_SSD_back.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

    @ExceptionHandler
    public ResponseEntity<Problem> handleScheduleOverlapException(ScheduleOverlapException exception) {
        Problem problem = new Problem(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request Info",
                exception.getLocalizedMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    // Ex.: senha atual incorreta ao trocar perfil/senha (UserService.verifyCurrentPassword).
    // login() e createUser() continuam com try/catch local pra isso (não passam por aqui).
    @ExceptionHandler
    public ResponseEntity<Problem> handleIllegalArgumentException(IllegalArgumentException exception) {
        Problem problem = new Problem(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request Info",
                exception.getLocalizedMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    // Disparada pelo @PreAuthorize quando o usuário está autenticado mas não
    // tem a role exigida (ex.: USER tentando cadastrar outro usuário). Erros
    // de "sem token nenhum" (401) não passam por aqui — são tratados antes,
    // no SecurityConfig, porque acontecem fora do ciclo do DispatcherServlet.
    @ExceptionHandler
    public ResponseEntity<Problem> handleAccessDeniedException(AccessDeniedException exception) {
        Problem problem = new Problem(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Você não tem permissão para executar esta ação.",
                null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }
}
