package com.trabajo.gestionexcepciones.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
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

    // Listener de RabbitMQ para procesar correos de bienvenida en segundo plano
    @RabbitListener(queues = "cola_emails_bienvenida")
    public void procesarEmailDesdeCola(String payload) {
        System.out.println("RabbitMQ recibió tarea: " + payload);

        try {
            // Extrae los valores enviados desde el controlador
            String[] datos = payload.split("\\|\\|");
            String nombreEntrenador = datos[0];
            int medallas = Integer.parseInt(datos[1]);
            String correoDestino = datos[2];

            // Construye y envía el mensaje de bienvenida
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(correoDestino);
            mensaje.setSubject("¡Bienvenido a la Liga Pokémon Oficial!");
            mensaje.setText("Hola " + nombreEntrenador + ",\n\n" +
                    "Tu registro en la Liga Pokémon ha sido completado con éxito a través de nuestro Sistema Distribuido.\n\n" +
                    "Datos de tu ficha:\n" +
                    "- Nombre: " + nombreEntrenador + "\n" +
                    "- Medallas Registradas: " + medallas + "\n\n" +
                    "¡Mucho éxito en tu aventura para convertirte en Maestro Pokémon!");

            mailSender.send(mensaje);
            System.out.println("Correo de bienvenida enviado a " + correoDestino);

        } catch (Exception e) {
            System.err.println("Fallo al procesar el correo de bienvenida: " + e.getMessage());
            // En producción este error debería derivarse a una Dead Letter Queue o registro persistente
        }
    }
}