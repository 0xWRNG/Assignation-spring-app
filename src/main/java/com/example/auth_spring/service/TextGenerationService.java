package com.example.auth_spring.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TextGenerationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String serverUrl = "http://25.50.144.203:11434/api/";

    public String askQuestion(String modelName, String question) {
        String endpoint = serverUrl + "/generate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format("{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}", modelName, question);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            String raw_response =  restTemplate.postForObject(endpoint, request, String.class);
            try {
                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode rootNode = objectMapper.readTree(raw_response);

                return rootNode.get("response").asText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка: " + e.getMessage();
        }
        return null;
    }
}
