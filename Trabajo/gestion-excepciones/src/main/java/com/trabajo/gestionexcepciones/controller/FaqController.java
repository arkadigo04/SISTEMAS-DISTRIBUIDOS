package com.trabajo.gestionexcepciones.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faq")
public class FaqController {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${GEMINI_API_KEY:NO_KEY}")
    private String geminiApiKey;

    @PostMapping
    public ResponseEntity<Map<String, String>> preguntarOak(@RequestBody Map<String, String> request) {
        String preguntaUsuario = request.get("pregunta");

        String prompt = "Eres el Profesor Oak, el famoso investigador del mundo Pokémon. " +
                "Estás ayudando a un usuario en una aplicación web de la Liga Pokémon. " +
                "El usuario puede hacerte preguntas sobre cómo usar la aplicación (gestión de Entrenadores y Gimnasios) " +
                "o preguntas sobre el propio universo Pokémon. " +
                "Responde de forma breve (máximo 3 frases), con tono muy amigable, sabio, y metiéndote totalmente en tu papel a esta pregunta: " + preguntaUsuario;

        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + geminiApiKey;
        try {
            // Construye el JSON de la petición de forma segura
            Map<String, Object> bodyMap = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );
            String requestBody = objectMapper.writeValueAsString(bodyMap);

            String response = restClient.post()
                    .uri(geminiUrl)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String oakResponse = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            return ResponseEntity.ok(Map.of("respuesta", oakResponse));
        } catch (Exception e) {
            // Registrar error de la llamada externa para depuración
            System.err.println("ERROR AL LLAMAR A GEMINI: " + e.getMessage());
            return ResponseEntity.ok(Map.of("respuesta", "Vaya... parece que el servidor no está disponible. Inténtalo más tarde."));
        }
    }
}