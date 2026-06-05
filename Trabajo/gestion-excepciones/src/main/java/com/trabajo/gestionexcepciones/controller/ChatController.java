package com.trabajo.gestionexcepciones.controller;

import com.trabajo.gestionexcepciones.model.ChatMessage;
import com.trabajo.gestionexcepciones.model.MensajeChat;
import com.trabajo.gestionexcepciones.repository.MensajeChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MensajeChatRepository chatRepository;

    @Autowired
    private SimpUserRegistry userRegistry;

    // Controlador para la vista principal de chat
    @GetMapping("/chat")
    public String mostrarChat() {
        return "chat"; // Devuelve la plantilla chat.html
    }

    // Endpoints REST para usuarios activos e historial de mensajes

    @GetMapping("/api/chat/usuarios-activos")
    @ResponseBody
    public List<String> obtenerUsuariosActivos() {
        return userRegistry.getUsers().stream()
                .map(user -> user.getName())
                .collect(Collectors.toList());
    }

    @GetMapping("/api/chat/historial/global")
    @ResponseBody
    public List<MensajeChat> obtenerHistorialGlobal() {
        return chatRepository.findByDestinatarioOrderByFechaHoraAsc("GLOBAL");
    }

    @GetMapping("/api/chat/historial/privado/{otroUsuario}")
    @ResponseBody
    public List<MensajeChat> obtenerHistorialPrivado(@PathVariable String otroUsuario, Principal principal) {
        return chatRepository.obtenerHistorialPrivado(principal.getName(), otroUsuario);
    }

    // WebSocket para chat global

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        guardarMensajeEnBD(chatMessage, "GLOBAL");
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    // WebSocket para mensajes privados

    @MessageMapping("/chat.sendPrivate")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        guardarMensajeEnBD(chatMessage, chatMessage.getRecipient());
        chatMessage.setType(ChatMessage.MessageType.PRIVADO);

        // Enviar al destinatario privado
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipient(), "/queue/reply", chatMessage
        );
        // También enviar al remitente para mostrar el mensaje localmente
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSender(), "/queue/reply", chatMessage
        );
    }

    // Métodos auxiliares

    private void guardarMensajeEnBD(ChatMessage chatMessage, String destinatario) {
        MensajeChat msg = new MensajeChat();
        msg.setRemitente(chatMessage.getSender());
        msg.setDestinatario(destinatario);
        msg.setContenido(chatMessage.getContent());
        msg.setFechaHora(LocalDateTime.now());
        msg.setTipo(chatMessage.getType().name());
        chatRepository.save(msg);
    }
}