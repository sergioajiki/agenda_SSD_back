package com.ssd.agenda_SSD_back.repository;

import com.ssd.agenda_SSD_back.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM Meeting m
            WHERE m.meetingDate = :date
              AND m.meetingRoom = :room
              AND m.timeStart < :endTime
              AND m.timeEnd > :startTime
            """)
    boolean existOverlappingMeeting(
            @Param("date") LocalDate date,
            @Param("room") String room,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
            SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
            FROM Meeting m
            WHERE m.meetingDate = :date
              AND m.meetingRoom = :room
              AND m.timeStart < :endTime
              AND m.timeEnd > :startTime
              AND (:excludeId IS NULL OR m.id <> :excludeId)
            """)
    boolean existsOverlappingMeetingExcludingId(
            @Param("date") LocalDate date,
            @Param("room") String room,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
