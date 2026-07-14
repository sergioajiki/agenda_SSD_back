package com.ssd.agenda_SSD_back.converter;

import com.ssd.agenda_SSD_back.enums.MeetingRoom;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// Grava/lê pelo mesmo label usado no JSON ("SALA WEB", com espaço) em vez do
// name() do enum (SALA_WEB). Isso bate com o texto que já estava gravado nas
// reuniões criadas antes deste enum existir, quando meetingRoom era uma
// String livre — evita quebrar a leitura de dado histórico.
@Converter(autoApply = true)
public class MeetingRoomConverter implements AttributeConverter<MeetingRoom, String> {

    @Override
    public String convertToDatabaseColumn(MeetingRoom meetingRoom) {
        return meetingRoom == null ? null : meetingRoom.getLabel();
    }

    @Override
    public MeetingRoom convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MeetingRoom.fromLabel(dbData);
    }
}
