package com.ssd.agenda_SSD_back.exception;

public class ScheduleOverlapException extends RuntimeException{
    public ScheduleOverlapException(String message) {
        super(message);
    }
}
