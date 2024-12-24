package com.example.auth_spring;

import com.example.auth_spring.service.TextGenerationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TextGenerationServiceTest {

    private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
    private final TextGenerationService textGenerationService = new TextGenerationService();

    @Test
    void testAskQuestion() throws Exception {
        String modelName = "llama2:latest";
        String question = "Как дела?";
        String jsonResponse = "{\"response\": \"Все хорошо!\"}";

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(jsonResponse);

        String response = textGenerationService.askQuestion(modelName, question);
        assertNotNull(response);
    }

}
