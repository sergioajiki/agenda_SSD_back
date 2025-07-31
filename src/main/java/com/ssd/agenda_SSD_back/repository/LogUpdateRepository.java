package com.ssd.agenda_SSD_back.repository;

import com.ssd.agenda_SSD_back.entity.LogUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogUpdateRepository extends JpaRepository<LogUpdate, Long> {

}
