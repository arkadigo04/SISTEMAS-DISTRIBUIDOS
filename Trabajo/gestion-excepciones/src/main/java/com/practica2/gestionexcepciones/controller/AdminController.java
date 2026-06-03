package com.practica2.gestionexcepciones.controller;

import com.practica2.gestionexcepciones.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usuarios")
public class AdminController {

    @Autowired
    private UserService userService;

    // Leer (Read) - Lista todos los usuarios
    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", userService.obtenerTodos());
        return "usuarios"; // Llama a la plantilla usuarios.html
    }

    // Borrar (Delete) - Elimina un usuario por su ID
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        userService.eliminarUsuario(id);
        return "redirect:/usuarios?eliminado=true";
    }
}