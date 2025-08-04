package com.ssd.agenda_SSD_back.repository;

import com.ssd.agenda_SSD_back.entity.LogUpdate;
import com.ssd.agenda_SSD_back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogUpdateRepository extends JpaRepository<LogUpdate, Long> {
    //Busca pro tarefa
    List<LogUpdate> findByMeetingId(Long meeting);
    // Busca por intervalo de tempo
    List<LogUpdate> findByLogDateTimeBetween(LocalDateTime start, LocalDateTime end );
    //Busca por usuário
    List<LogUpdate> findByUpdatedBy(User user);
}
