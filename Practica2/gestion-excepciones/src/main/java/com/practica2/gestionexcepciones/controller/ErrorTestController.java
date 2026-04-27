package com.practica2.gestionexcepciones.controller;

import com.practica2.gestionexcepciones.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ErrorTestController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/login")
    public String mostrarLogin() { return "login"; }

    @GetMapping("/")
    public String mostrarInicio() { return "index"; }

    @GetMapping("/pruebas-api")
    public String mostrarPantallaPruebas() { return "pruebas_api"; }

    // --- Endpoints Funcionales ---

    @GetMapping("/funcional/archivo")
    public String probarArchivo(@RequestParam String nombreArchivo, Model model) {
        String resultado = apiService.leerArchivo(nombreArchivo);
        model.addAttribute("exito", "Resultado de Archivo: " + resultado);
        return "pruebas_api";
    }

    @GetMapping("/funcional/basedatos")
    public String probarBD(@RequestParam String nombreTabla, Model model) {
        String resultado = apiService.consultarBD(nombreTabla);
        model.addAttribute("exito", "Registro de la Liga Pokémon: " + resultado);
        return "pruebas_api";
    }

    @GetMapping("/funcional/pokemon")
    public String probarPokemon(@RequestParam String nombrePokemon, Model model) {
        String resultado = apiService.buscarPokemon(nombrePokemon.toLowerCase());
        model.addAttribute("exito", "Pokémon encontrado: " + resultado);
        return "pruebas_api";
    }
}