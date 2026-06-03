package com.practica2.gestionexcepciones.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ApiService {

    private final RestClient restClient = RestClient.create();
    private final String BASE_URL = System.getenv("PYTHON_URL") != null
            ? System.getenv("PYTHON_URL")
            : "http://localhost:5000/api";

    public List leerArchivo(String nombreArchivo) {
        return restClient.get().uri(BASE_URL + "/archivo/" + nombreArchivo).retrieve().body(List.class);
    }

    public String consultarBD(String tabla) {
        return restClient.get().uri(BASE_URL + "/basedatos/" + tabla).retrieve().body(String.class);
    }

    public String buscarPokemon(String nombre) {
        return restClient.get().uri(BASE_URL + "/pokemon/" + nombre).retrieve().body(String.class);
    }

    public String añadirEntrenador(String nombre, int medallas) {
        String jsonBody = String.format("{\"nombre\": \"%s\", \"medallas\": %d}", nombre, medallas);
        return restClient.post()
                .uri(BASE_URL + "/basedatos/entrenadores")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .retrieve()
                .body(String.class);
    }

    public String simularError(String codigoError) {
        return restClient.get()
                .uri(BASE_URL + "/test-error/" + codigoError)
                .retrieve()
                .body(String.class);
    }

    public void actualizarEntrenador(Long id, String nombre, int medallas) {
        String jsonBody = String.format("{\"nombre\": \"%s\", \"medallas\": %d}", nombre, medallas);
        restClient.put()
                .uri(BASE_URL + "/basedatos/entrenadores/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .retrieve()
                .toBodilessEntity();
    }

    public void eliminarEntrenador(Long id) {
        restClient.delete()
                .uri(BASE_URL + "/basedatos/entrenadores/" + id)
                .retrieve()
                .toBodilessEntity();
    }

    public String añadirGimnasio(String nombre, String ciudad) {
        String jsonBody = String.format("{\"nombre\": \"%s\", \"ciudad\": \"%s\"}", nombre, ciudad);
        return restClient.post()
                .uri(BASE_URL + "/basedatos/gimnasios")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .retrieve()
                .body(String.class);
    }

    public void actualizarGimnasio(Long id, String nombre, String ciudad) {
        String jsonBody = String.format("{\"nombre\": \"%s\", \"ciudad\": \"%s\"}", nombre, ciudad);
        restClient.put()
                .uri(BASE_URL + "/basedatos/gimnasios/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .retrieve()
                .toBodilessEntity();
    }

    public void eliminarGimnasio(Long id) {
        restClient.delete()
                .uri(BASE_URL + "/basedatos/gimnasios/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}