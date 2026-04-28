package com.practica2.gestionexcepciones.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ApiService {

    private final RestClient restClient = RestClient.create();
    private final String BASE_URL = System.getenv("PYTHON_URL") != null
            ? System.getenv("PYTHON_URL")
            : "http://localhost:5000/api";

    public String leerArchivo(String nombre) {
        return restClient.get().uri(BASE_URL + "/archivo/" + nombre).retrieve().body(String.class);
    }

    public String consultarBD(String tabla) {
        return restClient.get().uri(BASE_URL + "/basedatos/" + tabla).retrieve().body(String.class);
    }

    public String buscarPokemon(String nombre) {
        return restClient.get().uri(BASE_URL + "/pokemon/" + nombre).retrieve().body(String.class);
    }

    // NUEVO: Método para guardar entrenadores
    public String añadirEntrenador(String nombre, int medallas) {
        String jsonBody = String.format("{\"nombre\": \"%s\", \"medallas\": %d}", nombre, medallas);
        return restClient.post()
                .uri(BASE_URL + "/basedatos/entrenadores")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .retrieve()
                .body(String.class);
    }

    // NUEVO: Método para probar excepciones
    public String simularError(String codigoError) {
        return restClient.get()
                .uri(BASE_URL + "/test-error/" + codigoError)
                .retrieve()
                .body(String.class);
    }
}