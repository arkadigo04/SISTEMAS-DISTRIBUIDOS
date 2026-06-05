package com.practica2.gestionexcepciones.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    public void enviarAlertaNuevoEntrenador(String nombreEntrenador, int medallas) {
        SimpleMailMessage mensaje = new SimpleMailMessage();

        // IMPORTANTE: Outlook exige que el remitente sea exactamente el mismo email que se loguea
        mensaje.setFrom(remitente);

        // Nos enviamos el correo a nosotros mismos como administradores del sistema
        mensaje.setTo(remitente);

        mensaje.setSubject("ALERTA LIGA POKÉMON: Nuevo Entrenador Registrado");
        mensaje.setText("Notificación del Sistema Distribuido:\n\n" +
                "Se ha registrado exitosamente a un nuevo entrenador en la base de datos de PostgreSQL.\n\n" +
                "Nombre: " + nombreEntrenador + "\n" +
                "Medallas Actuales: " + medallas + "\n\n" +
                "Revisa la terminal de gestión para más detalles.");

        mailSender.send(mensaje);
    }
}