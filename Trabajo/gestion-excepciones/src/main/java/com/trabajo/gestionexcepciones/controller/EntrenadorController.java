package com.trabajo.gestionexcepciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabajo.gestionexcepciones.service.ApiService;
import com.trabajo.gestionexcepciones.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/entrenadores")
public class EntrenadorController {

    // Inyecta RabbitMQ para publicar tareas de envío de correo
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ApiService apiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cargar la vista con la lista de entrenadores
    @GetMapping
    public String listarEntrenadores(Model model) throws Exception {
        String resultado = apiService.consultarBD("entrenadores");
        Map<String, Object> map = objectMapper.readValue(resultado, Map.class);
        model.addAttribute("entrenadores", map.get("datos"));
        return "entrenadores";
    }

    // Guardar o actualizar entrenador con el mismo endpoint
    @PostMapping("/guardar")
    public String guardarEntrenador(@RequestParam(required = false) Long id,
                                    @RequestParam String nombre,
                                    @RequestParam int medallas,
                                    @RequestParam(required = false) String correo) {

        if (id == null) {
            // Crear entrenador en el servicio de Python
            apiService.añadirEntrenador(nombre, medallas); // Create

            // Si hay correo, publicar el mensaje en RabbitMQ
            if (correo != null && !correo.isEmpty()) {
                // Formato: nombre||medallas||correo
                String payload = nombre + "||" + medallas + "||" + correo;

                rabbitTemplate.convertAndSend(RabbitMQConfig.COLA_EMAILS, payload);
                System.out.println("Tarea enviada a RabbitMQ: " + payload);
            }

        } else {
            // Actualizar datos del entrenador, sin enviar correo
            apiService.actualizarEntrenador(id, nombre, medallas); // Update
        }

        return "redirect:/entrenadores?exito=true";
    }

    // Eliminar un entrenador existente
    @PostMapping("/eliminar/{id}")
    public String eliminarEntrenador(@PathVariable Long id) {
        apiService.eliminarEntrenador(id);
        return "redirect:/entrenadores?eliminado=true";
    }
}