package com.trabajo.gestionexcepciones.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Maneja errores HTTP provenientes de los servicios externos.
    @ExceptionHandler(HttpStatusCodeException.class)
    public String manejarErroresApi(HttpStatusCodeException ex, Model model){
        String mensajeTraducido = "Se produjo un error de conexión con el servicio externo.";
        String respuestaCuerpo = ex.getResponseBodyAsString();

        if (respuestaCuerpo.contains("FILE_ERROR") || ex.getStatusCode().value() == 404){
            mensajeTraducido = "No se encontró el recurso solicitado en el servicio Python.";
        } else if (respuestaCuerpo.contains("DB_ERROR") || ex.getStatusCode().value() == 500) {
            mensajeTraducido = "Error en la base de datos externa al procesar la petición.";
        } else if (respuestaCuerpo.contains("API_THIRD_PARTY_ERROR") || ex.getStatusCode().value() == 502) {
            mensajeTraducido = "Fallo en la llamada externa a la API de Pokémon o el recurso no está disponible.";
        }

        model.addAttribute("error_traducido", mensajeTraducido);
        return "pruebas_api";
    }
}
