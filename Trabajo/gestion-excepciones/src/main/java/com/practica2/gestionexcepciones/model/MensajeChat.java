package com.practica2.gestionexcepciones.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mensajes_chat")
public class MensajeChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String remitente;
    private String destinatario; // "GLOBAL" o el nombre del usuario
    private String contenido;
    private LocalDateTime fechaHora;
    private String tipo;
}