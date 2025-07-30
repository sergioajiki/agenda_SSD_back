package com.ssd.agenda_SSD_back.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="meeting") // Nome da tabela
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String title;
    @Column(nullable = false)
    private LocalDate data;
    @Column(nullable = false)
    private LocalTime timeStart;
    @Column(nullable = false)
    private LocalTime timeEnd;
    @Column(nullable = false)
    private User userId;
}
