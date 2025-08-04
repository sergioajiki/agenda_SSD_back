package com.ssd.agenda_SSD_back.exception;

public class InvalidEmailFormatException extends RuntimeException{
    public InvalidEmailFormatException(String message) {
        super(message);
    }
}
