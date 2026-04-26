package com.practica2.gestionexcepciones.service;

import org.springframework.web.client.RestTemplate;

public class ApiService {
    private final RestTemplate restTemplate = new RestTemplate();
    //Apuntar a api de python
    private final String BASE_URL = "http://localhost:5000/api/error";

    public String testArchivo() {
        return restTemplate.getForObject(BASE_URL + "/archivo", String.class);
    }

    public String testBaseDatos() {
        return restTemplate.getForObject(BASE_URL + "/basedatos", String.class);
    }

    public String testPokemon() {
        return restTemplate.getForObject(BASE_URL + "/pokemon", String.class);
    }
}
