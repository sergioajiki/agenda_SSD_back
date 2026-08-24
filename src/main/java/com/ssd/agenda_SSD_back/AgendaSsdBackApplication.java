// Agenda SSD — Developed by S. Ajiki.
package com.ssd.agenda_SSD_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Liga o backup agendado do banco (DatabaseBackupService) — sem isso o
// @Scheduled nunca dispara.
@SpringBootApplication
@EnableScheduling
public class AgendaSsdBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendaSsdBackApplication.class, args);
	}

}
