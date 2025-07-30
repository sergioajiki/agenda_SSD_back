package com.ssd.agenda_SSD_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @Value("${spring.application.name:agenda_SSD_back}")
    private  String appName;

    @Value("${spring.application.version:1.0.0}")
    private String appVersion;
    @GetMapping("/health")
    public String healthCheck(){
        return "Aplicação ativa: " + appName + " | Versão: " + appVersion;
    }
}
