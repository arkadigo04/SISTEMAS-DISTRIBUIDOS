package com.practica2.gestionexcepciones.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ApiService {

    private final RestClient restClient = RestClient.create();
    // Si la variable "PYTHON_URL" existe (en Docker), la usa. Si no, usa localhost (para pruebas locales).
    private final String BASE_URL = System.getenv("PYTHON_URL") != null
            ? System.getenv("PYTHON_URL")
            : "http://localhost:5000/api";

    public String leerArchivo(String nombre) {
        return restClient.get()
                .uri(BASE_URL + "/archivo/" + nombre)
                .retrieve()
                .body(String.class);
    }

    public String consultarBD(String tabla) {
        // Pasar la tabla a Python
        return restClient.get()
                .uri(BASE_URL + "/basedatos/" + tabla)
                .retrieve()
                .body(String.class);
    }

    public String buscarPokemon(String nombre) {
        return restClient.get()
                .uri(BASE_URL + "/pokemon/" + nombre)
                .retrieve()
                .body(String.class);
    }
}