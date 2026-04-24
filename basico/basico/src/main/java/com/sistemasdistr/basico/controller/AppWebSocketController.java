package com.sistemasdistr.basico.controller.rabitmq;

import com.sistemasdistr.basico.config.websocket.OutputMessage;
import com.sistemasdistr.basico.dto.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class AppWebSocketController {

    @GetMapping("/mensajes")
    public String mensaje(ModelMap interfazConPantalla){

        return "mensajes";
    }

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public OutputMessage send(Message message){
        String time=new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(message.getFrom(), message.getText(), time);
    }

    @MessageMapping("/register")
    @SendTo("/topic/register")
    public OutputMessage register(UserDTO dto) throws Exception {
        System.out.println("Registrando usuario: " + dto.getUsername());
        // Set authentication manually
        setSecurityContextFrom(dto.getUsername());
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        userService.register(dto.getUsername(), "nolausoaun");
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(dto.getUsername(), "Me he registrado enviame un mensaje", time, "todos");
    }

    public void setSecurityContextFrom(String username) {

        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
