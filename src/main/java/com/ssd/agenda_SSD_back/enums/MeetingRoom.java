package com.ssd.agenda_SSD_back.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MeetingRoom {
    APOIO("APOIO"),
    CIEGES("CIEGES"),
    SALA_WEB("SALA WEB");

    private final String label;

    MeetingRoom(String label) {
        this.label = label;
    }

    // Serializa/desserializa pelo label ("SALA WEB", com espaço) em vez do
    // name() do enum (SALA_WEB) — o front já manda e espera esse formato.
    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static MeetingRoom fromLabel(String label) {
        String normalized = label == null ? null : label.trim();
        for (MeetingRoom room : values()) {
            if (room.label.equalsIgnoreCase(normalized) || room.name().equalsIgnoreCase(normalized)) {
                return room;
            }
        }
        throw new IllegalArgumentException("Sala inválida: " + label);
    }
}
