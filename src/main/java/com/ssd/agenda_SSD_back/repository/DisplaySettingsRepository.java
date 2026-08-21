package com.ssd.agenda_SSD_back.repository;

import com.ssd.agenda_SSD_back.entity.DisplaySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisplaySettingsRepository extends JpaRepository<DisplaySettings, Long> {
}
