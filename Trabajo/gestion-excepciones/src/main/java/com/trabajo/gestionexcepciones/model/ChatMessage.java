package com.trabajo.gestionexcepciones.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatMessage {
    private String content;
    private String sender;
    private String recipient;
    private MessageType type;

    public enum MessageType {
        CHAT, JOIN, LEAVE, PRIVADO
    }
}