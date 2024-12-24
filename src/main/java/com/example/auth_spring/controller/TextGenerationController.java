package com.example.auth_spring.controller;

import com.example.auth_spring.model.User;
import com.example.auth_spring.service.TextGenerationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Controller
@AllArgsConstructor
@RequestMapping("/generate")
public class TextGenerationController {

    TextGenerationService textGenerationService;
    @PostMapping("")
    public ResponseEntity<String> generateText(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        String model = body.get("model");
        try{
            String answer = textGenerationService.askQuestion(model,prompt);
            return ResponseEntity.ok(answer);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Что-то пошло не так, попробуйте позже");
        }
    }

}
