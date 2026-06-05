package com.practica2.gestionexcepciones.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatMessage {
    private String content;
    private String sender;
    private String recipient; // NUEVO: Para saber a quién va el mensaje privado
    private MessageType type;

    public enum MessageType {
        CHAT, JOIN, LEAVE, PRIVADO
    }
}