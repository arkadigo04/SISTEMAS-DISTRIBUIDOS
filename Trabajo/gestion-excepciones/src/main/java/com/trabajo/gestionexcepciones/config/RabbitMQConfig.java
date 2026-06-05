package com.trabajo.gestionexcepciones.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Nombre oficial de nuestro "buzón"
    public static final String COLA_EMAILS = "cola_emails_bienvenida";

    @Bean
    public Queue emailsQueue() {
        // El parámetro 'true' hace que la cola sea persistente en disco
        return new Queue(COLA_EMAILS, true);
    }
}