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

    // AÑADIDO: Recibimos el correoDestino del formulario
    public void enviarAlertaNuevoEntrenador(String nombreEntrenador, int medallas, String correoDestino) {

        if(correoDestino == null || correoDestino.isEmpty()) {
            return; // Si por lo que sea no hay correo, abortamos
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);

        // AHORA SÍ: Se lo enviamos al entrenador nuevo
        mensaje.setTo(correoDestino);

        mensaje.setSubject("¡Bienvenido a la Liga Pokémon Oficial!");
        mensaje.setText("Hola " + nombreEntrenador + ",\n\n" +
                "Tu registro en la Liga Pokémon ha sido completado con éxito a través de nuestro Sistema Distribuido.\n\n" +
                "Datos de tu ficha:\n" +
                "- Nombre: " + nombreEntrenador + "\n" +
                "- Medallas Registradas: " + medallas + "\n\n" +
                "¡Mucho éxito en tu aventura para convertirte en Maestro Pokémon!");

        mailSender.send(mensaje);
    }
}