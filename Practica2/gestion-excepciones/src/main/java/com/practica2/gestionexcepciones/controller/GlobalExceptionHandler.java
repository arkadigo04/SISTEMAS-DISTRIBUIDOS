package com.practica2.gestionexcepciones.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpStatusCodeException.class)
    public String manejarErroresApi(HttpStatusCodeException ex, Model model){
        String mensajeTraducido = "Ocurrió un error desconocido de conexion.";
        String respuestaCuerpo = ex.getResponseBodyAsString();

        if (respuestaCuerpo.contains("FILE_ERROR") || ex.getStatusCode().value() == 404){
            mensajeTraducido = "Excepción Capturada: El sistema Python no pudo encontrar el archivo solicitado.";
        } else if (respuestaCuerpo.contains("DB_ERROR") || ex.getStatusCode().value() == 500) {
            mensajeTraducido = "Excepción Capturada: Error crítico al intentar conectar o leer la base de datos externa.";
        } else if (respuestaCuerpo.contains("API_THIRD_PARTY_ERROR") || ex.getStatusCode().value() == 502) {
            mensajeTraducido = "Excepción Capturada: La llamada a la PokeAPI externa ha fallado o el Pokémon no existe.";
        }

        model.addAttribute("error_traducido", mensajeTraducido);
        return "pruebas_api";
    }
}
