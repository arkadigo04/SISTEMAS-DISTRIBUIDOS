package com.practica2.gestionexcepciones.controller;

import com.practica2.gestionexcepciones.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorTestController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/")
    public String mostrarPantallaPruebas() {
        return "pruebas_api"; // Redirigimos la raíz a nuestra pantalla
    }

    @GetMapping("/probar/archivo")
    public String probarArchivo() {
        apiService.testArchivo();
        return "pruebas_api";
    }

    @GetMapping("/probar/basedatos")
    public String probarBD() {
        apiService.testBaseDatos();
        return "pruebas_api";
    }

    @GetMapping("/probar/pokemon")
    public String probarPokemon() {
        apiService.testPokemon();
        return "pruebas_api";
    }
}