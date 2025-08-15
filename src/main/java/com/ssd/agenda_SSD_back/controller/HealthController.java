package com.ssd.agenda_SSD_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/health")
public class HealthController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.application.name:agenda_SSD_back}")
    private  String appName;

    @Value("${spring.application.version:1.0.0}")
    private String appVersion;
    @GetMapping
    public Map<String, Object> getHealthStatus(){
        Map<String, Object> healthStatus = new HashMap<>();

        //Informações gerais

        healthStatus.put("applicationName", appName);
        healthStatus.put("version", appVersion);
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", LocalDateTime.now());

        //Tempo de execução do servidor
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        healthStatus.put("uptime", uptime + " ms");

        //Recursos do sistema
        Runtime runtime = Runtime.getRuntime();
        healthStatus.put("freeMemory", runtime.freeMemory() / (1024 * 1024) + " MB");
        healthStatus.put("totalMemory", runtime.totalMemory() / (1024 * 1024) + " MB");
        healthStatus.put("maxMemory", runtime.maxMemory() / (1024 * 1024) + " MB");

        //Verificação do Banco de Dados
        try {
            jdbcTemplate.queryForObject("Select 1", Integer.class);
            healthStatus.put("databaseStatus", "UP");
        } catch (Exception e) {
            healthStatus.put("databaseStatus", "DOWN");
            healthStatus.put("databaseError", e.getMessage());
        }

        return healthStatus;
    };
}
