package com.trabajo.gestionexcepciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trabajo.gestionexcepciones.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/gimnasios")
public class GimnasioController {

    @Autowired
    private ApiService apiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public String listarGimnasios(Model model) throws Exception {
        String resultado = apiService.consultarBD("gimnasios");
        Map<String, Object> map = objectMapper.readValue(resultado, Map.class);
        model.addAttribute("gimnasios", map.get("datos"));
        return "gimnasios";
    }

    @PostMapping("/guardar")
    public String guardarGimnasio(@RequestParam(required = false) Long id,
                                  @RequestParam String nombre,
                                  @RequestParam String ciudad) {
        if (id == null) {
            apiService.añadirGimnasio(nombre, ciudad);
        } else {
            apiService.actualizarGimnasio(id, nombre, ciudad);
        }
        return "redirect:/gimnasios?exito=true";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarGimnasio(@PathVariable Long id) {
        apiService.eliminarGimnasio(id);
        return "redirect:/gimnasios?eliminado=true";
    }
}