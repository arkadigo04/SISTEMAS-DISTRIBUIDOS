package com.practica2.gestionexcepciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practica2.gestionexcepciones.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/entrenadores")
public class EntrenadorController {

    @Autowired
    private ApiService apiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // READ: Cargar la vista con todos los datos
    @GetMapping
    public String listarEntrenadores(Model model) throws Exception {
        String resultado = apiService.consultarBD("entrenadores");
        Map<String, Object> map = objectMapper.readValue(resultado, Map.class);
        model.addAttribute("entrenadores", map.get("datos"));
        return "entrenadores";
    }

    // CREATE & UPDATE: Guardar o editar usando el mismo formulario
    @PostMapping("/guardar")
    public String guardarEntrenador(@RequestParam(required = false) Long id,
                                    @RequestParam String nombre,
                                    @RequestParam int medallas) {
        if (id == null) {
            apiService.añadirEntrenador(nombre, medallas); // Create
        } else {
            apiService.actualizarEntrenador(id, nombre, medallas); // Update
        }
        return "redirect:/entrenadores?exito=true";
    }

    // DELETE: Borrar un entrenador
    @PostMapping("/eliminar/{id}")
    public String eliminarEntrenador(@PathVariable Long id) {
        apiService.eliminarEntrenador(id);
        return "redirect:/entrenadores?eliminado=true";
    }
}